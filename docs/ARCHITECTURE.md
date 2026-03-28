# Simplicity - Architecture & Design

## Overview

Simplicity is an AI answer engine that combines retrieval-augmented generation (RAG) with personal knowledge graphs to deliver highly personalized, verifiable answers.

## Core Principles

1. **Personalization by Default** - Every user has a knowledge graph that shapes their search results
2. **Built on Lucene** - We don't rely on external search engines; we build on Apache Lucene
3. **Knowledge Graph Native** - Entities and relationships, not just documents
4. **Continuous Learning** - The system learns from user behavior to improve results
5. **Verifiable Answers** - Every answer comes with citations and confidence scores

## System Architecture

### Layer 1: User Knowledge Graph

The foundation of personalization is the **User Knowledge Graph (UKG)**:

```
┌──────────────────────────────────────────────────────────────┐
│                    User Knowledge Graph                       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────┐     ┌──────────┐     ┌─────────────────────┐   │
│  │Identity │────▶│ Features │────▶│ Interests           │   │
│  │         │     │          │     │                     │   │
│  │• Name   │     │• Role    │     │• Topics              │   │
│  │• Email  │     │• Team    │     │• Keywords            │   │
│  │• Org    │     │• Domain  │     │• Preferences         │   │
│  │• Perms  │     │• Level   │     │• History             │   │
│  └─────────┘     └──────────┘     └─────────────────────┘   │
│       │                                    │                 │
│       │                                    ▼                 │
│       │              ┌───────────────────────────────────┐   │
│       │              │     Organizational Context        │   │
│       │              │                                    │   │
│       │              │  • Team structures & hierarchies   │   │
│       │              │  • Domain expertise mapping        │   │
│       │              │  • Trust relationships            │   │
│       │              │  • Access permissions             │   │
│       │              └───────────────────────────────────┘   │
│       │                                                     │
│       ▼                                                     │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              Behavioral Signals                          │ │
│  │  • Search patterns      • Click-through rates           │ │
│  │  • Query refinements   • Time-on-content               │ │
│  │  • Feedback signals    • Session context               │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### Layer 2: Simplicity RAG Engine

Built on Apache Lucene, our RAG engine differs from standard implementations:

```
┌─────────────────────────────────────────────────────────────┐
│                    Simplicity RAG Engine                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Query ──▶ ┌─────────────┐ ──▶ Context ──▶ ┌─────────────┐  │
│            │   Hybrid    │                  │  Knowledge   │  │
│            │   Search    │                  │   Grounder  │  │
│            │             │                  │             │  │
│            │ ┌─────────┐ │                  │ • Fact bind  │  │
│            │ │BM25     │ │                  │ • Citation  │  │
│            │ │(Keyword)│ │                  │ • Verify     │  │
│            │ └─────────┘ │                  └─────────────┘  │
│            │             │                        │          │
│            │ ┌─────────┐ │                        ▼          │
│            │ │Neural   │ │                  ┌─────────────┐   │
│            │ │Vector   │ │                  │   Model     │   │
│            │ │Search   │ │                  │  Generator  │   │
│            │ └─────────┘ │                  └─────────────┘   │
│            └─────────────┘                        │          │
│                    │                              ▼          │
│                    ▼                    ┌─────────────────┐  │
│            ┌─────────────┐               │   Cited Answer  │  │
│            │   Neural    │               │                 │  │
│            │  Reranker  │               │ [1] Source...   │  │
│            │            │               │ [2] Source...   │  │
│            │ • Learning │               └─────────────────┘  │
│            │ • Signals  │                                  │
│            │ • Personal │                                  │
│            └─────────────┘                                  │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Layer 3: Intelligence Orchestration

The orchestration layer connects user context to the RAG engine:

```
┌─────────────────────────────────────────────────────────────┐
│              Intelligence Orchestration Layer               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────┐    ┌────────────┐    ┌────────────────────┐ │
│  │   Query    │    │    User    │    │   Model            │ │
│  │   Intent   │───▶│   Context  │───▶│   Router           │ │
│  │   Parser   │    │   Builder  │    │   (PPO-RL)         │ │
│  │            │    │            │    │                    │ │
│  │ • Type     │    │ • KG       │    │ • Task match       │ │
│  │ • Entity   │    │   extract  │    │ • Latency target    │ │
│  │ • Scope    │    │ • History  │    │ • Cost budget       │ │
│  │ • Depth    │    │ • Signals  │    │ • Quality need      │ │
│  └────────────┘    └────────────┘    └────────────────────┘ │
│         │                  │                   │            │
│         ▼                  ▼                   ▼            │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                  Personalization Engine                  │ │
│  │                                                          │ │
│  │  Combines user graph + query context + behavioral       │ │
│  │  signals to create a personalized search profile         │ │
│  │                                                          │ │
│  │  Output: { query, context, boost_weights, filters }     │ │
│  └─────────────────────────────────────────────────────────┘ │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Response Synthesizer                  │ │
│  │                                                          │ │
│  │  • Merge RAG results with user context                   │ │
│  │  • Apply personalization weights                          │ │
│  │  • Generate answer with citations                         │ │
│  │  • Log interaction for learning                           │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Data Models

### User Knowledge Graph (Neo4j-friendly schema)

```java
// Core entities
User {
    id: UUID
    email: String
    name: String
    organizationId: UUID
    createdAt: Timestamp
    lastActiveAt: Timestamp
}

Feature {
    id: UUID
    userId: UUID → User
    type: FeatureType (ROLE, TEAM, DOMAIN, LEVEL, SKILL)
    value: String
    confidence: Double (0-1)
    source: SourceType (EXPLICIT, INFERRED, DERIVED)
}

Interest {
    id: UUID
    userId: UUID → User
    topic: String
    keywords: List<String>
    strength: Double (0-1)
    recency: Timestamp
    interactions: Integer
}

Domain {
    id: UUID
    name: String
    description: String
    parentDomainId: UUID → Domain (hierarchical)
    experts: List<UUID> → User
}

Organization {
    id: UUID
    name: String
    structure: JSON (team hierarchy)
    domains: List<UUID> → Domain
}
```

### Knowledge Relationships

```
(User) ─[HAS_FEATURE]──▶ (Feature)
(User) ─[INTERESTED_IN]──▶ (Interest)
(User) ─[BELONGS_TO]──▶ (Team)
(User) ─[WORKS_IN]──▶ (Domain)
(User) ─[TRUSTS]──▶ (User)  // expertise endorsement
(Feature) ─[PART_OF]──▶ (Domain)
(Interest) ─[RELATED_TO]──▶ (Interest)
(Domain) ─[CONTAINS]──▶ (SubDomain)
```

## API Design

### Core Endpoints

```
POST /api/v1/query
  Request: { query, userId, options }
  Response: { answer, citations, confidence, suggestions }

POST /api/v1/ingest
  Request: { documents[], source, metadata }
  Response: { jobId, status }

GET /api/v1/user/{userId}/graph
  Response: { identity, features, interests, context }

PUT /api/v1/user/{userId}/feedback
  Request: { queryId, helpful, corrections[], signals }
  Response: { status }
```

### Streaming

```
POST /api/v1/stream/query
  Server-Sent Events for real-time answer generation
```

## Personalization Flow

```
1. User submits query
         │
         ▼
2. Extract user from request
         │
         ▼
3. Load User Knowledge Graph
   • Identity features
   • Interest vector
   • Recent interactions
   • Team/Domain context
         │
         ▼
4. Build personalization profile
   • Boost relevant documents
   • Filter by permissions
   • Adjust confidence thresholds
         │
         ▼
5. Execute RAG with profile
   • Lucene search with boosts
   • Rerank with personalization signals
   • Ground with user context
         │
         ▼
6. Generate answer
         │
         ▼
7. Log interaction
   • Store for learning
   • Update behavioral signals
   • Refine user model
         │
         ▼
8. Return personalized answer
```

## Search Engine (Lucene)

### Index Structure

```
simplicity-index/
├── documents/
│   ├── _0.cfe
│   ├── _0.cfs
│   └── segments_N
├── embeddings/
│   ├── vectors.slm
│   └── index.mdb
├── kg_entities/
│   └── entities.slm
└── user_signals/
    └── signals.mdb
```

### Custom Analyzers

1. **OrgAwareAnalyzer**: Respects organizational boundaries
2. **PersonalizedAnalyzer**: Incorporates user interest boosting
3. **DomainAnalyzer**: Optimized for technical/domain-specific terms

## Learning System

### Feedback Loop

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Explicit   │    │   Implicit   │    │   Derived    │
│   Feedback   │    │   Signals    │    │   Features   │
├──────────────┤    ├──────────────┤    ├──────────────┤
│ 👍/👎        │    │ Click rate   │    │ Topic affinity│
│ Corrections │    │ Time-on-doc   │    │ Depth prefer  │
│ Saves       │    │ Scroll %     │    │ Style match   │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                    │                    │
       └────────────────────┴────────────────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │  Learning Engine │
                  │                  │
                  │ • Weight updates │
                  │ • Interest decay │
                  │ • Anomaly detect │
                  └────────┬─────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │  User Knowledge  │
                  │     Graph        │
                  │                  │
                  │ Updated profiles  │
                  │ New interests    │
                  │ Refined features │
                  └──────────────────┘
```

## Comparison with Perplexity

| Aspect | Perplexity | Simplicity |
|--------|------------|------------|
| Search Backend | Vespa.ai | Lucene (built from scratch) |
| Knowledge Model | Flat documents | Knowledge graph |
| User Context | None | Full UKG |
| Personalization | None | Core feature |
| Learning | Implicit (clicks) | Full behavioral modeling |
| Scope | Global web | Organizational first |
| Citations | Source-level | Claim-level |

## Getting Started

See [README.md](README.md) for quick start instructions.

## Future Vision

- [ ] Federated learning across organizations
- [ ] Real-time collaborative search
- [ ] Multi-modal knowledge graphs
- [ ] Explainable personalization
- [ ] Privacy-preserving ML
