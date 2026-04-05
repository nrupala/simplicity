# Perplexity.ai Architecture Reference

## Overview
Perplexity.ai is an AI-powered answer engine that combines real-time web retrieval with multi-model LLM orchestration to deliver sourced, verifiable answers. It processes 780M+ monthly queries across its platform.

## Core Components

### 1. Query Understanding Layer
- **Query Intent Classifier**: LLM-based semantic parsing (not keyword matching)
- **Query Type Detection**: Factual, Comparison, How-to, Opinion, Technical, Research
- **Recency Requirements**: Current vs. historical information gating
- **Domain Signals**: Technical, Medical, Legal, General classification
- **Search Depth Selector**: Simple (1-pass) vs. Comprehensive (multi-pass)

### 2. Hybrid Retrieval Engine (Vespa AI)
- **Dense Retrieval**: Vector/embedding-based semantic search (~30 results)
- **Sparse Retrieval**: BM25/keyword lexical search (~20 results)
- **Result Merge**: Deduplication + interleaved relevance scoring → 50 candidates
- **Web Index**: 200B+ URLs tracked, 100B+ indexed, 400PB hot storage
- **Multi-tier Crawling**: Real-time updates (10K+ updates/second)
- **Content Chunking**: Fine-grained semantic units for precise context

### 3. Reranking Stage
- **Specialized Reranker Model**: Smaller than generation LLM, trained for relevance
- **Multi-feature Scoring**: Lexical + vector + authority + freshness + engagement
- **Selection**: 50 candidates → 5-10 highest quality documents
- **Provider**: Cohere Rerank API (managed) or custom fine-tuned

### 4. Intelligent Model Router (Orchestration Layer)
- **Intent Classifier**: Small efficient model determines query complexity
- **Dynamic Routing Logic**: PPO-reinforcement-learning-based optimization
- **Cost-Latency-Quality Balance**: Smallest model that achieves best UX

#### Model Portfolio:
| Model | Provider | Use Case |
|-------|----------|----------|
| Sonar (base/pro) | Proprietary | Default web-grounded answers |
| PPLX 7B/70B/405B | Fine-tuned Llama | Lightweight inference |
| GPT-4o/5 | OpenAI | Frontier reasoning |
| Claude 3.5/4 | Anthropic | Complex analysis |
| Gemini 2.0 | Google | Multimodal tasks |
| Grok | xAI | Real-time data |
| + 12+ others | Various | Specialized tasks |

### 5. Generation Engine
- **ROSE Inference Engine**: Custom Python/PyTorch core, Rust serving layer
- **Grounded Synthesis**: Strict "no facts beyond retrieved context" policy
- **Multi-Model Support**: 20+ models served simultaneously via Triton
- **Speculative Decoding**: MTP (Multi-Token Prediction) for speed

### 6. Citation & Context Fusion
- **Claim-to-Source Mapping**: Post-generation attribution
- **Inline Citations**: [1], [2], etc. with clickable links
- **Evidence Metadata**: URLs, snippets, authority scores
- **Grounded Prompt Engineering**: Citations baked into generation prompt

### 7. Perplexity Computer (Agentic Layer)
- **19-Model Orchestration**: Claude, GPT, Gemini, Grok + specialized models
- **MCP Integration**: 100+ tool integrations
- **Agentic Workflows**: Sequential planning, multi-step execution
- **Task Decomposition**: Complex queries → sub-queries → parallel execution

## Infrastructure Stack

### Compute Layer
- **GPU Fleet**: NVIDIA H100/Hopper (4-GPU and 8-GPU pods via HGX)
- **Orchestration**: Kubernetes clusters
- **Inference**: NVIDIA Triton → NVIDIA Dynamo Triton
- **Optimization**: TensorRT-LLM, CUDA kernels, tensor parallelism (4x/8x)

### Serving Architecture
- **Load Balancing**: Power-of-two random choices algorithm
- **Prefill-Decode Disaggregation**: Separate GPU allocation per phase
- **SLA Management**: A/B-tested latency/quality targets
- **Autoscaling**: Dynamic GPU allocation based on request load

### Cloud Platform
- **Primary**: AWS (EC2, Bedrock for Claude access)
- **Key Services**: Amazon Bedrock (third-party models), EKS, S3

## Search Modes & Workflows

| Mode | Retrieval | Model | Citations | Latency |
|------|-----------|-------|-----------|---------|
| Standard | 1-pass | Auto/Best | Basic | <2s |
| Pro Search | Multi-step | User/Auto | Structured | 3-5s |
| Deep Research | Sequential | Advanced | Report-grade | 10-30s |
| Computer | Agentic | Multi-model | Dense | Variable |

## API Products
- **Sonar API**: Real-time web-grounded answers, streaming
- **Sonar Pro API**: Enhanced reasoning, larger context
- **Agentic Research API**: Multi-step plans, structured reports
- **Computer API**: Full 19-model agentic access

## Key Design Principles
1. **Retrieval First**: Never generate without grounding
2. **Evidence Transparency**: Every claim has a source
3. **Model Agnostic**: No vendor lock-in, best tool per task
4. **Continuous Learning**: User signals improve all layers
5. **Trust Architecture**: Verifiable = trustworthy

## Complexity vs. Capability Matrix

| Component | MVP | Production | Scale (100M MAU) |
|-----------|-----|------------|-------------------|
| RAG Pipeline | 2-4 weeks | 3-6 months | Years |
| Citations | 1 week | 1 month | Ongoing |
| Model Routing | Not needed | 2-3 months | Complex |
| Multi-model Infra | Not needed | 3-6 months | Tens of millions |
