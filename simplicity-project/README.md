# SIMPLICITY - Your Sovereign AI Agent

SIMPLICITY is a completely sovereign AI agent that runs entirely on your local machine. It maintains user sovereignty by never sending data to the cloud, using local AI models, and providing full data portability.

## Features

### Seven Core Pillars
1. **Correlation Engine** - Analyzes queries and correlates with user knowledge
2. **Knowledge Graph** - SQLite-based personal knowledge storage and retrieval
3. **GAN-RAG** - Generates responses using local LLMs with web knowledge retrieval
4. **Personification** - Applies personality profiles to AI responses
5. **Sovereignty** - GPG encryption and zero-knowledge proofs
6. **Model Layer** - Adapter system for multiple local AI models
7. **Interface Layer** - Web interface for interaction and management

### Key Benefits
- **Complete Privacy** - All data stays on your machine
- **Data Sovereignty** - Full control over your knowledge and encryption keys
- **Local AI** - Uses Ollama for local LLM execution
- **Knowledge Building** - Learns from your questions and builds a personal graph
- **Portability** - Export/import your knowledge and settings anytime

## Quick Start

### Prerequisites
- Node.js 16+
- Ollama (for full AI responses)

### Installation

1. **Clone and setup:**
```bash
cd simplicity-project/backend
npm install
```

2. **Start the backend:**
```bash
cd simplicity-project/backend/src
node server.js
```

3. **Open the interface:**
Open `simplicity-web/index.html` in your browser

### Enable Full AI Responses

1. **Install Ollama:**
```bash
# Download from https://ollama.ai
# Install and start Ollama service
```

2. **Pull the model:**
```bash
ollama pull llama3.2
```

3. **Restart SIMPLICITY backend** - it will automatically detect Ollama

## API Endpoints

### Query
```http
POST /api/query
Content-Type: application/json

{
  "query": "What is machine learning?",
  "userId": "default"
}
```

### Knowledge Management
```http
POST /api/knowledge
GET /api/knowledge?userId=default&q=search_term
DELETE /api/knowledge/:userId
```

### Data Sovereignty
```http
POST /api/sovereignty/generate-key
POST /api/sovereignty/sign
POST /api/sovereignty/verify
POST /api/sovereignty/encrypt
POST /api/sovereignty/decrypt
```

### Portability
```http
GET /api/portability/export/:userId
POST /api/portability/import/:userId
POST /api/portability/export-file/:userId
POST /api/portability/import-file/:userId
```

## Architecture

```
Frontend (HTML/JS)
    ↓
Express Backend
    ↓
├── Correlation Engine
├── Knowledge Graph (SQLite)
├── GAN-RAG Engine
├── Personification Engine
├── Sovereignty Engine (GPG)
├── Model Adapters (Ollama)
└── Portability Manager
```

## Data Storage

- **Knowledge Graph**: SQLite database (`backend/data/knowledge.db`)
- **Encryption**: GPG keys stored locally
- **Backups**: JSON exports in `backups/` directory

## Security

- All data encrypted with user-controlled GPG keys
- No external API calls (except optional web knowledge retrieval)
- Local SQLite storage with user isolation
- Zero-knowledge architecture

## Development

### Project Structure
```
simplicity-project/
├── backend/
│   ├── src/
│   │   ├── server.js
│   │   ├── routes/
│   │   ├── engines/
│   │   ├── models/
│   │   └── portability/
│   ├── data/
│   └── package.json
└── simplicity-web/
    └── index.html
```

### Adding New Engines

1. Create engine in `backend/src/engines/`
2. Export functions from engine module
3. Import and use in appropriate route
4. Update API documentation

## Troubleshooting

### Backend won't start
- Check Node.js version: `node --version`
- Ensure dependencies: `npm install`
- Check database permissions

### No AI responses
- Verify Ollama is running: `ollama list`
- Check model availability: `ollama pull llama3.2`
- Restart backend after starting Ollama

### CORS errors
- Backend runs on port 3001
- Frontend served from file:// protocol
- Modern browsers allow this for local development

## Contributing

SIMPLICITY is designed for sovereignty and local operation. Contributions should maintain:
- Local-first architecture
- User data control
- No external dependencies
- Clear documentation

## License

This project emphasizes user sovereignty and data ownership. Use responsibly.