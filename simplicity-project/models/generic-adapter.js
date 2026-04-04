const axios = require('axios');

// Generic model adapter for API-based models
class GenericModelAdapter {
    constructor(config = {}) {
        this.config = {
            apiUrl: config.apiUrl || '',
            apiKey: config.apiKey || '',
            model: config.model || 'generic',
            temperature: config.temperature || 0.7,
            maxTokens: config.maxTokens || 2048,
            headers: config.headers || {},
            ...config
        };
    }

    async generate(prompt, options = {}) {
        try {
            const requestBody = {
                prompt: prompt,
                model: options.model || this.config.model,
                temperature: options.temperature || this.config.temperature,
                max_tokens: options.maxTokens || this.config.maxTokens,
                ...options
            };

            const headers = {
                'Content-Type': 'application/json',
                ...this.config.headers
            };

            if (this.config.apiKey) {
                headers['Authorization'] = `Bearer ${this.config.apiKey}`;
            }

            const response = await axios.post(this.config.apiUrl, requestBody, { headers });

            // Handle different response formats
            const data = response.data;
            let text = '';
            let usage = {};

            if (data.choices && data.choices[0]) {
                // OpenAI-style response
                text = data.choices[0].text || data.choices[0].message?.content || '';
                usage = data.usage || {};
            } else if (data.response) {
                // Ollama-style response
                text = data.response;
                usage = {
                    promptTokens: data.prompt_eval_count || 0,
                    completionTokens: data.eval_count || 0,
                    totalTokens: (data.prompt_eval_count || 0) + (data.eval_count || 0)
                };
            } else if (data.generated_text) {
                // Hugging Face style
                text = data.generated_text;
            } else {
                text = JSON.stringify(data);
            }

            return {
                text: text,
                model: this.config.model,
                usage: usage,
                metadata: {
                    status: response.status,
                    headers: response.headers
                }
            };
        } catch (error) {
            console.error('Generic model generation error:', error);
            throw new Error(`Failed to generate with generic model: ${error.message}`);
        }
    }

    async listModels() {
        // Generic adapter doesn't know how to list models
        return [{ name: this.config.model, type: 'generic' }];
    }

    async checkModel(modelName) {
        return modelName === this.config.model;
    }
}

module.exports = GenericModelAdapter;