const axios = require('axios');

class LMStudioAdapter {
    constructor(config = {}) {
        this.name = 'lmstudio';
        this.label = 'LM Studio';
        this.defaultPort = 1234;
        this.config = {
            host: config.host || 'http://localhost:' + this.defaultPort,
            model: config.model || '',
            temperature: config.temperature || 0.7,
            maxTokens: config.maxTokens || 2048,
            ...config
        };
    }

    async generate(prompt, options = {}) {
        const response = await axios.post(this.config.host + '/v1/chat/completions', {
            model: this.config.model || 'local-model',
            messages: [{ role: 'user', content: prompt }],
            temperature: options.temperature || this.config.temperature,
            max_tokens: options.maxTokens || this.config.maxTokens
        });

        const text = response.data.choices?.[0]?.message?.content || '';
        return {
            text,
            model: this.config.model,
            provider: this.name,
            usage: response.data.usage || {}
        };
    }

    async listModels() {
        try {
            const response = await axios.get(this.config.host + '/v1/models');
            return response.data.data.map(m => ({ name: m.id, size: 0 }));
        } catch {
            return [];
        }
    }

    async isAvailable() {
        try {
            await axios.get(this.config.host + '/v1/models', { timeout: 3000 });
            return true;
        } catch {
            return false;
        }
    }
}

module.exports = LMStudioAdapter;
