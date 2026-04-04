const ollama = require('ollama');

// Ollama model adapter for SIMPLICITY
class OllamaAdapter {
    constructor(config = {}) {
        this.config = {
            model: config.model || 'llama3.2',
            temperature: config.temperature || 0.7,
            maxTokens: config.maxTokens || 2048,
            ...config
        };
    }

    async generate(prompt, options = {}) {
        try {
            const response = await ollama.generate({
                model: this.config.model,
                prompt: prompt,
                options: {
                    temperature: options.temperature || this.config.temperature,
                    num_predict: options.maxTokens || this.config.maxTokens,
                    ...options
                }
            });

            return {
                text: response.response,
                model: this.config.model,
                usage: {
                    promptTokens: response.prompt_eval_count || 0,
                    completionTokens: response.eval_count || 0,
                    totalTokens: (response.prompt_eval_count || 0) + (response.eval_count || 0)
                },
                metadata: {
                    done: response.done,
                    context: response.context
                }
            };
        } catch (error) {
            console.error('Ollama generation error:', error);
            throw new Error(`Failed to generate with Ollama: ${error.message}`);
        }
    }

    async listModels() {
        try {
            const response = await ollama.list();
            return response.models.map(model => ({
                name: model.name,
                size: model.size,
                modified: model.modified_at,
                digest: model.digest
            }));
        } catch (error) {
            console.error('Failed to list Ollama models:', error);
            return [];
        }
    }

    async pullModel(modelName) {
        try {
            await ollama.pull({ model: modelName });
            return { success: true, model: modelName };
        } catch (error) {
            console.error(`Failed to pull model ${modelName}:`, error);
            throw error;
        }
    }

    async checkModel(modelName) {
        try {
            const models = await this.listModels();
            return models.some(model => model.name === modelName);
        } catch (error) {
            return false;
        }
    }
}

module.exports = OllamaAdapter;