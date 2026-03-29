# Simplicity AI - Releases

This folder contains pre-built release artifacts for each version.

## Latest Release: v1.3.0

### Quick Download

| Artifact | Download |
|----------|----------|
| simplicity-core-1.0.0.jar | [Download](v1.3.0/simplicity-core-1.0.0.jar) |
| simplicity-model-binder-1.0.0.jar | [Download](v1.3.0/simplicity-model-binder-1.0.0.jar) |
| PWA (Web App) | [Download](v1.3.0/pwa.zip) or [Browse](v1.3.0/pwa/) |

### What's Included in v1.3.0

- **Java JARs**: Ready-to-run .jar files (requires Java 21)
- **PWA**: Progressive Web App - works offline in browser
- **Model Binder Demo**: Tests Ollama and LM Studio connections

### System Requirements

| Platform | Requirements |
|----------|-------------|
| PWA | Any modern browser |
| Java JAR | Java 21+, 4GB+ RAM |
| Local AI | Ollama or LM Studio installed |

---

## How to Use

### 1. Try the PWA (Easiest)

```bash
# Just open in browser
open v1.3.0/pwa/index.html
```

### 2. Run Java Demo

```powershell
# Requires Java 21
java -cp "v1.3.0/simplicity-model-binder-1.0.0.jar;v1.3.0/simplicity-core-1.0.0.jar" com.simplicity.model.binder.ModelBinderDemo
```

### 3. Build from Source

```bash
git clone https://github.com/nrupala/simplicity.git
cd simplicity
mvn clean package -DskipTests
```

---

## Free Open-Source LLMs

**Comprehensive List**: https://github.com/eugeneyan/open-llms

Popular models to try with Ollama:

```bash
# Install Ollama from https://ollama.ai, then:
ollama pull llama3.2        # Meta's latest (2GB)
ollama pull mistral         # Efficient (4GB)
ollama pull phi3            # Microsoft's tiny model (2GB)
ollama pull qwen2.5-coder   # Coding specialized (8GB)
ollama pull deepseek-r1     # Reasoning (5GB)
```

---

## All Releases

| Version | Date | Downloads |
|---------|------|----------|
| [v1.3.0](v1.3.0/) | 2026-03-29 | JARs + PWA |
| v1.2.0 | 2026-03-28 | PWA only |

---

For source code, documentation, and more: https://github.com/nrupala/simplicity
