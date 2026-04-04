# SIMPLICITY: Your Sovereign AI Agent

> **"Your Intelligence. Your Rules. Your Portability. Your Sovereignty."**

SIMPLICITY is a Perplexity-like AI answer engine that runs entirely locally on your machine. Unlike cloud-based AI services, SIMPLICITY gives you complete control over your data, knowledge, and AI interactions.

## 🚀 Features

- **User Sovereignty**: Your data stays on your device, encrypted with GPG
- **Knowledge Graph**: Build and maintain your personal knowledge base
- **Local AI**: Uses Ollama for running LLMs locally
- **GAN-RAG Coupling**: Advanced response generation combining user knowledge with web retrieval
- **Personification**: AI responses tailored to your preferred interaction style
- **Full Portability**: Export/import your knowledge and settings
- **PWA Interface**: Modern web app that works offline

## 🏗️ Architecture

SIMPLICITY consists of seven core pillars:

1. **User Sovereignty** - GPG encryption, zero-knowledge architecture
2. **Knowledge Graph** - SQLite-based graph database for user knowledge
3. **Correlation Engine** - Analyzes queries and correlates with user knowledge
4. **GAN-RAG Engine** - Generates responses using local LLMs and web retrieval
5. **Personification Engine** - Styles responses based on user preferences
6. **Model Layer** - Pluggable adapters for different LLM providers
7. **Interface Layer** - PWA for seamless user interaction

## 📋 Prerequisites

- **Node.js 18+** - Backend and build tools
- **Ollama** - Local LLM runtime
- **Git** - For cloning repositories

### Install Ollama

```bash
# Windows
# Download from: https://ollama.ai/download

# After installation, pull a model
ollama pull llama3.2
```

## 🛠️ Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/nrupala/simplicity.git
   cd simplicity
   ```

2. **Install dependencies**
   ```bash
   npm run setup
   ```

   This will install dependencies for both backend and frontend.

3. **Start the backend**
   ```bash
   npm run start:backend
   ```

   The backend will start on http://localhost:3001

4. **Start the frontend** (in a new terminal)
   ```bash
   npm run start:frontend
   ```

   The PWA will be available at http://localhost:5173

## 🎯 Usage

1. **Open the PWA** in your browser at http://localhost:5173

2. **Ask questions** in the query interface

3. **Build knowledge** - SIMPLICITY learns from your interactions

4. **Export data** - Use the Sovereignty tab to export your knowledge

5. **Customize personality** - Choose from curious, empathetic, professional, or creative profiles

## 🔧 Configuration

### Environment Variables

Create a `.env` file in `simplicity-project/backend/`:

```env
PORT=3001
OLLAMA_MODEL=llama3.2
GPG_KEY_ID=your-gpg-key-id
JWT_SECRET=your-jwt-secret
```

### Model Configuration

Edit `simplicity-project/models/registry.js` to configure different LLM adapters.

## 📁 Project Structure

```
simplicity/
├── simplicity-web/          # React PWA frontend
├── simplicity-project/      # Backend services
│   ├── backend/            # Express server
│   │   ├── src/
│   │   │   ├── engines/    # Core AI engines
│   │   │   ├── routes/     # API routes
│   │   │   └── server.js   # Main server
│   │   └── package.json
│   ├── models/             # LLM adapters
│   └── portability/        # Data export/import
├── package.json            # Root scripts
└── README.md
```

## 🧪 Testing

```bash
# Run backend tests
npm test

# Manual testing
curl -X POST http://localhost:3001/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "What is AI?", "userId": "test-user"}'
```

## 🔒 Security & Privacy

- **Local Execution**: All processing happens on your device
- **GPG Encryption**: Knowledge is encrypted with your GPG keys
- **No Telemetry**: No data collection or tracking
- **Zero Knowledge**: Even developers cannot access your data

## 🚀 Advanced Usage

### Custom Models

Add new model adapters in `simplicity-project/models/`:

```javascript
// Example: Add OpenAI adapter
const openaiAdapter = new GenericModelAdapter({
  apiUrl: 'https://api.openai.com/v1/completions',
  apiKey: process.env.OPENAI_API_KEY,
  model: 'gpt-3.5-turbo'
});
modelRegistry.registerAdapter('openai', openaiAdapter);
```

### Backup & Restore

```bash
# Export data
curl http://localhost:3001/api/portability/export/default-user > backup.json

# Import data
curl -X POST http://localhost:3001/api/portability/import/default-user \
  -H "Content-Type: application/json" \
  -d @backup.json
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

MIT License - see LICENSE file for details.

## 🙏 Acknowledgments

- Ollama for local LLM execution
- React for the frontend framework
- Express.js for the backend API
- OpenPGP for encryption

---

**SIMPLICITY**: Because your intelligence should be yours, not someone else's product.
| GPG Key Ownership | ❌ | ❌ | ❌ | ✅ Native Support |
| Unique User Experience | ❌ | ❌ | ❌ | ✅ Immutable Hash |
| GAN-RAG Correlation | ❌ | ❌ | ❌ | ✅ Self-Evolving |
| Cryptographic Sovereignty | ❌ | ❌ | ❌ | ✅ Zero-Knowledge |
| User-Owned Data | ❌ | ❌ | ❌ | ✅ By Design |

---

## Core Architecture

### The Seven Pillars of Simplicity

```
┌─────────────────────────────────────────────────────────────────────┐
│                     SIMPLICITY ARCHITECTURE                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🏛️ USER SOVEREIGNTY ENGINE                                   │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • GPG Key Management (User Controls Keys)                     │  │
│  │  • Certificate Authority (X.509, PGP, WebAuthn)                │  │
│  │  • Zero-Knowledge Proofs                                      │  │
│  │  • Immutable Consent Ledger                                    │  │
│  │  • Homomorphic Encryption (Compute on Encrypted)               │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🧠 USER KNOWLEDGE GRAPH (The Unique Asset)                  │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • Entities & Relationships (Owned by User)                    │  │
│  │  • Adaptive Learning (Grows with User)                        │  │
│  │  • Unique Experience Hash (Immutable Signature)                 │  │
│  │  • Intelligence Fingerprint (Cannot Be Copied)                │  │
│  │  • Personal Evolution Tracking                                 │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🔗 CORRELATION ENGINE (Creating Uniqueness)                │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • User Knowledge ↔ Model Capability Correlation             │  │
│  │  • Dynamic Weighting (Who Knows More?)                       │  │
│  │  • Topic/Complexity/Style Matching                           │  │
│  │  • Approach Selection (Guided/Collaborative/Educational)     │  │
│  │  • Confidence Calibration                                    │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🧠 GAN-RAG COUPLING (Self-Improving Intelligence)          │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • Hybrid Retrieval (Vector + Keyword + Knowledge Graph)     │  │
│  │  • Generator Network (Multiple Candidates)                    │  │
│  │  • Discriminator Network (Accuracy/Relevance/Personal)        │  │
│  │  • Adversarial Training (Generator vs Discriminator)          │  │
│  │  • Continuous Learning (Implicit + Explicit Feedback)         │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🎭 PERSONIFICATION ENGINE (The Human Touch)                 │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • Emotional Intelligence (Curious, Empathetic, Humble)       │  │
│  │  • Personality Profiles (Warm, Professional, Creative)       │  │
│  │  • Expression Generator (Natural Language)                  │  │
│  │  • Tone/Pacing/Depth Adjustment                              │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🤖 MODEL LAYER (Pluggable Intelligence)                    │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • Proprietary Models (GPT-5, Claude 4, Gemini Ultra)        │  │
│  │  • Open Source Models (Llama 4, Mistral, DeepSeek)           │  │
│  │  • Local Models (Ollama, LM Studio)                         │  │
│  │  • Model Registry (100+ Models, Tuesday Updates)              │  │
│  │  • User Model Preferences (Per Domain)                        │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🎛️ INTERFACE LAYER (Accessibility for All)                  │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • Casual Mode (Presets, One-Click)                          │  │
│  │  • Standard Mode (Full Features)                            │  │
│  │  • Expert Mode (Raw Config, JSON)                            │  │
│  │  • Architect Mode (Enterprise, API Access)                   │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  🔄 PORTABILITY ENGINE (Freedom to Leave)                    │  │
│  │  ─────────────────────────────────────────────────────────   │  │
│  │  • Export (JSON, GraphQL, RDF, JSON-LD)                     │  │
│  │  • Import (From Any System)                                │  │
│  │  • Migrate (To Any Model, Any Provider)                     │  │
│  │  • Backup (Encrypted, User's Keys, Offline)                 │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## The Unique Experience Engine

### How Simplicity Creates a Unique Experience

```
┌─────────────────────────────────────────────────────────────────────┐
│                 UNIQUE EXPERIENCE GENERATION                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  👤 USER INPUT                                                      │
│  └── Query + Context + Preferences                                   │
│                                                                     │
│  ↓                                                                  │
│                                                                     │
│  🔐 CRYPTOGRAPHIC VERIFICATION                                     │
│  ├── GPG Signature (Verify User Identity)                          │
│  ├── Consent Check (Data Usage Permission)                          │
│  └── Zero-Knowledge Auth (Privacy-Preserving)                       │
│                                                                     │
│  ↓                                                                  │
│                                                                     │
│  🧠 BUILD UNIQUE INTELLIGENCE                                      │
│  ├── Load User Knowledge Graph (Personal Context)                    │
│  ├── Correlate User ↔ Model (Create Unified State)                  │
│  └── Generate Unique Hash (Immutable Experience ID)                 │
│      ┌──────────────────────────────────────────────┐              │
│      │  NOBODY ELSE IN THE WORLD GETS THIS EXACT    │              │
│      │  RESPONSE BECAUSE YOUR KNOWLEDGE + YOUR      │              │
│      │  PREFERENCES + YOUR PATTERNS = UNIQUE        │              │
│      └──────────────────────────────────────────────┘              │
│                                                                     │
│  ↓                                                                  │
│                                                                     │
│  🧠 GAN-RAG ENGINE                                                 │
│  ├── Retrieve Knowledge (Hybrid + Personal)                         │
│  ├── Generate Candidates (Multiple Models)                          │
│  ├── Discriminate (Accuracy/Relevance/Personal)                     │
│  └── Score & Rank (Best Response)                                   │
│                                                                     │
│  ↓                                                                  │
│                                                                     │
│  🎭 PERSONIFICATION                                                │
│  ├── Inject Emotion (Based on Correlation)                          │
│  ├── Adjust Tone (User's Preference)                               │
│  └── Format Response (User's Style)                                │
│                                                                     │
│  ↓                                                                  │
│                                                                     │
│  📤 UNIQUE OUTPUT                                                  │
│  ├── Unique Response (Nobody Else Gets This)                        │
│  ├── Citations (With Confidence Scores)                             │
│  ├── Emotional Tone (Natural, Personal)                            │
│  └── GPG-Signed Response (Authenticity Guaranteed)                  │
│                                                                     │
│  ↓                                                                  │
│                                                                     │
│  📈 CONTINUOUS LEARNING                                             │
│  ├── Collect Feedback (Implicit + Explicit)                         │
│  ├── Update Knowledge Graph (User's Intelligence Grows)             │
│  └── Evolve Model (Weekly Improvements)                             │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Cryptographic Sovereignty

### GPG Key Management

```java
// User's GPG Key Ring (User Controls Everything)
UserSovereigntyEngine {
    
    // Master Key - Root of Trust
    MasterGPGKey masterKey;
    
    // Sub-Keys for Different Purposes
    List<GPGSubKey> subKeys = {
        encryptionKey,    // Encrypt user data
        signingKey,       // Sign everything
        authenticationKey // Authenticate
    };
    
    // Certificate Management
    CertificateManager certManager;
    
    // Zero-Knowledge Verification
    ZeroKnowledgeProof zkp;
}
```

### Consent Ledger (Immutable)

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CONSENT LEDGER (Immutable)                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────┬─────────────┬─────────────┬─────────────────────┐  │
│  │ Timestamp   │ ConsentType │   Status    │   Action            │  │
│  ├─────────────┼─────────────┼─────────────┼─────────────────────┤  │
│  │ 2024-01-15 │ PERSONALIZE │   GRANTED   │ Enable learning     │  │
│  │ 2024-02-20 │ ANALYTICS  │   GRANTED   │ Enable aggregation  │  │
│  │ 2024-03-01 │ MODEL_TRAIN│   DENIED    │ Reject training use  │  │
│  │ 2024-03-10 │ SHARING    │   REVOKED   │ Revoked by user     │  │
│  └─────────────┴─────────────┴─────────────┴─────────────────────┘  │
│                                                                     │
│  📜 Each entry is GPG-signed and timestamped                       │
│  🔒 Changes are permanent and traceable                            │
│  🚫 Instant revocation = immediate data deletion                    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Encryption Layers

| Layer | Algorithm | Key Owner | Purpose |
|-------|-----------|-----------|---------|
| At Rest | AES-256-GCM | User | Protect stored data |
| In Transit | TLS 1.3+ | Mutual | Secure communication |
| Compute | Homomorphic | User | Compute on encrypted |
| Zero-Knowledge | ZK-SNARKs | User | Prove without revealing |

---

## Portability: Freedom to Leave

### Export Formats

```json
{
  "version": "1.0",
  "exportedAt": "2024-03-15T10:30:00Z",
  "format": "simplicity-portable",
  "checksum": "sha256:abc123...",
  
  "userKnowledgeGraph": {
    "entities": [...],
    "relationships": [...],
    "interestVector": {...},
    "behaviorPatterns": [...]
  },
  
  "preferences": {
    "personality": {...},
    "emotions": {...},
    "interface": {...}
  },
  
  "learningHistory": {
    "interactions": [...],
    "feedback": [...]
  },
  
  "cryptographic": {
    "signature": "-----BEGIN PGP SIGNATURE-----",
    "publicKey": "-----BEGIN PGP PUBLIC KEY BLOCK-----"
  }
}
```

### Migration Paths

```
Simplicity KG ──┬──► OpenAI Ecosystem
                ├──► Anthropic (Claude)
                ├──► Google AI
                ├──► Any Local Model
                ├──► Custom Deployment
                └──► Future AI Systems
```

---

## Comparison: Why use Simplicity

| Capability | Perplexity | OpenAI | Google | Simplicity |
|------------|------------|--------|--------|------------|
| **User Knowledge Graph** | ❌ | ❌ | ❌ | ✅ Core |
| **User Owns Intelligence** | ❌ | ❌ | ❌ | ✅ Native |
| **Portable Data** | ❌ | ❌ | ❌ | ✅ Full |
| **GPG Key Control** | ❌ | ❌ | ❌ | ✅ Native |
| **GAN-RAG Coupling** | ❌ | ❌ | ❌ | ✅ Self-Evolving |
| **Unique Experience** | ❌ | ❌ | ❌ | ✅ Immutable |
| **Zero-Knowledge** | ❌ | ❌ | ❌ | ✅ Built-in |
| **Consent Ledger** | ❌ | ❌ | ❌ | ✅ Immutable |
| **Homomorphic Encryption** | ❌ | ❌ | ❌ | ✅ Optional |
| **Federated Learning** | ❌ | ❌ | ❌ | ✅ Supported |
| **Model Agnostic** | ❌ | ❌ | ❌ | ✅ 100+ Models |
| **Weekly Model Refresh** | ❌ | ❌ | ❌ | ✅ Every Tuesday |

---

## The Simplicity Difference

### Traditional AI Systems

```
❌ Your intelligence is TRAPPED in their model
❌ They OWN your learning
❌ You CANNOT leave with your data
❌ One-size-fits-all experience
❌ Data harvesting for training
❌ Generic, impersonal responses
```

### Simplicity

```
✅ YOUR intelligence belongs to YOU
✅ YOU own your knowledge graph
✅ Freedom to leave with everything
✅ YOUR unique experience (immutable hash)
✅ Zero data harvesting (by design)
✅ Personified responses (correlated to YOU)
✅ GPG keys in YOUR control
✅ Zero-knowledge proofs (privacy preserved)
✅ Consent ledger (you decide everything)
✅ GAN-RAG self-evolution (gets smarter for YOU)
```

---

## Module Structure

```
simplicity/
├── simplicity-core/              # Core types, domain models
├── simplicity-rag/               # Lucene-based RAG engine
├── simplicity-knowledge-graph/   # User knowledge graph
├── simplicity-model-registry/    # Model registry with versioning
├── simplicity-intelligence/      # GAN-RAG, correlation, personification
│   ├── IntelligenceCore.java     # Main intelligence engine
│   ├── PersonificationEngine.java # Unique experience creation
│   ├── SentienceEngine.java      # Emotional intelligence
│   └── InterfaceLayer.java        # Dual interface (casual↔expert)
├── simplicity-sovereignty/       # GPG, crypto, portability
└── simplicity-api/              # REST/gRPC API
```

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/your-org/simplicity.git
cd simplicity

# Build all modules
./mvnw clean install

# Run the server
java -jar simplicity-api/target/simplicity-api.jar

# Access the API
curl http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Explain quantum computing",
    "userId": "your-user-id"
  }'
```

---

## License

MIT License - Because your intelligence should be free.

---

**Built with the vision that AI should serve humanity, not control it.**
**Your data. Your keys. Your intelligence. Your sovereignty.**

---

##