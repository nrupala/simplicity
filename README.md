# Simplicity - AI Answer Engine with User-Owned Intelligence

> **"The Next Decade of AI - Your Intelligence. Your Rules. Your Portability."**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21+-green.svg)](https://adoptium.net/)
[![Build](https://github.com/YOUR_ORG/simplicity/actions/workflows/build.yml/badge.svg)]()

## What is Simplicity?

Simplicity is a fundamentally different AI answer engine. Unlike systems like Perplexity.ai, OpenAI, or Google where **your intelligence is trapped in their models**, Simplicity ensures **YOU own your intelligence forever**.

### Why We're Different

| Feature | Perplexity | OpenAI | Google | **Simplicity** |
|---------|-----------|--------|--------|----------------|
| User Knowledge Graph | ❌ | ❌ | ❌ | ✅ Core Foundation |
| Portable Intelligence | ❌ | ❌ | ❌ | ✅ Full Portability |
| GPG Key Ownership | ❌ | ❌ | ❌ | ✅ Native Support |
| Unique User Experience | ❌ | ❌ | ❌ | ✅ Immutable Hash |
| GAN-RAG Correlation | ❌ | ❌ | ❌ | ✅ Self-Evolving |
| Personification | ❌ | ❌ | ❌ | ✅ Emotional AI |
| Cryptographic Sovereignty | ❌ | ❌ | ❌ | ✅ Zero-Knowledge |

---

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+

### Build & Run
```bash
# Clone
git clone https://github.com/YOUR_ORG/simplicity.git
cd simplicity

# Build
mvn clean install

# Run
java -jar simplicity-api/target/simplicity-api.jar

# Query
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{"query": "Explain quantum computing", "userId": "user-123"}'
```

### Docker
```bash
docker-compose up -d
```

---

## Key Features

### 🧠 User Knowledge Graph
Your knowledge, preferences, and patterns - **owned by you**.

```java
// Simplicity learns your context
UserKnowledgeGraph kg = new UserKnowledgeGraph();
kg.recordInterest(userId, "quantum computing");
kg.addFeature(userId, Feature.domain("physics"));
kg.addFeature(userId, Feature.role("researcher"));
```

### 🔗 Correlation Engine
Creates a **unique experience** no one else can replicate.

```
User Knowledge ──┬──▶ Correlation ──▶ Unique Experience Hash
Model Capability─┘                   (Nobody else has this)
```

### 🧠 GAN-RAG Coupling
Self-improving intelligence that gets smarter with every query.

```java
// Generator creates candidates
List<GeneratedResponse> candidates = generator.create(request, context);

// Discriminator scores them
ScoredResponse best = discriminator.rank(candidates, criteria);

// Learning updates the system
learning.record(feedback);
```

### 🎭 Personification
AI that feels human - curious, empathetic, humble.

```java
// Configure personality
sentience.setPersonaPreset(userId, PersonaPreset.COMPASSIONATE);

// Generate emotional response
EmotionalResponse response = sentience.generateEmotionalResponse(context, userId);
String answer = sentience.injectEmotion(baseAnswer, response, persona);
```

### 🔐 Cryptographic Sovereignty
Your keys. Your data. Your control.

```java
// Export your knowledge graph
ExportedPackage pkg = sovereignty.exportUserData(userId, ExportFormat.JSON, EncryptionLevel.STRONG);

// Migrate to any system
sovereignty.migrateToNewModel(userId, "openai");
```

### 🤖 100+ AI Models
Choose your models. Change anytime. Keep your intelligence.

```bash
# Set preferred model per domain
POST /api/v1/models/preference
{
  "domain": "coding",
  "model": "claude-3-opus",
  "provider": "anthropic"
}
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           SIMPLICITY STACK                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Interface Layer (Casual → Standard → Expert → Architect)        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Personification Engine (Emotional AI + Personality)              │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Correlation Engine (User ↔ Model = Unique Experience)           │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  GAN-RAG Coupling (Generator + Discriminator + Learning)        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  User Knowledge Graph (Owned by User)                           │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Simplicity RAG (Lucene: Vector + BM25 + Personalization)       │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Model Registry (100+ Models, Tuesday Updates)                  │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Sovereignty Engine (GPG + Consent + Portability)               │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| `simplicity-core` | Domain models and types | ✅ Ready |
| `simplicity-rag` | Lucene-based RAG engine | ✅ Ready |
| `simplicity-knowledge-graph` | User knowledge graph | ✅ Ready |
| `simplicity-model-registry` | Model management | ✅ Ready |
| `simplicity-intelligence` | GAN-RAG + Personification | ✅ Ready |
| `simplicity-sovereignty` | GPG + Portability | ✅ Ready |
| `simplicity-api` | REST API | ✅ Ready |

---

## Intent Commands

Control AI behavior with `/commands`:

| Command | Description |
|---------|-------------|
| `/research` | Deep research with citations |
| `/code` | Write or debug code |
| `/explain` | Simple explanations |
| `/compare` | Compare options |
| `/summarize` | Brief summaries |
| `/brainstorm` | Creative ideas |

```bash
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "quantum computing",
    "customization": {
      "intentCommand": "/research"
    }
  }'
```

---

## API Reference

### Query
```bash
POST /api/v1/query
{
  "query": "What is machine learning?",
  "userId": "user-123",
  "customization": {
    "responseType": "MARKDOWN",
    "length": "MEDIUM"
  }
}
```

### Ingest Documents
```bash
POST /api/v1/ingest
{
  "documents": [
    {
      "title": "ML Guide",
      "content": "Machine learning is...",
      "tags": ["ai", "ml"]
    }
  ]
}
```

### Export Knowledge Graph
```bash
POST /api/v1/sovereignty/export
{
  "format": "JSON",
  "encryption": "STANDARD"
}
```

---

## Configuration

### Environment Variables
```bash
export OPENAI_API_KEY=sk-...
export ANTHROPIC_API_KEY=sk-ant-...
export JAVA_OPTS="-Xmx8g"
```

### config.yaml
```yaml
server:
  port: 8080

search:
  hybrid_weights:
    vector: 0.6
    keyword: 0.4

models:
  default: gpt-4
  providers:
    openai:
      api_key: ${OPENAI_API_KEY}

sovereignty:
  encryption: true
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [QUICKSTART.md](docs/QUICKSTART.md) | Installation and usage guide |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System architecture deep-dive |
| [API.md](docs/API.md) | API reference |

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

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing`)
5. Open a Pull Request

---

## Roadmap

- [x] Core architecture
- [x] RAG engine
- [x] Knowledge graph
- [x] GAN-RAG coupling
- [x] Personification
- [x] Sovereignty engine
- [x] GPG key management
- [ ] Federated learning
- [ ] Multi-modal support
- [ ] Enterprise deployment

---

## License

MIT License - See [LICENSE](LICENSE)

---

**Built with the vision that AI should serve humanity, not control it.**

**Your data. Your keys. Your intelligence. Your sovereignty.**
