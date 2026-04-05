const { Ollama } = require('ollama');
const axios = require('axios');

class OllamaAdapter {
    constructor(config = {}) {
        this.name = 'ollama';
        this.label = 'Ollama';
        this.defaultPort = 11434;
        this.config = {
            host: config.host || 'http://localhost:' + this.defaultPort,
            model: config.model || 'llama3.2',
            temperature: config.temperature || 0.7,
            maxTokens: config.maxTokens || 2048,
            ...config
        };
        this.client = new Ollama({ host: this.config.host });
    }

    async generate(prompt, options = {}) {
        const response = await this.client.generate({
            model: this.config.model,
            prompt: prompt,
            options: {
                temperature: options.temperature || this.config.temperature,
                num_predict: options.maxTokens || this.config.maxTokens
            }
        });
        return {
            text: response.response,
            model: this.config.model,
            provider: this.name,
            usage: {
                promptTokens: response.prompt_eval_count || 0,
                completionTokens: response.eval_count || 0,
                totalTokens: (response.prompt_eval_count || 0) + (response.eval_count || 0)
            }
        };
    }

    async listModels() {
        try {
            const response = await axios.get(this.config.host + '/api/tags', { timeout: 5000 });
            return (response.data.models || []).map(m => ({ name: m.name, size: m.size }));
        } catch {
            return [];
        }
    }

    async isAvailable() {
        try {
            await axios.get(this.config.host, { timeout: 3000 });
            return true;
        } catch {
            return false;
        }
    }
}

module.exports = OllamaAdapter;
