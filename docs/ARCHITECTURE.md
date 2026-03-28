# Simplicity Architecture Guide

## Overview

Simplicity is a **next-generation AI answer engine** with user-owned intelligence. This guide explains the architecture and how the pieces connect.

---

## System Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              USER INPUT                                  │
│                    (Query + Context + Preferences)                       │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CRYPTOGRAPHIC VERIFICATION                            │
│              (GPG Signature → Consent Check → Auth)                     │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    USER KNOWLEDGE GRAPH                                 │
│         (Entities + Relationships + Preferences + Patterns)              │
│                    🔐 OWNED BY USER 🔐                                  │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CORRELATION ENGINE                                    │
│            (User Knowledge ↔ Model Capability)                          │
│                 Creates: Unique Experience Hash                          │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        GAN-RAG ENGINE                                   │
│                                                                          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐             │
│  │   RAG       │───▶│  GENERATOR   │───▶│ DISCRIMINATOR│             │
│  │  Retrieval   │    │  (Multiple)   │    │  (Scoring)   │             │
│  │  + Personal │    │              │    │              │             │
│  └──────────────┘    └──────────────┘    └──────────────┘             │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    CONTINUOUS LEARNING                             │  │
│  │         (Feedback → Update KG → Evolve Model)                     │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    PERSONIFICATION ENGINE                                │
│              (Emotional Tone + Personality + Style)                     │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          OUTPUT                                         │
│         Unique Response + Citations + Emotions + Signature               │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Module Breakdown

### 1. Simplicity Core (`simplicity-core`)

**Purpose:** Shared domain models and types

```
simplicity-core/
├── domain/
│   ├── DomainModels.java      # User, Feature, Interest, Document
│   ├── QueryModels.java      # QueryRequest, QueryResponse, SearchResult
│   └── UserCustomization.java # Intent commands, Response config
└── service/
    └── UserCustomizationService.java
```

**Key Types:**
- `User` - Identity with preferences
- `Feature` - User attributes (role, team, domain)
- `Interest` - Topics user cares about
- `QueryRequest` - Query with customization
- `QueryResponse` - Answer with citations

---

### 2. Simplicity RAG (`simplicity-rag`)

**Purpose:** Lucene-based retrieval engine

```
simplicity-rag/
└── SimplicitySearchEngine.java
```

**Features:**
- Hybrid search (Vector + BM25)
- Personalized reranking
- Citation mapping
- Knowledge grounding

**Flow:**
```
Query → Analyzer → Boolean Query → Search → Score → Rerank → Results
                                    ↑
                        User Knowledge Graph (boost)
```

---

### 3. Simplicity Knowledge Graph (`simplicity-knowledge-graph`)

**Purpose:** User-owned knowledge graph

```
simplicity-knowledge-graph/
└── UserKnowledgeGraph.java
```

**Components:**
- User profiles with features
- Interest tracking
- Behavioral signals
- Organizational context

**Data Flow:**
```
User Action → Record Interest → Update Features → Learn Patterns → Personalized Results
```

---

### 4. Simplicity Model Registry (`simplicity-model-registry`)

**Purpose:** Manage 100+ AI models

```
simplicity-model-registry/
└── ModelRegistry.java
```

**Model Types:**
| Tier | Providers | Examples |
|------|-----------|----------|
| Premium | OpenAI, Anthropic, Google | GPT-5, Claude 4, Gemini Ultra |
| Open | Meta, Mistral, DeepSeek | Llama 4, Mistral Nemo, DeepSeek V4 |
| Local | Ollama, LM Studio | Local LLMs |

**Updates:** Every Tuesday

---

### 5. Simplicity Intelligence (`simplicity-intelligence`)

**Purpose:** The brain - GAN-RAG + Personification

```
simplicity-intelligence/
├── IntelligenceCore.java       # Main orchestration
├── PersonificationEngine.java # Unique experience
├── SentienceEngine.java      # Emotional intelligence
└── InterfaceLayer.java       # UI configuration
```

#### Intelligence Core Flow

```
Query → Intent Analysis → Correlation → RAG Retrieval
       → Generate Candidates → Discriminate → Select
       → Personify → Response
```

#### GAN-RAG Coupling

```
┌─────────────────────────────────────────────────────────────┐
│                      GAN-RAG LAYER                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   RETRIEVAL ─────┬──────▶ GENERATORS                        │
│   (Hybrid +     │        ├── Primary                       │
│    Personal)     │        ├── Alternative                   │
│                 │        ├── Concise                       │
│                 │        └── Detailed                      │
│                 │               │                           │
│                 │               ▼                           │
│                 │        DISCRIMINATORS                   │
│                 │        ├── Accuracy                      │
│                 │        ├── Relevance                     │
│                 │        ├── Personal                     │
│                 │        └── Quality                      │
│                 │               │                           │
│                 └──────────────┴──▶ SCORE + RANK          │
│                                                              │
│   LEARNING ◀──────────────────────────────────────────────  │
│   (Implicit + Explicit Feedback)                            │
└─────────────────────────────────────────────────────────────┘
```

#### Personification Engine

```
┌─────────────────────────────────────────────────────────────┐
│                   PERSONIFICATION                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   User KG ──┬──▶ CORRELATION ENGINE                        │
│             │       ├── Topic Match                         │
│             │       ├── Complexity Match                    │
│             │       └── Style Match                        │
│             │               │                              │
│             │               ▼                              │
│             │       WEIGHT CALCULATION                    │
│             │       ├── User Weight (expertise)           │
│             │       └── Model Weight (capability)         │
│             │               │                              │
│             │               ▼                              │
│             │       UNIQUE APPROACH                       │
│             │       ├── USER_GUIDED                       │
│             │       ├── COLLABORATIVE                     │
│             │       ├── EDUCATIONAL                       │
│             │       └── MODEL_AUTHORITATIVE              │
│             │               │                              │
│             └───────────────┴──────▶ PERSONIFIED OUTPUT     │
│                                              │              │
│   Personality Profile ───────────────────────┤              │
│   Emotional Engine ───────────────────────────┤              │
│   Expression Generator ───────────────────────┴──▶ Response  │
└─────────────────────────────────────────────────────────────┘
```

---

### 6. Simplicity Sovereignty (`simplicity-sovereignty`)

**Purpose:** User-owned data and portability

```
simplicity-sovereignty/
└── UserSovereigntyEngine.java
```

**Features:**
- GPG key management
- Consent ledger
- Data export (JSON, GraphQL, RDF)
- Data import from any system
- Model portability

**Sovereignty Principles:**
```
┌─────────────────────────────────────────────────────────────┐
│                    USER SOVEREIGNTY                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐     │
│   │  OWNERSHIP │   │   CONTROL   │   │ PORTABILITY │     │
│   │    🔐      │   │     🎛️     │   │     🔄      │     │
│   │ User owns  │   │  User decides │  │  User can  │     │
│   │ their data │   │  what to share│  │  take data  │     │
│   └─────────────┘   └─────────────┘   └─────────────┘     │
│                                                              │
│   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐     │
│   │   CONSENT  │   │ TRANSPARENCY│   │   ZERO      │     │
│   │     ⚖️     │   │     🔍      │   │  KNOWLEDGE  │     │
│   │ Explicit   │   │  Full audit │   │     🔒      │     │
│   │  consent   │   │    logs     │   │   Privacy   │     │
│   │  required  │   │            │   │ preserved   │     │
│   └─────────────┘   └─────────────┘   └─────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

### 7. Simplicity API (`simplicity-api`)

**Purpose:** REST API layer

```
simplicity-api/
├── Main.java                  # Entry point
└── SimplicityServer.java     # HTTP handlers
```

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/query` | Query with personalization |
| POST | `/api/v1/ingest` | Ingest documents |
| GET | `/api/v1/user/{id}/graph` | Get user KG |
| PUT | `/api/v1/user/{id}/feedback` | Submit feedback |
| POST | `/api/v1/sovereignty/export` | Export user data |
| POST | `/api/v1/sovereignty/import` | Import user data |

---

## Data Flow Examples

### Example 1: Simple Query

```
User: "What is quantum computing?"

Flow:
1. GPG verify user identity
2. Load user knowledge graph
3. Detect query type = FACTUAL
4. Check user expertise = general
5. RAG retrieve documents
6. Generate response
7. Add emotional tone
8. Apply personalization
9. Return with citations
```

### Example 2: Research Query

```
User: "/research impact of AI on healthcare"

Flow:
1. Parse intent command = /research
2. Set deep research mode
3. Load user's domain knowledge (healthcare)
4. RAG retrieve comprehensive sources
5. Generate multiple candidates
6. Discriminate for accuracy
7. Create detailed report
8. Add citations
9. Suggest follow-ups
```

### Example 3: Personalization Learning

```
User clicks on result about "machine learning"
User spends 45 seconds reading
User provides positive feedback

Flow:
1. Record interaction
2. Update interest vector (+ml)
3. Boost ml-related features
4. Learn user expertise level
5. Adjust future results
6. Update behavior patterns
```

---

## Configuration

### config.yaml Structure

```yaml
# Server Configuration
server:
  host: 0.0.0.0
  port: 8080

# Search Engine
search:
  index_path: ./data/index
  max_results: 10
  hybrid_weights:
    vector: 0.6
    keyword: 0.4

# Model Configuration
models:
  default: gpt-4
  providers:
    openai:
      api_key: ${OPENAI_API_KEY}
    anthropic:
      api_key: ${ANTHROPIC_API_KEY}
    local:
      endpoint: http://localhost:11434

# Intelligence Settings
intelligence:
  gan_rag:
    candidates: 3
    discriminator_threshold: 0.7
  personalization:
    strength: 0.6

# Sovereignty
sovereignty:
  encryption: true
  consent_ledger: immutable
  export_formats:
    - json
    - graphql
    - rdf
```

---

## Security Model

```
┌─────────────────────────────────────────────────────────────┐
│                      SECURITY LAYERS                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. IDENTITY                                                │
│     └── GPG Key Pair (User-controlled)                       │
│                                                              │
│  2. AUTHENTICATION                                          │
│     └── JWT + GPG Signature                                  │
│                                                              │
│  3. ENCRYPTION                                               │
│     ├── At Rest: AES-256-GCM                                 │
│     ├── In Transit: TLS 1.3+                                 │
│     └── Zero-Knowledge: Optional                              │
│                                                              │
│  4. CONSENT                                                  │
│     └── Immutable Ledger (User-controlled)                     │
│                                                              │
│  5. AUDIT                                                    │
│     └── Full Access Log (Tamper-proof)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Performance Targets

| Metric | Target |
|--------|--------|
| Query Latency (p50) | < 500ms |
| Query Latency (p99) | < 2s |
| Throughput | 1000 req/min |
| Index Size | 100M documents |
| User KG Size | Unlimited |

---

## Monitoring

**Health Endpoint:**
```bash
curl http://localhost:8080/health
```

**Metrics (Prometheus format):**
```bash
curl http://localhost:8080/metrics
```

**Key Metrics:**
- `simplicity_queries_total`
- `simplicity_query_duration_seconds`
- `simplicity_kg_entities_total`
- `simplicity_model_invocations_total`

---

## Deployment Options

### 1. Development (Local)
```bash
mvn spring-boot:run
```

### 2. Docker
```bash
docker-compose up -d
```

### 3. Kubernetes
```bash
kubectl apply -f k8s/
```

### 4. Cloud (AWS/GCP/Azure)
```bash
terraform apply -f infra/
```

---

**For detailed API docs:** See [QUICKSTART.md](QUICKSTART.md)
