package com.simplicity.model.registry;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Model Registry domain models.
 * 
 * Central registry for all AI models available in Simplicity.
 * Includes:
 * - Model metadata (name, version, provider)
 * - Activation and deprecation dates
 * - Capability tags for routing
 * - User preferences for model selection
 */
public sealed class ModelRegistry permits 
        AIModel, ModelProvider, ModelCapability, UserModelPreference, ModelRegistryEntry {

    /**
     * An AI model in the registry.
     */
    public record AIModel(
        UUID id,
        String name,
        String version,
        ProviderType provider,
        ModelCategory category,
        List<ModelCapability> capabilities,
        ModelSpec spec,
        Instant addedAt,
        Instant activatedAt,
        Instant deprecatedAt,
        ModelStatus status,
        String description,
        String documentationUrl,
        String endpoint,
        double defaultTemperature,
        int maxTokens,
        int contextWindow
    ) {
        /**
         * Model specification details.
         */
        public record ModelSpec(
            int parameters,           // billions
            String architecture,      // e.g., "Transformer", "MixtureOfExperts"
            String precision,         // e.g., "FP16", "FP8", "BF16"
            boolean isOpenSource,
            String license,
            List<String> trainingData
        ) {}

        /**
         * Check if model is currently active.
         */
        public boolean isActive() {
            Instant now = Instant.now();
            return status == ModelStatus.ACTIVE &&
                   (activatedAt == null || activatedAt.isBefore(now)) &&
                   (deprecatedAt == null || deprecatedAt.isAfter(now));
        }

        /**
         * Check if model supports a capability.
         */
        public boolean hasCapability(String capability) {
            return capabilities.stream()
                .anyMatch(c -> c.name().equalsIgnoreCase(capability) ||
                             c.tags().contains(capability.toLowerCase()));
        }
    }

    /**
     * Model provider types.
     */
    public enum ProviderType {
        // Proprietary Providers
        OPENAI("OpenAI", "https://api.openai.com", ProviderTier.PREMIUM),
        ANTHROPIC("Anthropic", "https://api.anthropic.com", ProviderTier.PREMIUM),
        GOOGLE("Google AI", "https://generativelanguage.googleapis.com", ProviderTier.PREMIUM),
        META("Meta AI", "https://llama.meta.com", ProviderTier.PREMIUM),
        XAI("xAI", "https://api.x.ai", ProviderTier.PREMIUM),
        MISTRAL("Mistral AI", "https://api.mistral.ai", ProviderTier.PREMIUM),
        COHERE("Cohere", "https://api.cohere.ai", ProviderTier.PREMIUM),
        
        // Open Source Providers
        HUGGING_FACE("Hugging Face", "https://api-inference.huggingface.co", ProviderTier.OPEN),
        OLLAMA("Ollama (Local)", "http://localhost:11434", ProviderTier.OPEN),
        GROQ("Groq", "https://api.groq.com", ProviderTier.OPEN),
        TOGETHER("Together AI", "https://api.together.ai", ProviderTier.OPEN),
        ANTHROPIC_BEDROCK("Anthropic (Bedrock)", "AWS Bedrock", ProviderTier.PREMIUM),
        OPENAI_AZURE("Azure OpenAI", "Azure OpenAI Service", ProviderTier.ENTERPRISE),
        GITHUB("GitHub Models", "https://models.inference.ai.github.com", ProviderTier.OPEN),
        DEEPSEEK("DeepSeek", "https://api.deepseek.com", ProviderTier.OPEN),
        QWEN("Qwen (Alibaba)", "https://dashscope.aliyuncs.com", ProviderTier.OPEN),
        GROK_LOCAL("Grok (Local)", "Local Deployment", ProviderTier.OPEN),
        LLAMA_LOCAL("Llama (Local)", "Local Deployment", ProviderTier.OPEN),
        MISTRAL_LOCAL("Mistral (Local)", "Local Deployment", ProviderTier.OPEN),
        Gemma("Google Gemma", "https://huggingface.co/google", ProviderTier.OPEN),
        Phi("Microsoft Phi", "https://huggingface.co/microsoft", ProviderTier.OPEN),
        StableLM("Stability AI StableLM", "https://huggingface.co/stabilityai", ProviderTier.OPEN),
        Falcon("Falcon (TII)", "https://huggingface.co/tiiuae", ProviderTier.OPEN),
        MPT("MosaicML MPT", "https://huggingface.co/mosaicml", ProviderTier.OPEN),
        WizardLM("WizardLM", "https://huggingface.co/WizardLM", ProviderTier.OPEN),
        Vicuna("Vicuna", "https://huggingface.co/lmsys", ProviderTier.OPEN),
        Orca("Microsoft Orca", "https://huggingface.co/microsoft", ProviderTier.OPEN),
        Yi("Yi (01.AI)", "https://huggingface.co/01-ai", ProviderTier.OPEN),
        InternLM("InternLM", "https://huggingface.co/internlm", ProviderTier.OPEN),
        Qwen2("Qwen2", "https://huggingface.co/Qwen", ProviderTier.OPEN),
        GLM("ChatGLM", "https://huggingface.co/THUDM", ProviderTier.OPEN),
        Phi3("Microsoft Phi-3", "https://huggingface.co/microsoft", ProviderTier.OPEN),
        Gemma2("Google Gemma 2", "https://huggingface.co/google", ProviderTier.OPEN),
        Llama3_1("Meta Llama 3.1", "https://huggingface.co/meta-llama", ProviderTier.OPEN),
        Llama3_2("Meta Llama 3.2", "https://huggingface.co/meta-llama", ProviderTier.OPEN),
        Mistral_Nemo("Mistral Nemo", "https://huggingface.co/mistralai", ProviderTier.OPEN),
        Mistral_Pixtral("Mistral Pixtral", "https://huggingface.co/mistralai", ProviderTier.OPEN),
        Command("Cohere Command", "https://cohere.ai", ProviderTier.OPEN);

        private final String displayName;
        private final String baseUrl;
        private final ProviderTier tier;

        ProviderType(String displayName, String baseUrl, ProviderTier tier) {
            this.displayName = displayName;
            this.baseUrl = baseUrl;
            this.tier = tier;
        }

        public String displayName() { return displayName; }
        public String baseUrl() { return baseUrl; }
        public ProviderTier tier() { return tier; }

        public boolean isOpenSource() {
            return tier == ProviderTier.OPEN;
        }
    }

    public enum ProviderTier {
        OPEN,       // Fully open source, free to use
        PREMIUM,    // Proprietary, paid API
        ENTERPRISE // Enterprise/custom deployment
    }

    /**
     * Model categories for classification.
     */
    public enum ModelCategory {
        // General Purpose
        GENERAL_LLM("General LLM", "Balanced capabilities for most tasks"),
        REASONING("Reasoning", "Advanced chain-of-thought and reasoning"),
        CODE("Code", "Code generation and understanding"),
        MULTIMODAL("Multimodal", "Text + image/video/audio understanding"),
        EMBEDDING("Embedding", "Vector embeddings for RAG"),
        RERANKING("Reranking", "Document reranking and relevance"),
        
        // Specialized
        RESEARCH("Research", "Academic and scientific research"),
        LEGAL("Legal", "Legal document analysis"),
        MEDICAL("Medical", "Healthcare and medical content"),
        FINANCIAL("Financial", "Financial analysis and reporting"),
        CREATIVE("Creative", "Creative writing and generation"),
        
        // Domain-Specific
        MATH("Mathematics", "Mathematical reasoning and calculation"),
        TRANSLATION("Translation", "Multilingual translation"),
        SUMMARIZATION("Summarization", "Document summarization"),
        QA("Question Answering", "Fact-based question answering"),
        
        // Infrastructure
        FAST("Fast/Chat", "Low latency responses"),
        LARGE_CONTEXT("Large Context", "Extended context windows"),
        INSTRUCTION_FOLLOWING("Instruction Following", "Strict instruction adherence"),
        
        // Local
        LOCAL_SMALL("Local (Small)", "Run locally, resource efficient"),
        LOCAL_LARGE("Local (Large)", "Run locally, high capability");

        private final String displayName;
        private final String description;

        ModelCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String displayName() { return displayName; }
        public String description() { return description; }
    }

    /**
     * Model capabilities/tags for routing.
     */
    public record ModelCapability(
        String name,
        String description,
        List<String> tags,
        double proficiencyScore  // 0-1, how good the model is at this
    ) {
        public static ModelCapability of(String name, String... tags) {
            return new ModelCapability(name, name, List.of(tags), 0.8);
        }
    }

    /**
     * User's preference for a specific model.
     */
    public record UserModelPreference(
        UUID id,
        UUID userId,
        UUID modelId,
        String topicDomain,        // e.g., "machine learning", "legal"
        double preferenceWeight,   // 0-1, how strongly they prefer this
        Instant createdAt,
        Instant lastUsedAt,
        boolean enabled,
        String notes              // e.g., "works best for my research"
    ) {
        /**
         * Create a new preference with defaults.
         */
        public static UserModelPreference create(UUID userId, UUID modelId, String domain) {
            return new UserModelPreference(
                UUID.randomUUID(),
                userId,
                modelId,
                domain,
                0.7,
                Instant.now(),
                Instant.now(),
                true,
                ""
            );
        }

        /**
         * Update with usage.
         */
        public UserModelPreference withUsage() {
            return new UserModelPreference(
                id, userId, modelId, topicDomain, preferenceWeight,
                createdAt, Instant.now(), enabled, notes
            );
        }
    }

    /**
     * Registry entry log (audit trail).
     */
    public record ModelRegistryEntry(
        UUID id,
        UUID modelId,
        String modelName,
        String modelVersion,
        RegistryAction action,
        Instant timestamp,
        String performedBy,    // "system" or user email
        String reason,
        String details
    ) {
        public enum RegistryAction {
            REGISTERED("Model registered"),
            ACTIVATED("Model activated"),
            DEACTIVATED("Model deactivated"),
            DEPRECATED("Model deprecated"),
            VERSION_UPDATED("Model version updated"),
            UPDATED("Model metadata updated"),
            DELETED("Model removed from registry");

            private final String description;
            RegistryAction(String description) {
                this.description = description;
            }
            public String description() { return description; }
        }
    }

    /**
     * Model status.
     */
    public enum ModelStatus {
        REGISTERED,   // In registry, not yet active
        ACTIVE,       // Available for use
        DEPRECATED,   // Being phased out
        RETIRED,      // No longer available
        SUSPENDED     // Temporarily unavailable
    }
}
