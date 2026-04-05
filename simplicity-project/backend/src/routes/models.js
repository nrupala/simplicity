const express = require('express');
const modelRegistry = require('../models/registry');
const { downloadModel, listModels, deleteModel, getSystemMemory, estimateMemoryRequirements, updateModelMetadata } = require('../engines/inference/model-loader');
const inferenceEngine = require('../engines/inference/inference');
const { embedAllChunks, getCacheStats, demoteColdChunks } = require('../engines/inference/embedding-engine');

const router = express.Router();

// Helper: resolve model path by ID (works for both internal and external)
function resolveModelPath(modelId) {
    const models = listModels();
    const model = models.find(m => m.id === modelId);
    if (!model) return null;
    if (!model.exists) return null;
    return model.path;
}

// List all available models with system status
router.get('/available', async (req, res) => {
    try {
        const models = listModels();
        const engineStatus = await modelRegistry.getNativeStatus();
        const memory = getSystemMemory();

        res.json({
            models,
            engine: engineStatus,
            system: {
                memory,
                cpus: require('os').cpus().length,
                platform: require('os').platform(),
                arch: require('os').arch()
            }
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Download model from HuggingFace
router.post('/download', async (req, res) => {
    try {
        const { repo, filename, onProgress } = req.body;
        if (!repo) return res.status(400).json({ error: 'repo is required (e.g., "bartowski/gemma-3-4b-it-GGUF")' });

        const hfUrl = filename
            ? `https://huggingface.co/${repo}/resolve/main/${filename}`
            : repo;

        const result = await downloadModel(hfUrl, {
            filename: filename || hfUrl.split('/').pop(),
            onProgress: (progress) => {
                // Could emit SSE progress events here
                console.log(`Download progress: ${(progress * 100).toFixed(1)}%`);
            }
        });

        res.json({
            success: true,
            message: result.alreadyExists ? 'Model already exists' : 'Model downloaded successfully',
            model: {
                id: result.id,
                filename: result.filename,
                size: result.size,
                sizeGB: (result.size / (1024 ** 3)).toFixed(2),
                path: result.path
            }
        });
    } catch (error) {
        res.status(500).json({ error: `Download failed: ${error.message}` });
    }
});

// Load model into memory
router.post('/load', async (req, res) => {
    try {
        const { modelId, modelPath, ...options } = req.body;

        if (!modelId && !modelPath) {
            return res.status(400).json({ error: 'modelId or modelPath is required' });
        }

        // Resolve model path
        let resolvedPath = modelPath;
        if (!resolvedPath && modelId) {
            resolvedPath = resolveModelPath(modelId);
            if (!resolvedPath) {
                return res.status(404).json({ error: `Model ${modelId} not found on disk` });
            }
        }

        // Check memory before loading
        const memory = getSystemMemory();
        const memLevel = inferenceEngine.getMemoryLevel(memory.free / memory.total);

        if (memLevel === 'emergency') {
            return res.status(503).json({
                error: 'System memory critically low',
                memory: { freeGB: memory.freeGB, usagePercent: memory.usagePercent.toFixed(0) + '%' },
                suggestion: 'Close other applications or increase system memory'
            });
        }

        // Load the model
        const result = await modelRegistry.loadNativeModel(modelId || resolvedPath, {
            modelId,
            modelPath: resolvedPath,
            ...options
        });

        // Update metadata
        if (modelId) {
            updateModelMetadata(modelId, { loaded: true, loadedAt: new Date().toISOString() });
        }

        res.json({
            success: true,
            message: 'Model loaded successfully',
            model: {
                id: modelId || resolvedPath,
                path: resolvedPath,
                config: result.config,
                memoryLevel: memLevel
            },
            system: {
                memory: {
                    totalGB: memory.totalGB,
                    freeGB: memory.freeGB,
                    usagePercent: memory.usagePercent.toFixed(0) + '%'
                }
            }
        });
    } catch (error) {
        console.error('Model load error:', error);
        res.status(500).json({ error: `Failed to load model: ${error.message}` });
    }
});

// Unload model from memory
router.post('/unload', async (req, res) => {
    try {
        await modelRegistry.unloadNativeModel();
        res.json({ success: true, message: 'Model unloaded' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Delete model from disk
router.delete('/:modelId', async (req, res) => {
    try {
        const { modelId } = req.params;

        // Unload first if it's the active model
        const status = await modelRegistry.getNativeStatus();
        if (status.loaded && status.modelId === modelId) {
            await modelRegistry.unloadNativeModel();
        }

        const result = deleteModel(modelId);
        if (!result.success) {
            return res.status(404).json({ error: result.error });
        }

        res.json({ success: true, message: `Deleted ${result.filename}` });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get engine status
router.get('/status', async (req, res) => {
    try {
        const status = await modelRegistry.getNativeStatus();
        const memory = getSystemMemory();
        res.json({
            ...status,
            system: {
                memory,
                memoryLevel: inferenceEngine.getMemoryLevel(memory.free / memory.total)
            }
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Update inference parameters on the fly
router.post('/config', async (req, res) => {
    try {
        const { temperature, topK, topP, minP, repeatPenalty, maxTokens, n_ctx, n_gpu_layers, n_threads } = req.body;

        const adapter = modelRegistry.getAdapter('native');
        if (!adapter) {
            return res.status(404).json({ error: 'Native adapter not available' });
        }

        if (temperature !== undefined) adapter.config.temperature = temperature;
        if (topK !== undefined) adapter.config.topK = topK;
        if (topP !== undefined) adapter.config.topP = topP;
        if (minP !== undefined) adapter.config.minP = minP;
        if (repeatPenalty !== undefined) adapter.config.repeatPenalty = repeatPenalty;
        if (maxTokens !== undefined) adapter.config.maxTokens = maxTokens;

        // These require model reload
        const needsReload = n_ctx !== undefined || n_gpu_layers !== undefined || n_threads !== undefined;

        if (needsReload && inferenceEngine.model) {
            if (n_ctx !== undefined) adapter.config.n_ctx = n_ctx;
            if (n_gpu_layers !== undefined) adapter.config.n_gpu_layers = n_gpu_layers;
            if (n_threads !== undefined) adapter.config.n_threads = n_threads;

            // Reload with new config
            const modelPath = inferenceEngine.config?.modelPath;
            if (modelPath) {
                await inferenceEngine.loadModel(modelPath, {
                    ...adapter.config,
                    modelId: inferenceEngine.loadedModelId
                });
            }
        }

        res.json({
            success: true,
            config: {
                temperature: adapter.config.temperature,
                topK: adapter.config.topK,
                topP: adapter.config.topP,
                minP: adapter.config.minP,
                repeatPenalty: adapter.config.repeatPenalty,
                maxTokens: adapter.config.maxTokens,
                n_ctx: adapter.config.n_ctx,
                n_gpu_layers: adapter.config.n_gpu_layers,
                n_threads: adapter.config.n_threads
            },
            reloaded: needsReload
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get current config
router.get('/config', async (req, res) => {
    try {
        const adapter = modelRegistry.getAdapter('native');
        if (!adapter) {
            return res.status(404).json({ error: 'Native adapter not available' });
        }
        res.json(adapter.config);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Memory monitoring
router.get('/memory', async (req, res) => {
    try {
        const memory = getSystemMemory();
        const status = await modelRegistry.getNativeStatus();
        const memLevel = inferenceEngine.getMemoryLevel(memory.free / memory.total);

        res.json({
            system: memory,
            engine: status,
            memoryLevel: memLevel,
            thresholds: inferenceEngine.memoryThresholds,
            recommendations: getMemoryRecommendations(memory, memLevel, status)
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

function getMemoryRecommendations(memory, memLevel, status) {
    const recommendations = [];

    if (memLevel === 'critical' || memLevel === 'emergency') {
        recommendations.push({
            type: 'warning',
            message: 'System memory is critically low',
            actions: [
                'Close other applications',
                'Reduce context window size',
                'Offload more layers to disk',
                'Consider using a smaller model'
            ]
        });
    }

    if (status.config && status.config.n_ctx > 4096 && memory.free < 4 * 1024 ** 3) {
        recommendations.push({
            type: 'suggestion',
            message: 'High context window with limited RAM',
            actions: ['Reduce n_ctx to 2048 or 4096 for better performance']
        });
    }

    if (status.config && status.config.n_gpu_layers === -1 && memory.free < 2 * 1024 ** 3) {
        recommendations.push({
            type: 'suggestion',
            message: 'All GPU layers loaded with low RAM',
            actions: ['Reduce n_gpu_layers to free up system memory']
        });
    }

    return recommendations;
}

// Embed all document chunks
router.post('/embed/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const result = await embedAllChunks(userId);
        res.json({ success: true, ...result });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get cache statistics
router.get('/cache/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const stats = await getCacheStats(userId);
        const actions = await demoteColdChunks(userId);
        res.json({ stats, actions });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;
