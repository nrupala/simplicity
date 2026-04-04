const OllamaAdapter = require('./ollama-adapter');
const GenericModelAdapter = require('./generic-adapter');

// Model registry for SIMPLICITY
class ModelRegistry {
    constructor() {
        this.adapters = new Map();
        this.defaultModel = null;
    }

    // Register a model adapter
    registerAdapter(name, adapter) {
        this.adapters.set(name, adapter);
        if (!this.defaultModel) {
            this.defaultModel = name;
        }
    }

    // Get adapter by name
    getAdapter(name) {
        return this.adapters.get(name);
    }

    // Get all registered adapters
    getAllAdapters() {
        return Array.from(this.adapters.entries()).map(([name, adapter]) => ({
            name,
            type: adapter.constructor.name,
            config: adapter.config
        }));
    }

    // Generate text using specified model
    async generate(modelName, prompt, options = {}) {
        const adapter = this.getAdapter(modelName);
        if (!adapter) {
            throw new Error(`Model adapter '${modelName}' not found`);
        }
        return await adapter.generate(prompt, options);
    }

    // Generate using default model
    async generateDefault(prompt, options = {}) {
        if (!this.defaultModel) {
            throw new Error('No default model set');
        }
        return await this.generate(this.defaultModel, prompt, options);
    }

    // List models for all adapters
    async listAllModels() {
        const results = {};
        for (const [name, adapter] of this.adapters) {
            try {
                results[name] = await adapter.listModels();
            } catch (error) {
                console.error(`Failed to list models for ${name}:`, error);
                results[name] = [];
            }
        }
        return results;
    }

    // Initialize with default adapters
    initializeDefaults() {
        // Ollama adapter
        const ollamaAdapter = new OllamaAdapter({
            model: 'llama3.2'
        });
        this.registerAdapter('ollama', ollamaAdapter);

        // Generic adapter (can be configured for other APIs)
        const genericAdapter = new GenericModelAdapter({
            apiUrl: process.env.GENERIC_MODEL_API_URL || '',
            apiKey: process.env.GENERIC_MODEL_API_KEY || '',
            model: 'generic'
        });
        this.registerAdapter('generic', genericAdapter);
    }

    // Set default model
    setDefaultModel(name) {
        if (this.adapters.has(name)) {
            this.defaultModel = name;
        } else {
            throw new Error(`Model '${name}' not registered`);
        }
    }

    // Get default model name
    getDefaultModel() {
        return this.defaultModel;
    }
}

// Singleton instance
const modelRegistry = new ModelRegistry();
modelRegistry.initializeDefaults();

module.exports = modelRegistry;