const os = require('os');
const inferenceEngine = require('./inference');

class MemoryOrchestrator {
    constructor() {
        this.monitoring = false;
        this.monitorInterval = null;
        this.checkIntervalMs = 500;
        this.history = [];
        this.maxHistoryLength = 120;
        this.thresholds = {
            optimal: 0.7,
            comfortable: 0.5,
            throttle: 0.3,
            critical: 0.15
        };
        this.actions = [];
        this.lastAction = null;
        this.lastActionTime = null;
        this.actionCooldownMs = 5000;
        this.listeners = [];
    }

    // Get current system memory info
    getMemoryInfo() {
        const total = os.totalmem();
        const free = os.freemem();
        const used = total - free;
        const freePercent = free / total;
        const usedPercent = used / total;

        return {
            total,
            free,
            used,
            totalGB: total / (1024 ** 3),
            freeGB: free / (1024 ** 3),
            usedGB: used / (1024 ** 3),
            freePercent,
            usedPercent,
            timestamp: Date.now()
        };
    }

    // Get CPU info
    getCpuInfo() {
        const cpus = os.cpus();
        return {
            count: cpus.length,
            model: cpus[0]?.model || 'Unknown',
            speed: cpus[0]?.speed || 0,
            loadAvg: os.loadavg?.() || [0, 0, 0]
        };
    }

    // Determine memory level
    getMemoryLevel(freePercent) {
        if (freePercent > this.thresholds.optimal) return 'optimal';
        if (freePercent > this.thresholds.comfortable) return 'comfortable';
        if (freePercent > this.thresholds.throttle) return 'throttle';
        if (freePercent > this.thresholds.critical) return 'critical';
        return 'emergency';
    }

    // Start monitoring
    start() {
        if (this.monitoring) return;
        this.monitoring = true;
        this.history = [];
        this.actions = [];

        this.monitorInterval = setInterval(() => {
            this.tick();
        }, this.checkIntervalMs);

        console.log('[Memory Orchestrator] Started monitoring (interval: ' + this.checkIntervalMs + 'ms)');
    }

    // Stop monitoring
    stop() {
        if (!this.monitoring) return;
        this.monitoring = false;
        if (this.monitorInterval) {
            clearInterval(this.monitorInterval);
            this.monitorInterval = null;
        }
        console.log('[Memory Orchestrator] Stopped monitoring');
    }

    // Single monitoring tick
    tick() {
        const memory = this.getMemoryInfo();
        const cpu = this.getCpuInfo();
        const level = this.getMemoryLevel(memory.freePercent);

        // Record history
        this.history.push({
            ...memory,
            level,
            cpuLoad: cpu.loadAvg?.[0] || 0
        });

        // Trim history
        if (this.history.length > this.maxHistoryLength) {
            this.history = this.history.slice(-this.maxHistoryLength);
        }

        // Check if action needed
        this.evaluateAndAct(memory, level);

        // Notify listeners
        this.notifyListeners({ memory, cpu, level });
    }

    // Evaluate current state and take action if needed
    evaluateAndAct(memory, level) {
        const now = Date.now();
        if (this.lastActionTime && (now - this.lastActionTime) < this.actionCooldownMs) {
            return; // Cooldown active
        }

        const engineStatus = inferenceEngine.getStatus();
        if (!engineStatus.loaded) return; // No model loaded, nothing to adjust

        const currentConfig = engineStatus.config;
        if (!currentConfig) return;

        let action = null;

        switch (level) {
            case 'emergency':
                action = {
                    type: 'emergency',
                    severity: 'critical',
                    message: 'Memory critically low - reducing context to minimum',
                    actions: [
                        { param: 'n_ctx', value: 512, reason: 'Emergency context reduction' },
                        { param: 'n_gpu_layers', value: 0, reason: 'Offload all layers to CPU' }
                    ]
                };
                break;

            case 'critical':
                action = {
                    type: 'critical',
                    severity: 'high',
                    message: 'Memory critically low - reducing context and offloading layers',
                    actions: [
                        { param: 'n_ctx', value: 1024, reason: 'Critical context reduction' },
                        { param: 'n_gpu_layers', value: 0, reason: 'CPU-only mode to free VRAM' }
                    ]
                };
                break;

            case 'throttle':
                const currentCtx = currentConfig.n_ctx || 4096;
                if (currentCtx > 2048) {
                    action = {
                        type: 'throttle',
                        severity: 'medium',
                        message: 'Memory pressure detected - reducing context window',
                        actions: [
                            { param: 'n_ctx', value: 2048, reason: 'Throttle context reduction' },
                            { param: 'n_gpu_layers', value: Math.max(0, (currentConfig.n_gpu_layers || 20) - 5), reason: 'Reduce GPU layers' }
                        ]
                    };
                }
                break;

            case 'comfortable':
                const currentCtxComfortable = currentConfig.n_ctx || 2048;
                if (currentCtxComfortable < 4096) {
                    action = {
                        type: 'upgrade',
                        severity: 'info',
                        message: 'Memory improved - increasing context window',
                        actions: [
                            { param: 'n_ctx', value: 4096, reason: 'Comfortable context increase' },
                            { param: 'n_gpu_layers', value: Math.min(20, (currentConfig.n_gpu_layers || 10) + 5), reason: 'Increase GPU layers' }
                        ]
                    };
                }
                break;

            case 'optimal':
                const currentCtxOptimal = currentConfig.n_ctx || 4096;
                if (currentCtxOptimal < 8192) {
                    action = {
                        type: 'upgrade',
                        severity: 'info',
                        message: 'Memory optimal - maximizing context window',
                        actions: [
                            { param: 'n_ctx', value: 8192, reason: 'Optimal context increase' },
                            { param: 'n_gpu_layers', value: -1, reason: 'All layers on GPU' }
                        ]
                    };
                }
                break;
        }

        if (action) {
            this.executeAction(action);
        }
    }

    // Execute a memory adjustment action
    async executeAction(action) {
        const now = Date.now();
        this.lastAction = action;
        this.lastActionTime = now;
        this.actions.push({ ...action, executedAt: now });

        console.log(`[Memory Orchestrator] Action: ${action.type} - ${action.message}`);
        for (const a of action.actions) {
            console.log(`  → ${a.param}: ${a.value} (${a.reason})`);
        }

        // Apply to inference engine if model is loaded
        if (inferenceEngine.model) {
            try {
                const modelPath = inferenceEngine.config?.modelPath;
                if (modelPath) {
                    const newConfig = { ...inferenceEngine.config };
                    for (const a of action.actions) {
                        newConfig[a.param] = a.value;
                    }
                    await inferenceEngine.loadModel(modelPath, {
                        ...newConfig,
                        modelId: inferenceEngine.loadedModelId
                    });
                    console.log(`[Memory Orchestrator] Model reloaded with new config`);
                }
            } catch (error) {
                console.error(`[Memory Orchestrator] Failed to apply action:`, error.message);
            }
        }

        // Notify listeners
        this.notifyListeners({ action });
    }

    // Register a listener for memory events
    on(listener) {
        this.listeners.push(listener);
        return () => {
            this.listeners = this.listeners.filter(l => l !== listener);
        };
    }

    // Notify all listeners
    notifyListeners(data) {
        for (const listener of this.listeners) {
            try {
                listener(data);
            } catch (error) {
                console.error('[Memory Orchestrator] Listener error:', error.message);
            }
        }
    }

    // Get monitoring status
    getStatus() {
        const memory = this.getMemoryInfo();
        const cpu = this.getCpuInfo();
        const level = this.getMemoryLevel(memory.freePercent);
        const engineStatus = inferenceEngine.getStatus();

        // Calculate trends
        let trend = 'stable';
        if (this.history.length >= 10) {
            const recent = this.history.slice(-10);
            const old = this.history.slice(-20, -10);
            if (recent.length > 0 && old.length > 0) {
                const recentAvg = recent.reduce((s, h) => s + h.freePercent, 0) / recent.length;
                const oldAvg = old.reduce((s, h) => s + h.freePercent, 0) / old.length;
                if (recentAvg < oldAvg - 0.05) trend = 'declining';
                else if (recentAvg > oldAvg + 0.05) trend = 'improving';
            }
        }

        return {
            monitoring: this.monitoring,
            memory,
            cpu,
            level,
            trend,
            engine: engineStatus,
            thresholds: this.thresholds,
            actions: this.actions.slice(-10),
            lastAction: this.lastAction,
            historyLength: this.history.length
        };
    }

    // Update thresholds
    setThresholds(thresholds) {
        Object.assign(this.thresholds, thresholds);
    }

    // Update check interval
    setInterval(ms) {
        this.checkIntervalMs = ms;
        if (this.monitoring) {
            this.stop();
            this.start();
        }
    }
}

const memoryOrchestrator = new MemoryOrchestrator();
module.exports = memoryOrchestrator;
