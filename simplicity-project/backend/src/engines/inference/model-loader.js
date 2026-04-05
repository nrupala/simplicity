const fs = require('fs');
const path = require('path');
const os = require('os');
const crypto = require('crypto');
const { downloadFile } = require('@huggingface/hub');

const MODELS_DIR = path.join(__dirname, '../../../models');
const METADATA_FILE = path.join(MODELS_DIR, 'models.json');

// External model directories to scan
const EXTERNAL_MODEL_DIRS = [
    { path: path.join(os.homedir(), '.lmstudio', 'hub', 'models'), label: 'LM Studio', ext: '.gguf' },
    { path: path.join(os.homedir(), '.aitk', 'models'), label: 'AITK', ext: '.gguf' },
    { path: path.join(os.homedir(), '.ollama', 'models'), label: 'Ollama', ext: '.gguf' }
];

// Ensure models directory exists
if (!fs.existsSync(MODELS_DIR)) {
    fs.mkdirSync(MODELS_DIR, { recursive: true });
}

// Load model registry
function loadRegistry() {
    if (fs.existsSync(METADATA_FILE)) {
        try {
            return JSON.parse(fs.readFileSync(METADATA_FILE, 'utf8'));
        } catch {
            return { models: [] };
        }
    }
    return { models: [] };
}

// Save model registry
function saveRegistry(registry) {
    fs.writeFileSync(METADATA_FILE, JSON.stringify(registry, null, 2));
}

// Calculate file hash
function hashFile(filePath) {
    const hash = crypto.createHash('sha256');
    const data = fs.readFileSync(filePath);
    hash.update(data);
    return hash.digest('hex');
}

// Get system memory info
function getSystemMemory() {
    const total = os.totalmem();
    const free = os.freemem();
    const used = total - free;
    return {
        total,
        free,
        used,
        usagePercent: (used / total) * 100,
        totalGB: (total / (1024 ** 3)).toFixed(1),
        freeGB: (free / (1024 ** 3)).toFixed(1),
        usedGB: (used / (1024 ** 3)).toFixed(1)
    };
}

// Estimate model memory requirements
function estimateMemoryRequirements(fileSize, params, quantization) {
    const fileSizeGB = fileSize / (1024 ** 3);
    // KV cache: ~2 bytes * hidden_size * num_layers * context_size
    // Rough estimate: 0.5MB per token for 7B model at 4-bit
    const kvCachePerToken = (params / 1e9) * 0.5; // MB per token

    return {
        modelLoad: fileSizeGB,
        kvCache8k: (kvCachePerToken * 8192 / 1024).toFixed(1) + ' GB',
        kvCache4k: (kvCachePerToken * 4096 / 1024).toFixed(1) + ' GB',
        kvCache2k: (kvCachePerToken * 2096 / 1024).toFixed(1) + ' GB',
        recommendedRAM: (fileSizeGB * 1.5).toFixed(1) + ' GB',
        quantization: quantization || 'unknown'
    };
}

// Download model from HuggingFace
async function downloadModel(hfUrl, options = {}) {
    const { onProgress } = options;

    // Parse HuggingFace URL: https://huggingface.co/org/model/resolve/main/file.gguf
    // Or just repo ID: org/model
    let repoId, filename;

    if (hfUrl.includes('huggingface.co')) {
        const parts = hfUrl.split('/');
        const resolveIdx = parts.indexOf('resolve');
        if (resolveIdx > 0) {
            repoId = parts[resolveIdx - 2] + '/' + parts[resolveIdx - 1];
            filename = parts[resolveIdx + 1];
        } else {
            // URL without resolve - assume it's the repo
            repoId = parts[parts.length - 2] + '/' + parts[parts.length - 1];
            filename = options.filename || 'model.gguf';
        }
    } else {
        // Assume it's a repo ID
        repoId = hfUrl;
        filename = options.filename || 'model.gguf';
    }

    const destPath = path.join(MODELS_DIR, filename);

    // Check if already downloaded
    if (fs.existsSync(destPath)) {
        const stat = fs.statSync(destPath);
        return {
            success: true,
            path: destPath,
            filename,
            size: stat.size,
            alreadyExists: true
        };
    }

    console.log(`Downloading ${repoId}/${filename}...`);

    try {
        const fileIter = downloadFile({
            repo: repoId,
            path: filename
        });

        const file = await fileIter;
        if (!file) {
            throw new Error(`File not found: ${repoId}/${filename}`);
        }

        const arrayBuffer = await file.arrayBuffer();
        const buffer = Buffer.from(arrayBuffer);
        fs.writeFileSync(destPath, buffer);

        const stat = fs.statSync(destPath);
        const fileHash = hashFile(destPath);

        // Add to registry
        const registry = loadRegistry();
        registry.models.push({
            id: crypto.randomUUID(),
            filename,
            repoId,
            path: destPath,
            size: stat.size,
            hash: fileHash,
            addedAt: new Date().toISOString(),
            loaded: false
        });
        saveRegistry(registry);

        return {
            success: true,
            path: destPath,
            filename,
            size: stat.size,
            hash: fileHash
        };
    } catch (error) {
        // Clean up partial download
        if (fs.existsSync(destPath)) {
            fs.unlinkSync(destPath);
        }
        throw error;
    }
}

// Recursively find all GGUF files in a directory
function findGGUFFiles(dir, source = 'local') {
    const results = [];
    if (!fs.existsSync(dir)) return results;

    function walk(currentDir) {
        try {
            const entries = fs.readdirSync(currentDir, { withFileTypes: true });
            for (const entry of entries) {
                const fullPath = path.join(currentDir, entry.name);
                if (entry.isDirectory()) {
                    // Skip blob directories and partial downloads
                    if (entry.name === 'blobs' || entry.name === 'manifests') continue;
                    if (entry.name.startsWith('downloading_')) continue;
                    walk(fullPath);
                } else if (entry.isFile() && entry.name.endsWith('.gguf')) {
                    const stat = fs.statSync(fullPath);
                    // Skip partial downloads
                    if (entry.name.endsWith('.part') || entry.name.endsWith('.gguf.part')) continue;
                    results.push({
                        id: 'ext_' + crypto.createHash('md5').update(fullPath).digest('hex'),
                        filename: entry.name,
                        path: fullPath,
                        size: stat.size,
                        source,
                        external: true,
                        addedAt: stat.birthtime.toISOString(),
                        loaded: false
                    });
                }
            }
        } catch {}
    }

    walk(dir);
    return results;
}

// List all models: internal registry + external directories
function listModels() {
    const registry = loadRegistry();
    const memory = getSystemMemory();
    const allModels = [...registry.models];

    // Scan external model directories
    for (const extDir of EXTERNAL_MODEL_DIRS) {
        if (fs.existsSync(extDir.path)) {
            const externalModels = findGGUFFiles(extDir.path, extDir.label);
            for (const ext of externalModels) {
                // Avoid duplicates - check if already in registry by path
                if (!allModels.some(m => m.path === ext.path)) {
                    allModels.push(ext);
                }
            }
        }
    }

    return allModels.map(m => {
        const exists = fs.existsSync(m.path);
        const stat = exists ? fs.statSync(m.path) : null;
        return {
            ...m,
            exists,
            size: stat?.size || m.size,
            sizeGB: stat ? (stat.size / (1024 ** 3)).toFixed(2) : 'N/A',
            memoryEstimate: stat ? estimateMemoryRequirements(stat.size, 0, m.quantization) : null,
            systemMemory: {
                totalGB: memory.totalGB,
                freeGB: memory.freeGB,
                usagePercent: memory.usagePercent.toFixed(0)
            }
        };
    });
}

// Delete a model
function deleteModel(modelId) {
    const registry = loadRegistry();
    const idx = registry.models.findIndex(m => m.id === modelId);
    if (idx === -1) return { success: false, error: 'Model not found' };

    const model = registry.models[idx];
    if (fs.existsSync(model.path)) {
        fs.unlinkSync(model.path);
    }
    registry.models.splice(idx, 1);
    saveRegistry(registry);

    return { success: true, filename: model.filename };
}

// Get model file path by ID
function getModelPath(modelId) {
    const registry = loadRegistry();
    const model = registry.models.find(m => m.id === modelId);
    if (!model) return null;
    if (!fs.existsSync(model.path)) return null;
    return model.path;
}

// Get model file path by filename
function getModelPathByFilename(filename) {
    const registry = loadRegistry();
    const model = registry.models.find(m => m.filename === filename);
    if (!model) return null;
    if (!fs.existsSync(model.path)) return null;
    return model.path;
}

// Update model metadata (loaded status, etc.)
function updateModelMetadata(modelId, updates) {
    const registry = loadRegistry();
    const model = registry.models.find(m => m.id === modelId);
    if (!model) return false;
    Object.assign(model, updates);
    saveRegistry(registry);
    return true;
}

module.exports = {
    MODELS_DIR,
    downloadModel,
    listModels,
    deleteModel,
    getModelPath,
    getModelPathByFilename,
    updateModelMetadata,
    getSystemMemory,
    estimateMemoryRequirements,
    loadRegistry,
    saveRegistry
};
