# SIMPLICITY: 

> **"Your Intelligence. Your Rules. Your Portability. Your Sovereignty."**

---

## Downloads (v1.3.0)

| Artifact | Description | Download |
|----------|------------|----------|
| simplicity-model-binder-1.0.0.jar | Model binding layer | [Download](releases/v1.3.0/simplicity-model-binder-1.0.0.jar) |
| simplicity-core-1.0.0.jar | Core domain models | [Download](releases/v1.3.0/simplicity-core-1.0.0.jar) |
| PWA (Web App) | Works in any browser | [Download](releases/v1.3.0/pwa.zip) or [Browse](releases/v1.3.0/pwa/) |

### Quick Start

```powershell
# Run PWA - just open in browser
# releases/v1.3.0/pwa/index.html

# Run Java demo (requires Java 21)
java -cp "releases/v1.3.0/simplicity-model-binder-1.0.0.jar;releases/v1.3.0/simplicity-core-1.0.0.jar" com.simplicity.model.binder.ModelBinderDemo
```

---

## Free Open-Source LLMs

**Comprehensive List**: https://github.com/eugeneyan/open-llms

Popular models to try with Ollama:

```bash
ollama pull llama3.2        # Meta's latest (2GB)
ollama pull mistral          # Efficient (4GB)
ollama pull phi3             # Microsoft's tiny model (2GB)
ollama pull qwen2.5-coder    # Coding specialized (8GB)
ollama pull deepseek-r1      # Reasoning (5GB)
```

---

## Executive Summary

Simplicity is a fundamentally different approach to AI-powered search and answers. While existing systems like Perplexity.ai, OpenAI, and Google trap your intelligence in their models, Simplicity ensures **YOU own your intelligence forever**.

| Feature | Perplexity | OpenAI | Google | **Simplicity** |
|---------|-----------|--------|--------|----------------|
| User Knowledge Graph | ❌ | ❌ | ❌ | ✅ Core Foundation |
| Portable Intelligence | ❌ | ❌ | ❌ | ✅ Full Portability |
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