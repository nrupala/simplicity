# Simplicity - Quick Start Guide

## System Requirements

### Minimum Requirements
| Component | Requirement |
|-----------|-------------|
| **OS** | macOS 12+, Ubuntu 22.04+, Windows 11+ |
| **RAM** | 8 GB (16 GB recommended) |
| **CPU** | 4 cores (8 cores recommended) |
| **Disk** | 10 GB free space |
| **Java** | OpenJDK 21+ or JDK 21+ |

### For Production/Development
| Component | Recommendation |
|-----------|----------------|
| **RAM** | 32 GB+ |
| **CPU** | 8+ cores |
| **GPU** | NVIDIA GPU with 8GB+ VRAM (for local models) |
| **Disk** | 50 GB+ SSD |
| **Network** | Stable internet for API calls |

---

## Installation

### 1. Install Java 21

**macOS:**
```bash
brew install openjdk@21
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Windows:**
Download from [Adoptium](https://adoptium.net/temurin/releases/?version=21)

**Verify:**
```bash
java --version
# Should show: openjdk 21.x.x
```

### 2. Install Maven 3.9+

**macOS:**
```bash
brew install maven
```

**Ubuntu/Debian:**
```bash
sudo apt install maven
```

**Verify:**
```bash
mvn --version
# Should show: Apache Maven 3.9.x
```

### 3. Install Git LFS (for large model files)
```bash
brew install git-lfs  # macOS
sudo apt install git-lfs  # Ubuntu
git lfs install
```

### 4. (Optional) Docker for Containerized Deployment
```bash
brew install docker  # macOS
# Download Docker Desktop for Windows
```

---

## Build Instructions

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/YOUR_ORG/simplicity.git
cd simplicity

# Build all modules
./mvnw clean install

# Or without Maven wrapper (requires Maven installed)
mvn clean install
```

### Build Specific Modules
```bash
# Build only core
mvn clean install -pl simplicity-core

# Build with dependencies
mvn clean install -pl simplicity-core -am

# Skip tests for faster build
mvn clean install -DskipTests
```

### Build Docker Image
```bash
# Build local Docker image
docker build -t simplicity:latest .

# Run with Docker
docker run -p 8080:8080 simplicity:latest
```

---

## Running Simplicity

### 1. Start the Server

**Development Mode (with hot reload):**
```bash
cd simplicity-api
./mvnw spring-boot:run
```

**Production Mode:**
```bash
java -jar simplicity-api/target/simplicity-api.jar
```

**With Docker:**
```bash
docker-compose up -d
```

### 2. Access the API

**Base URL:** `http://localhost:8080`

**Health Check:**
```bash
curl http://localhost:8080/health
```

**Expected Response:**
```json
{"status": "healthy", "version": "0.1.0"}
```

---

## API Usage

### Query Endpoint

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Explain quantum computing in simple terms",
    "userId": "user-123"
  }'
```

**Response:**
```json
{
  "resultId": "uuid",
  "answer": "Quantum computing is a type of computation...",
  "citations": [
    {
      "index": 1,
      "source": "Wikipedia",
      "url": "https://...",
      "confidence": 0.95
    }
  ],
  "confidence": 0.92,
  "suggestions": ["Related: quantum entanglement", "..."]
}
```

### With Intent Command
```bash
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Explain quantum computing",
    "userId": "user-123",
    "customization": {
      "intentCommand": "/explain",
      "response": {
        "type": "MARKDOWN",
        "length": "MEDIUM"
      }
    }
  }'
```

### Ingest Documents
```bash
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "documents": [
      {
        "title": "Quantum Computing Guide",
        "content": "Full article content here...",
        "source": "internal",
        "tags": ["quantum", "computing", "physics"]
      }
    ]
  }'
```

### User Feedback
```bash
curl -X PUT http://localhost:8080/api/v1/user/user-123/feedback \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Explain quantum computing",
    "clickedTags": ["quantum", "computing"],
    "timeOnResult": 45.2,
    "wasHelpful": true
  }'
```

---

## Intent Commands

Use `/command` to control AI behavior:

| Command | Description | Example |
|---------|-------------|---------|
| `/research` | Deep research with citations | `/research climate change` |
| `/code` | Write or debug code | `/code sorting algorithm in Python` |
| `/explain` | Simple explanations | `/explain relativity` |
| `/compare` | Compare options | `/compare React vs Vue` |
| `/summarize` | Brief summaries | `/summarize this article` |
| `/brainstorm` | Creative ideas | `/brainstorm startup ideas` |

---

## Environment Configuration

### config.yaml
```yaml
server:
  host: 0.0.0.0
  port: 8080

database:
  type: sqlite  # sqlite, postgresql, mysql
  path: ./data/simplicity.db

search:
  index_path: ./data/search_index
  max_results: 10
  hybrid_weights:
    vector: 0.6
    keyword: 0.4

models:
  default_provider: openai
  api_key: ${OPENAI_API_KEY}
  local_endpoint: http://localhost:11434

sovereignty:
  encryption_enabled: true
  consent_ledger: immutable

interface:
  default_mode: standard  # casual, standard, expert, architect
```

### Environment Variables
```bash
# API Keys
export OPENAI_API_KEY=sk-...
export ANTHROPIC_API_KEY=sk-ant-...
export GOOGLE_API_KEY=...

# Database
export DATABASE_URL=jdbc:sqlite:./data/simplicity.db

# Security
export GPG_KEY_PATH=./keys
export ENCRYPTION_KEY=your-256-bit-key

# Optional
export LOG_LEVEL=INFO
export MAX_UPLOAD_SIZE=100MB
```

---

## CLI Tool

### Install CLI
```bash
npm install -g @simplicity/cli
# or
pip install simplicity-cli
```

### CLI Commands
```bash
# Query
simplicity ask "What is machine learning?"

# With intent
simplicity ask "/code" "binary search in Java"

# Interactive mode
simplicity chat

# Set personality
simplicity config set personality=casual

# Export knowledge graph
simplicity kg export --format=json --output=./my-kg.json

# Import from other system
simplicity kg import --from=openai --file=./openai-data.json
```

---

## Troubleshooting

### Java Not Found
```bash
# Check JAVA_HOME
echo $JAVA_HOME

# Set JAVA_HOME (macOS)
export JAVA_HOME=$(/usr/libexec/java_home)

# Set JAVA_HOME (Linux)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use different port
java -jar simplicity-api.jar --server.port=8081
```

### Out of Memory
```bash
# Increase heap size
export JAVA_OPTS="-Xmx8g -Xms4g"

# Or in Docker
docker run -m 8g -p 8080:8080 simplicity:latest
```

### Model Connection Issues
```bash
# Check if Ollama is running (local models)
curl http://localhost:11434/api/tags

# Restart Ollama
ollama serve
```

---

## API Documentation

### OpenAPI/Swagger
Once running, access:
- **Swagger UI:** http://localhost:8080/swagger-ui
- **OpenAPI JSON:** http://localhost:8080/api-docs

### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/query` | Query with personalization |
| POST | `/api/v1/stream` | Streaming response |
| POST | `/api/v1/ingest` | Ingest documents |
| GET | `/api/v1/user/{id}/graph` | Get user knowledge graph |
| PUT | `/api/v1/user/{id}/feedback` | Submit feedback |
| POST | `/api/v1/user/{id}/features` | Add user feature |
| GET | `/api/v1/models` | List available models |
| POST | `/api/v1/models/preference` | Set model preferences |
| GET | `/api/v1/config/schema` | Get config schema |
| PUT | `/api/v1/sovereignty/export` | Export user data |
| POST | `/api/v1/sovereignty/import` | Import user data |
| GET | `/health` | Health check |

---

## Quick Reference

```bash
# Build
mvn clean install

# Run
java -jar simplicity-api/target/simplicity-api.jar

# Test
curl http://localhost:8080/health

# Query
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{"query": "hello", "userId": "test"}'

# Stop
pkill -f simplicity-api
```

---

**For more help:** [GitHub Issues](https://github.com/YOUR_ORG/simplicity/issues)
