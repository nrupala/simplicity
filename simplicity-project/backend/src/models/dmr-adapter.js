const axios = require('axios');

class DockerModelRunnerAdapter {
    constructor(config = {}) {
        this.name = 'dmr';
        this.label = 'Docker Model Runner';
        this.defaultPort = 8081;
        this.config = {
            host: config.host || 'http://localhost:' + this.defaultPort,
            model: config.model || 'llama3.2',
            temperature: config.temperature || 0.7,
            maxTokens: config.maxTokens || 2048,
            ...config
        };
    }

    async generate(prompt, options = {}) {
        const response = await axios.post(this.config.host + '/v1/chat/completions', {
            model: this.config.model,
            messages: [{ role: 'user', content: prompt }],
            temperature: options.temperature || this.config.temperature,
            max_tokens: options.maxTokens || this.config.maxTokens,
            stream: false
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
            return response.data.data?.map(m => ({ name: m.id, size: 0 })) || [];
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

module.exports = DockerModelRunnerAdapter;
