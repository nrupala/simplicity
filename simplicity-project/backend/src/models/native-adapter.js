const inferenceEngine = require('../engines/inference/inference');
const { getModelPath, getModelPathByFilename, updateModelMetadata, listModels } = require('../engines/inference/model-loader');

class NativeAdapter {
    constructor(config = {}) {
        this.name = 'native';
        this.label = 'SIMPLICITY Engine';
        this.config = {
            modelId: config.modelId || null,
            modelPath: config.modelPath || null,
            temperature: config.temperature || 0.7,
            maxTokens: config.maxTokens || 2048,
            topK: config.topK || 40,
            topP: config.topP || 0.9,
            minP: config.minP || 0.05,
            repeatPenalty: config.repeatPenalty || 1.1,
            n_ctx: config.n_ctx,
            n_gpu_layers: config.n_gpu_layers,
            n_threads: config.n_threads,
            ...config
        };
    }

    async ensureLoaded() {
        if (inferenceEngine.model) return true;

        // Try to load from config
        let modelPath = this.config.modelPath;
        if (!modelPath && this.config.modelId) {
            modelPath = getModelPath(this.config.modelId);
        }
        if (!modelPath) {
            // Try to load first available model
            const models = listModels();
            if (models.length > 0 && models[0].exists) {
                modelPath = models[0].path;
                this.config.modelId = models[0].id;
            }
        }

        if (!modelPath) {
            throw new Error('No model loaded. Download or select a model first.');
        }

        await inferenceEngine.loadModel(modelPath, {
            modelId: this.config.modelId,
            temperature: this.config.temperature,
            maxTokens: this.config.maxTokens,
            n_ctx: this.config.n_ctx,
            n_gpu_layers: this.config.n_gpu_layers,
            n_threads: this.config.n_threads
        });

        return true;
    }

    async generate(prompt, options = {}) {
        await this.ensureLoaded();

        const result = await inferenceEngine.generate(prompt, {
            temperature: options.temperature ?? this.config.temperature,
            maxTokens: options.maxTokens ?? this.config.maxTokens,
            topK: options.topK ?? this.config.topK,
            topP: options.topP ?? this.config.topP,
            minP: options.minP ?? this.config.minP,
            repeatPenalty: options.repeatPenalty ?? this.config.repeatPenalty
        });

        return result;
    }

    async *generateStream(prompt, options = {}) {
        await this.ensureLoaded();

        yield* inferenceEngine.generateStream(prompt, {
            temperature: options.temperature ?? this.config.temperature,
            maxTokens: options.maxTokens ?? this.config.maxTokens,
            topK: options.topK ?? this.config.topK,
            topP: options.topP ?? this.config.topP,
            minP: options.minP ?? this.config.minP,
            repeatPenalty: options.repeatPenalty ?? this.config.repeatPenalty
        });
    }

    async listModels() {
        const models = listModels();
        return models.filter(m => m.exists).map(m => ({
            name: m.filename,
            size: m.size,
            id: m.id
        }));
    }

    async isAvailable() {
        if (inferenceEngine.model) return true;
        const models = listModels();
        return models.some(m => m.exists);
    }

    async getEngineStatus() {
        return inferenceEngine.getStatus();
    }

    async unloadModel() {
        await inferenceEngine.unloadModel();
        return { success: true };
    }
}

module.exports = NativeAdapter;
