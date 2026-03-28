# Simplicity Model Binder Demo - User Guide

## What This Demo Shows

This demonstrates that **local AI models work on your laptop** using the Simplicity model binder, supporting:
- **Ollama** - Local models (Llama 3, Mistral, Phi-3, etc.)
- **LM Studio** - GGUF format models (quantized, efficient)
- **Cloud Providers** - OpenAI, Anthropic, Google, etc.

---

## Quick Start

### Prerequisites

1. **Java 21** - Download from [Adoptium](https://adoptium.net/)
2. **Maven 3.9+** - Download from [Apache Maven](https://maven.apache.org/download.cgi)
3. **Ollama** (optional) - Download from [ollama.ai](https://ollama.ai)
4. **LM Studio** (optional) - Download from [lmstudio.ai](https://lmstudio.ai)

### Run the Demo

```powershell
# Set Java home (adjust path if different)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin;$env:PATH"

# Navigate to project
cd C:\Users\YOUR_USER\simplicity

# First time: Install parent POM
mvn install -N

# Build and install modules
mvn install -pl simplicity-core,simplicity-model-binder -DskipTests

# Run the demo
mvn exec:java -pl simplicity-model-binder -Dexec.mainClass="com.simplicity.model.binder.ModelBinderDemo"
```

Or use the provided scripts:
```powershell
.\install-parent.ps1
.\full-build.ps1
.\run-demo.ps1
```

---

## Expected Output

### Success - Ollama Connected

```
============================================================
  SIMPLICITY MODEL BINDER - LIVE DEMO
============================================================

[1] Checking available providers...
  Local Providers:
    - Ollama (http://localhost:11434)
    - LM Studio (http://localhost:1234)

[2] Testing Ollama Provider...
----------------------------------------
  Health Check: ✓ CONNECTED
  Latency: 142.0ms
  Discovered 6 local models:
    • deepseek-r1:8b (Local)
    • nomic-embed-text (Local)
    • qwen2.5-coder:14b (Local)
    ... and 3 more
```

### Success - LM Studio Connected

```
[3] Testing LM Studio Provider...
----------------------------------------
  Health Check: ✓ CONNECTED
  Latency: 85.0ms
  Discovered models:
    • LFM2.5-1.2B-Instruct-Q8_0 (Local)
```

---

## How to Test Each Provider

### 1. Ollama (Recommended - Free, No API Key)

```powershell
# Install Ollama from https://ollama.ai

# Pull a model (one-time)
ollama pull llama3
ollama pull deepseek-r1:8b
ollama pull qwen2.5-coder:14b

# Verify Ollama is running
ollama list

# Should show your downloaded models
```

### 2. LM Studio

```powershell
# Install LM Studio from https://lmstudio.ai

# 1. Download a model (e.g., from HuggingFace)
# 2. Load the model in LM Studio UI
# 3. Click "Start Server" (usually port 1234)

# Verify it's running - should show in demo output
```

### 3. Cloud Providers (Require API Keys)

Set environment variables before running:
```powershell
$env:OPENAI_API_KEY = "sk-..."
$env:ANTHROPIC_API_KEY = "sk-ant-..."
$env:GOOGLE_API_KEY = "..."

# Then run the demo
.\run-demo.ps1
```

---

## Verification Checklist

- [ ] Demo runs without errors
- [ ] Ollama shows "CONNECTED" (if installed and running)
- [ ] Latency is reasonable (< 500ms)
- [ ] Models are discovered and listed
- [ ] No "FAILED" status for active providers

---

## Troubleshooting

### "Connection failed" for Ollama

```powershell
# Check if Ollama is running
ollama list

# If not running, start it
ollama serve

# In another terminal, pull a model
ollama pull llama3
```

### "Connection failed" for LM Studio

```powershell
# Open LM Studio
# 1. Click "Model" and select a model
# 2. Click "Load Model" (wait for it to load)
# 3. Click "Start Server" (bottom left)
# 4. Verify "Server Running on port 1234" message
```

### "Could not find artifact" errors

```powershell
# Reinstall parent POM
.\install-parent.ps1

# Clean and rebuild
mvn clean install -pl simplicity-core,simplicity-model-binder -DskipTests
```

---

## GitHub Pages Integration

### Option 1: Screenshots & Demo Video (Recommended)

1. Run the demo and take screenshots of successful output
2. Record a short video showing the models being discovered
3. Embed in your GitHub Pages site:

```html
<h2>Local AI Demo</h2>
<img src="screenshots/demo-ollama-connected.png" alt="Ollama connected" />
<img src="screenshots/demo-models-discovered.png" alt="Models discovered" />
<video controls>
  <source src="videos/local-ai-demo.mp4" type="video/mp4">
</video>
```

### Option 2: Live Demo with Backend

For a truly interactive GitHub Pages demo:

1. **Create a simple REST API** with the model binder:
```java
@RestController
public class ModelDemoController {
    
    @GetMapping("/api/demo/ollama-status")
    public Map<String, Object> getOllamaStatus() {
        ModelBinder binder = new ModelBinder();
        ModelBinder.ModelProvider ollama = binder.ollama("http://localhost:11434");
        ModelBinder.ProviderHealth health = ollama.healthCheck();
        return Map.of(
            "connected", health.isHealthy(),
            "latency", health.avgLatencyMs(),
            "models", ollama.getAvailableModels().stream()
                .map(ModelBinder.ModelInfo::displayName)
                .toList()
        );
    }
}
```

2. **Host on a free tier** (Railway, Render, Fly.io)
3. **Embed in GitHub Pages** with iframe or fetch API

### Option 3: Static Demo Output

Show what the demo produces without running it:

```html
<div class="demo-output">
  <h3>Ollama Connection Test</h3>
  <pre>
Health Check: ✓ CONNECTED
Latency: 142.0ms
Models Available:
  • llama3 (4.7GB)
  • deepseek-r1:8b (4.9GB)  
  • qwen2.5-coder:14b (8.5GB)
  • mistral (4.1GB)
  </pre>
</div>
```

---

## Project Structure

```
simplicity/
├── pom.xml                    # Parent POM
├── simplicity-core/          # Core domain models
│   └── src/main/java/com/simplicity/core/
│       ├── domain/
│       │   ├── DomainModels.java
│       │   ├── QueryModels.java
│       │   └── UserCustomization.java
├── simplicity-model-binder/  # Model bindings
│   └── src/main/java/com/simplicity/model/binder/
│       ├── ModelBinder.java       # Main connector
│       ├── OllamaProvider.java    # Ollama integration
│       ├── LMStudioProvider.java  # LM Studio integration
│       ├── OpenAIProvider.java    # GPT-4, o1
│       ├── AnthropicProvider.java # Claude
│       ├── GoogleProvider.java    # Gemini
│       └── ModelBinderDemo.java  # Demo/test class
├── install-parent.ps1        # Install parent POM
├── full-build.ps1            # Build all modules
└── run-demo.ps1             # Run the demo
```

---

## Supported Models

### Ollama (20+ models)
- **Llama 3 / 3.1 / 3.2** - Meta's open models
- **Mistral / Mixtral** - Efficient & MoE
- **Phi-3** - Microsoft's efficient model
- **Qwen 2 / Qwen 2.5 Coder** - Alibaba's models
- **Gemma 2** - Google's open model
- **DeepSeek Coder V2** - Code-specialized
- **Code Llama** - Meta's code model
- **Command R+** - Cohere's research model
- **WizardLM 2** - High-quality instruction

### LM Studio
Any GGUF format model including:
- LFM (LLaMA Factory Models)
- Mistral variants
- Phi variants
- Custom downloaded models

---

## Next Steps

1. **Try different models** - Pull several Ollama models and see them all
2. **Test chat** - The demo shows discovery; extend it to test actual chat
3. **Add API keys** - Test cloud providers with real API keys
4. **Build your app** - Use the model binder in your Simplicity application

---

## Support

- **Issues**: https://github.com/YOUR_USERNAME/simplicity/issues
- **Documentation**: https://github.com/YOUR_USERNAME/simplicity#readme
