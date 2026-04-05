const OllamaAdapter = require('./ollama-adapter');
const LMStudioAdapter = require('./lmstudio-adapter');
const OpenCodeAdapter = require('./opencode-adapter');
const DockerModelRunnerAdapter = require('./dmr-adapter');

const PROVIDERS = {
    ollama: {
        Adapter: OllamaAdapter,
        label: 'Ollama',
        defaultModel: 'gemma3:4b',
        defaultHost: 'http://localhost:11434'
    },
    lmstudio: {
        Adapter: LMStudioAdapter,
        label: 'LM Studio',
        defaultModel: '',
        defaultHost: 'http://localhost:1234'
    },
    opencode: {
        Adapter: OpenCodeAdapter,
        label: 'OpenCode',
        defaultModel: 'codellama',
        defaultHost: 'http://localhost:50561'
    },
    dmr: {
        Adapter: DockerModelRunnerAdapter,
        label: 'Docker Model Runner',
        defaultModel: 'llama3.2',
        defaultHost: 'http://localhost:12434'
    }
};

class ModelRegistry {
    constructor() {
        this.adapters = new Map();
        this.defaultProvider = 'ollama';
    }

    getProviderInfo() {
        return Object.entries(PROVIDERS).map(([key, info]) => ({
            id: key,
            label: info.label,
            defaultModel: info.defaultModel,
            defaultHost: info.defaultHost
        }));
    }

    getAdapter(name) {
        if (!this.adapters.has(name)) {
            const info = PROVIDERS[name];
            if (!info) return null;
            this.adapters.set(name, new info.Adapter({
                model: info.defaultModel,
                host: info.defaultHost
            }));
        }
        return this.adapters.get(name);
    }

    setDefaultProvider(name) {
        if (PROVIDERS[name]) {
            this.defaultProvider = name;
        }
    }

    getDefaultProvider() {
        return this.defaultProvider;
    }

    async generate(providerName, prompt, options = {}) {
        const adapter = this.getAdapter(providerName || this.defaultProvider);
        if (!adapter) throw new Error(`Provider '${providerName}' not found`);
        return await adapter.generate(prompt, options);
    }

    async generateDefault(prompt, options = {}) {
        return await this.generate(this.defaultProvider, prompt, options);
    }

    async checkProvider(name) {
        const adapter = this.getAdapter(name);
        if (!adapter) return false;
        return await adapter.isAvailable();
    }

    async checkAllProviders() {
        const results = {};
        for (const [name, info] of Object.entries(PROVIDERS)) {
            try {
                results[name] = await this.checkProvider(name);
            } catch {
                results[name] = false;
            }
        }
        return results;
    }
}

const modelRegistry = new ModelRegistry();
module.exports = modelRegistry;
