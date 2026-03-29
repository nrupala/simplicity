# Simplicity AI v1.3.0 - Release Artifacts

## Downloads

### JAR Files (Java 21 required)

| File | Description | Size |
|------|-------------|------|
| [simplicity-core-1.0.0.jar](simplicity-core-1.0.0.jar) | Core domain models | 72 KB |
| [simplicity-model-binder-1.0.0.jar](simplicity-model-binder-1.0.0.jar) | Model binding layer | 88 KB |

### PWA (Web App)

| File | Description |
|------|-------------|
| [pwa/](pwa/) | Progressive Web App folder |

---

## How to Run

### Option 1: Run the PWA (Easiest)

1. Download the `pwa/` folder
2. Open `pwa/index.html` in a browser
3. Works offline after first load!

### Option 2: Build and Run Java JAR

**Prerequisites:**
- Java 21: https://adoptium.net/
- Ollama (optional): https://ollama.ai

**Steps:**

```powershell
# Set Java path
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Download models (optional - for local AI)
ollama pull llama3

# Run the demo
java -cp "simplicity-model-binder-1.0.0.jar;simplicity-core-1.0.0.jar" com.simplicity.model.binder.ModelBinderDemo
```

### Option 3: Build from Source

```powershell
# Clone the repository
git clone https://github.com/nrupala/simplicity.git
cd simplicity

# Build
mvn clean package -DskipTests -pl simplicity-core,simplicity-model-binder -am

# Run demo
mvn exec:java -pl simplicity-model-binder -Dexec.mainClass="com.simplicity.model.binder.ModelBinderDemo"
```

---

## Features

### Supported AI Providers

#### Local (No API Key Required)
- **Ollama** - Llama 3, Mistral, Phi-3, Qwen, Gemma, DeepSeek, and 20+ models
- **LM Studio** - Any GGUF format model

#### Cloud (API Key Required)
- **OpenAI** - GPT-4, o1, GPT-4o
- **Anthropic** - Claude 3.5, Claude 3
- **Google** - Gemini 1.5, Gemini 2.0
- **Azure OpenAI** - Enterprise deployments
- **HuggingFace** - 100K+ models
- **Groq** - Fast inference
- **TogetherAI** - Open models

---

## Free OSS LLMs

For a comprehensive list of open-source LLMs available for local use:

**https://github.com/eugeneyan/open-llms**

This curated list includes:
- LLaMA variants
- Mistral & Mixtral
- Phi models
- Qwen & Qwen-Coder
- DeepSeek models
- Gemma
- And hundreds more...

### Quick Links to Popular Open Models

| Model | Size | Best For | Ollama Command |
|-------|------|----------|----------------|
| LLaMA 3.2 | 2GB | General | `ollama pull llama3.2` |
| Mistral | 4GB | Balanced | `ollama pull mistral` |
| Phi-3 | 2GB | Efficiency | `ollama pull phi3` |
| Qwen 2.5 Coder | 8GB | Coding | `ollama pull qwen2.5-coder:14b` |
| DeepSeek R1 | 5GB | Reasoning | `ollama pull deepseek-r1:8b` |
| Gemma 2 | 5GB | Google's open model | `ollama pull gemma2` |

---

## Documentation

- [Demo Guide](DEMO-HELP.md) - How to run and test
- [Changelog](CHANGELOG.md) - Version history
- [Main README](README.md) - Project overview

---

## System Requirements

### For PWA
- Modern web browser (Chrome, Firefox, Edge, Safari)
- No installation required

### For Java JAR
- Java 21 or higher
- 4GB+ RAM for local models
- Windows, macOS, or Linux

### For Local AI (Ollama)
- 8GB+ RAM recommended
- 5-20GB disk space per model
- GPU acceleration optional but recommended (NVIDIA CUDA)

---

## License

This project is open source under MIT License.

## Support

- Issues: https://github.com/nrupala/simplicity/issues
- Discussions: https://github.com/nrupala/simplicity/discussions
