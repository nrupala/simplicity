const os = require('os');
const { getSystemMemory } = require('./model-loader');

class InferenceEngine {
    constructor() {
        this.llama = null;
        this.model = null;
        this.context = null;
        this.session = null;
        this.loadedModelId = null;
        this.config = null;
        this.LlamaChatSession = null;
        this.memoryThresholds = {
            optimal: 0.7,
            comfortable: 0.5,
            throttle: 0.3,
            critical: 0.15
        };
    }

    async initialize() {
        if (this.llama) return this.llama;
        const { getLlama, LlamaChatSession } = await import('node-llama-cpp');
        this.LlamaChatSession = LlamaChatSession;
        this.llama = await getLlama();
        return this.llama;
    }

    async loadModel(modelPath, options = {}) {
        await this.initialize();

        if (this.model) {
            await this.unloadModel();
        }

        const memory = getSystemMemory();
        const memLevel = this.getMemoryLevel(memory.usagePercent / 100);
        const autoConfig = this.computeAutoConfig(memory, memLevel, options);

        this.config = {
            modelPath,
            ...autoConfig,
            ...options
        };

        console.log(`Loading model: ${modelPath}`);
        console.log(`Config: n_ctx=${this.config.n_ctx}, n_gpu_layers=${this.config.n_gpu_layers}, threads=${this.config.n_threads}`);

        this.model = await this.llama.loadModel({
            modelPath: this.config.modelPath,
            n_ctx: this.config.n_ctx,
            n_gpu_layers: this.config.n_gpu_layers,
            n_threads: this.config.n_threads,
            n_batch: this.config.n_batch,
            n_ubatch: this.config.n_ubatch,
            flashAttention: this.config.flashAttention ?? false,
            mmap: this.config.mmap ?? true,
            mlock: this.config.mlock ?? false,
            tensorSplit: this.config.tensorSplit,
            mainGpu: this.config.mainGpu,
            ropeFreqBase: this.config.ropeFreqBase,
            ropeFreqScale: this.config.ropeFreqScale,
            ropeScalingType: this.config.ropeScalingType
        });

        this.loadedModelId = options.modelId || modelPath;

        return {
            success: true,
            modelId: this.loadedModelId,
            config: {
                n_ctx: this.config.n_ctx,
                n_gpu_layers: this.config.n_gpu_layers,
                n_threads: this.config.n_threads,
                memoryLevel: memLevel
            }
        };
    }

    async unloadModel() {
        if (this.session) { this.session.dispose(); this.session = null; }
        if (this.context) { this.context.dispose(); this.context = null; }
        if (this.model) { this.model.dispose(); this.model = null; }
        this.loadedModelId = null;
        this.config = null;
    }

    getMemoryLevel(freePercent) {
        if (freePercent > this.memoryThresholds.optimal) return 'optimal';
        if (freePercent > this.memoryThresholds.comfortable) return 'comfortable';
        if (freePercent > this.memoryThresholds.throttle) return 'throttle';
        if (freePercent > this.memoryThresholds.critical) return 'critical';
        return 'emergency';
    }

    computeAutoConfig(memory, memLevel, userOptions = {}) {
        const cpuCount = os.cpus().length;
        let n_ctx, n_gpu_layers, n_threads, n_batch, n_ubatch;

        switch (memLevel) {
            case 'optimal':
                n_ctx = userOptions.n_ctx || 8192;
                n_gpu_layers = userOptions.n_gpu_layers ?? -1;
                n_threads = userOptions.n_threads || cpuCount;
                n_batch = userOptions.n_batch || 2048;
                n_ubatch = userOptions.n_ubatch || 512;
                break;
            case 'comfortable':
                n_ctx = userOptions.n_ctx || 4096;
                n_gpu_layers = userOptions.n_gpu_layers ?? 20;
                n_threads = userOptions.n_threads || Math.max(2, cpuCount - 1);
                n_batch = userOptions.n_batch || 1024;
                n_ubatch = userOptions.n_ubatch || 256;
                break;
            case 'throttle':
                n_ctx = userOptions.n_ctx || 2048;
                n_gpu_layers = userOptions.n_gpu_layers ?? 10;
                n_threads = userOptions.n_threads || Math.max(2, cpuCount - 2);
                n_batch = userOptions.n_batch || 512;
                n_ubatch = userOptions.n_ubatch || 128;
                break;
            case 'critical':
                n_ctx = userOptions.n_ctx || 1024;
                n_gpu_layers = userOptions.n_gpu_layers ?? 0;
                n_threads = userOptions.n_threads || Math.max(2, cpuCount - 3);
                n_batch = userOptions.n_batch || 256;
                n_ubatch = userOptions.n_ubatch || 64;
                break;
            default:
                n_ctx = userOptions.n_ctx || 512;
                n_gpu_layers = 0;
                n_threads = userOptions.n_threads || 2;
                n_batch = userOptions.n_batch || 128;
                n_ubatch = userOptions.n_ubatch || 32;
        }

        return { n_ctx, n_gpu_layers, n_threads, n_batch, n_ubatch };
    }

    async createSession(options = {}) {
        if (!this.model) throw new Error('No model loaded');

        const systemPrompt = options.systemPrompt ||
            'You are SIMPLICITY, a sovereign AI agent that runs locally on the user\'s machine. You maintain complete user sovereignty - no data is sent to the cloud. Be helpful, accurate, and concise.';

        this.context = await this.model.createContext();

        this.session = new this.LlamaChatSession({
            contextSequence: this.context.getSequence(),
            autoDisposeSequence: true,
            systemPrompt,
            chatWrapper: options.chatWrapper
        });

        return this.session;
    }

    async generate(prompt, options = {}) {
        if (!this.model) throw new Error('No model loaded. Load a model first.');
        if (!this.session) await this.createSession(options);

        const samplingOptions = {
            temperature: options.temperature ?? this.config?.temperature ?? 0.7,
            topK: options.topK ?? 40,
            topP: options.topP ?? 0.9,
            minP: options.minP ?? 0.05,
            repeatPenalty: options.repeatPenalty ?? 1.1,
            repeatPenaltyLastN: options.repeatPenaltyLastN ?? 64,
            ...options.sampling
        };

        const maxTokens = options.maxTokens ?? this.config?.maxTokens ?? 2048;

        try {
            const response = await this.session.prompt(prompt, { maxTokens, ...samplingOptions });
            return {
                text: response,
                model: this.loadedModelId,
                provider: 'native',
                usage: { contextSize: this.config?.n_ctx, gpuLayers: this.config?.n_gpu_layers }
            };
        } catch (error) {
            if (error.message?.includes('context full') || error.message?.includes('exceeds')) {
                await this.createSession(options);
                return this.generate(prompt, options);
            }
            throw error;
        }
    }

    async *generateStream(prompt, options = {}) {
        if (!this.model) throw new Error('No model loaded. Load a model first.');
        if (!this.session) await this.createSession(options);

        const samplingOptions = {
            temperature: options.temperature ?? this.config?.temperature ?? 0.7,
            topK: options.topK ?? 40,
            topP: options.topP ?? 0.9,
            minP: options.minP ?? 0.05,
            repeatPenalty: options.repeatPenalty ?? 1.1,
            repeatPenaltyLastN: options.repeatPenaltyLastN ?? 64,
            ...options.sampling
        };

        const maxTokens = options.maxTokens ?? this.config?.maxTokens ?? 2048;

        try {
            const response = await this.session.prompt(prompt, { maxTokens, ...samplingOptions });
            let buffer = '';
            for (const char of response) {
                buffer += char;
                if (buffer.length >= 4 || char === '\n' || char === ' ') {
                    yield buffer;
                    buffer = '';
                }
            }
            if (buffer) yield buffer;
        } catch (error) {
            if (error.message?.includes('context full') || error.message?.includes('exceeds')) {
                await this.createSession(options);
                yield* this.generateStream(prompt, options);
            } else {
                throw error;
            }
        }
    }

    getStatus() {
        const memory = getSystemMemory();
        return {
            loaded: !!this.model,
            modelId: this.loadedModelId,
            config: this.config ? {
                n_ctx: this.config.n_ctx,
                n_gpu_layers: this.config.n_gpu_layers,
                n_threads: this.config.n_threads,
                temperature: this.config.temperature
            } : null,
            memory,
            memoryLevel: this.getMemoryLevel(memory.free / memory.total)
        };
    }
}

const inferenceEngine = new InferenceEngine();
module.exports = inferenceEngine;
