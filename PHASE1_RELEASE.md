# SIMPLICITY Engine - Phase 1 Release Notes

**Version:** 1.0.0-engine  
**Branch:** simplicity-engine  
**Date:** 2026-04-05  
**Status:** ✅ MVP Ready

---

## Overview

SIMPLICITY Engine is a sovereign AI inference platform that runs entirely locally. Phase 1 delivers a complete native inference engine with zero external dependencies, replacing the need for Ollama, LM Studio, or any other wrapper.

---

## What's New

### 🔥 Native Inference Engine
- **Direct GGUF Loading**: Load GGUF models directly via `node-llama-cpp`
- **Zero External Dependencies**: No Ollama, no LM Studio server needed
- **Auto Memory Management**: Dynamically adjusts context size, GPU layers, and threads based on available RAM
- **5 Memory Levels**: Optimal → Comfortable → Throttle → Critical → Emergency

### 📂 Model Discovery
- **Automatic Scanning**: Finds models in:
  - `~/.lmstudio/hub/models/` (LM Studio folder)
  - `~/.aitk/models/` (AITK folder)
  - `~/.ollama/models/` (Ollama folder)
- **HuggingFace Download**: Download any GGUF model directly
- **Model Management**: Load, unload, delete models from UI

### 🧠 Inference Features
- **Streaming Responses**: Token-by-token generation
- **Full Parameter Control**: temperature, top_k, top_p, min_p, repeat_penalty, n_ctx, n_gpu_layers, n_threads
- **Context Auto-Recovery**: Automatically resets when context window fills
- **Chat Session Management**: Maintains conversation history with configurable context length

### 🎨 UI Updates
- **Models Tab**: View, load, unload, and delete models
- **Engine Status Bar**: Real-time memory usage and model status
- **Markdown Rendering**: Structured responses with headers, code blocks, lists, tables
- **Provider Fallback**: Native engine primary, Ollama/LM Studio as fallback

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   SIMPLICITY UI                      │
│  Chat | Models | Knowledge | Documents | Settings    │
├─────────────────────────────────────────────────────┤
│                 API Layer (Express)                  │
│  /api/query  /api/models  /api/documents  /health   │
├─────────────────────────────────────────────────────┤
│              Inference Engine (Native)               │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ Model Loader│  │   Inference  │  │  Embedding │ │
│  │  (GGUF)     │  │   (generate) │  │  (future)  │ │
│  └─────────────┘  └──────────────┘  └────────────┘ │
├─────────────────────────────────────────────────────┤
│              Fallback Providers                      │
│  Ollama | LM Studio | OpenCode | Docker Model Runner │
└─────────────────────────────────────────────────────┘
```

---

## Quick Start

### Prerequisites
- Node.js 18+
- GGUF model files (from HuggingFace, LM Studio, etc.)

### Installation
```bash
cd simplicity-project/backend
npm install
npm start
```

### Loading a Model
1. Open http://localhost:3001
2. Click **Models** tab
3. Click **Load** next to any discovered model
4. Start chatting

### API Usage
```bash
# List available models
curl http://localhost:3001/api/models/available

# Load a model
curl -X POST http://localhost:3001/api/models/load \
  -H "Content-Type: application/json" \
  -d '{"modelId":"ext_abc123","n_ctx":2048,"n_gpu_layers":0}'

# Query the engine
curl -X POST http://localhost:3001/api/query \
  -H "Content-Type: application/json" \
  -d '{"query":"Hello!","provider":"native"}'
```

---

## File Structure

```
simplicity-project/backend/
├── src/
│   ├── engines/
│   │   ├── inference/
│   │   │   ├── model-loader.js    # Model discovery, download, caching
│   │   │   └── inference.js       # Core inference engine
│   │   ├── gan-rag.js             # 5-candidate GAN-RAG discrimination
│   │   ├── documents.js           # Document RAG (upload, chunk, search)
│   │   ├── correlation.js         # User knowledge correlation
│   │   ├── knowledge-graph.js     # SQLite knowledge graph
│   │   ├── personification.js     # Response personification
│   │   └── sovereignty.js         # GPG encryption (stub)
│   ├── models/
│   │   ├── native-adapter.js      # Native inference adapter
│   │   ├── ollama-adapter.js      # Ollama fallback
│   │   ├── lmstudio-adapter.js    # LM Studio fallback
│   │   ├── opencode-adapter.js    # OpenCode fallback
│   │   ├── dmr-adapter.js         # Docker Model Runner fallback
│   │   └── registry.js            # Unified provider registry
│   ├── routes/
│   │   ├── query.js               # Query endpoints (streaming + non-streaming)
│   │   ├── models.js              # Model management API
│   │   ├── documents.js           # Document upload/search
│   │   ├── knowledge.js           # Knowledge graph API
│   │   ├── sovereignty.js         # Sovereignty API
│   │   └── portability.js         # Export/import API
│   ├── public/
│   │   └── index.html             # Vanilla JS frontend
│   └── server.js                  # Express server
├── models/                        # Downloaded GGUF models
└── data/
    └── knowledge.db               # SQLite knowledge graph
```

---

## Configuration

The engine auto-configures based on system memory:

| Memory Level | Free RAM | n_ctx | n_gpu_layers | n_threads |
|-------------|----------|-------|--------------|-----------|
| Optimal | > 70% | 8192 | -1 (all) | CPU count |
| Comfortable | 50-70% | 4096 | 20 | CPU-1 |
| Throttle | 30-50% | 2048 | 10 | CPU-2 |
| Critical | 15-30% | 1024 | 0 (CPU) | CPU-3 |
| Emergency | < 15% | 512 | 0 | 2 |

All parameters are overridable via API or UI.

---

## Known Limitations

- Embedding support requires models with embedding capability
- GPU acceleration depends on system Vulkan/CUDA drivers
- Large models (>8GB) may require significant RAM

---

## Next Phases

| Phase | Feature | Status |
|-------|---------|--------|
| Phase 2 | Memory Orchestrator + Real-time Dashboard | Planned |
| Phase 3 | Three-Tier Caching (VRAM → RAM → Disk) | Planned |
| Phase 4 | Dynamic Model Chunking + Predictive Loading | Planned |
| Phase 5 | Embedding Engine + Semantic RAG | Planned |
| Phase 6 | Enhanced GAN-RAG with Embedding Scoring | Planned |
| Phase 7-8 | Testing + Optimization | Planned |

---

## Credits

Built with:
- [node-llama-cpp](https://github.com/withcatai/node-llama-cpp) - Native llama.cpp bindings
- [@huggingface/hub](https://github.com/huggingface/huggingface.js) - Model downloads
- Express.js - HTTP server
- SQLite3 - Knowledge graph storage

---

**SIMPLICITY** — Your Sovereign AI Agent. All data stays on your machine.
