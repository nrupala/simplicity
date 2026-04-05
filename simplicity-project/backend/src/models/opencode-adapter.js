const axios = require('axios');

class OpenCodeAdapter {
    constructor(config = {}) {
        this.name = 'opencode';
        this.label = 'OpenCode';
        this.defaultPort = 8080;
        this.config = {
            host: config.host || 'http://localhost:' + this.defaultPort,
            model: config.model || 'codellama',
            temperature: config.temperature || 0.7,
            maxTokens: config.maxTokens || 2048,
            ...config
        };
    }

    async generate(prompt, options = {}) {
        const response = await axios.post(this.config.host + '/api/generate', {
            model: this.config.model,
            prompt: prompt,
            temperature: options.temperature || this.config.temperature,
            max_tokens: options.maxTokens || this.config.maxTokens,
            stream: false
        });

        const data = response.data;
        const text = data.response || data.generated_text || data.text || '';
        return {
            text,
            model: this.config.model,
            provider: this.name,
            usage: data.usage || {}
        };
    }

    async listModels() {
        try {
            const response = await axios.get(this.config.host + '/api/models');
            return response.data.models?.map(m => ({ name: m.name || m, size: 0 })) || [];
        } catch {
            return [];
        }
    }

    async isAvailable() {
        try {
            await axios.get(this.config.host + '/health', { timeout: 3000 });
            return true;
        } catch {
            return false;
        }
    }
}

module.exports = OpenCodeAdapter;
