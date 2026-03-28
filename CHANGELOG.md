# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release structure
- Simplicity Model Binder module with support for:
  - Ollama (local models)
  - LM Studio (GGUF models)
  - OpenAI (GPT-4, o1)
  - Anthropic (Claude)
  - Google (Gemini)
  - HuggingFace
  - Groq
  - Together AI
  - Azure OpenAI

### Changed
- Core domain models (DomainModels, QueryModels, UserCustomization)

### Fixed
- Sealed class compilation issues in core modules

### Features
- Universal model connector (ModelBinder)
- Local AI support for offline use
- Streaming response support
- Health check endpoints for all providers

## [1.0.0] - 2026-03-28

### Added
- Model Binder Demo - Live demonstration of local AI connectivity
- Cross-platform support:
  - Windows (.exe, .msi)
  - macOS (.app, .pkg)
  - Linux (.deb, .AppImage)
  - Web (PWA)
  - Android (APK)
- GitHub Actions CI/CD for automated releases
- Semantic versioning with auto-bump
- Auto-update configuration

### Model Support
- **Local (Ollama)**: 20+ models including Llama 3, Mistral, Phi-3, Qwen 2
- **Local (LM Studio)**: GGUF format models
- **Cloud**: All major AI providers

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2026-03-28 | Initial release with model binder |
| 0.1.0-SNAPSHOT | 2026-03-28 | Development snapshot |
