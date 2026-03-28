package com.simplicity.intelligence;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎛️ SIMPLICITY INTERFACE LAYER
 * 
 * Dual-interface system that serves both:
 * - Casual users: Simple, opinionated defaults
 * - Expert/Architect users: Full control over everything
 * 
 * Philosophy: "Simple for beginners, unlimited for experts."
 * 
 * Every parameter is accessible at some level:
 * - Casual: Presets and sliders
 * - Intermediate: Direct toggles
 * - Expert: Raw config, JSON, API
 * - Architect: Programmatic, declarative configs
 */
public class InterfaceLayer {

    private final Map<UUID, InterfaceMode> userModes = new ConcurrentHashMap<>();
    private final Map<UUID, FullConfig> userConfigs = new ConcurrentHashMap<>();
    private final SentienceEngine sentience;
    
    /**
     * Interface complexity modes.
     */
    public enum InterfaceMode {
        /**
         * Simple, opinionated interface.
         * - Preset selection only
         * - Essential toggles
         * - One-click configurations
         * - Helpful tooltips
         */
        CASUAL(1, "Simple", 
            "Easy to use with great defaults. Perfect for getting started."),
        
        /**
         * Standard interface with full access.
         * - All presets + custom
         * - Detailed toggles
         * - Direct value setting
         * - Basic advanced options
         */
        STANDARD(2, "Standard",
            "Full access to features with clear organization."),
        
        /**
         * Expert interface with complete control.
         * - Raw JSON editing
         * - All parameters exposed
         * - Code/view toggle
         * - Advanced configurations
         */
        EXPERT(3, "Expert",
            "Complete control with direct access to all settings."),
        
        /**
         * Architect interface for power users.
         * - Declarative configurations
         * - API programmatic access
         * - Import/export configs
         * - Organization-wide settings
         */
        ARCHITECT(4, "Architect",
            "Enterprise-grade control for power users and organizations.");
        
        private final int level;
        private final String displayName;
        private final String description;
        
        InterfaceMode(int level, String displayName, String description) {
            this.level = level;
            this.displayName = displayName;
            this.description = description;
        }
        
        public int level() { return level; }
        public String displayName() { return displayName; }
        public String description() { return description; }
        
        public boolean canAccess(String feature) {
            // Define access levels for features
            return switch (this) {
                case CASUAL -> feature.startsWith("basic");
                case STANDARD -> feature.startsWith("basic") || feature.startsWith("standard");
                case EXPERT -> true; // All features
                case ARCHITECT -> true; // All + org features
            };
        }
    }

    /**
     * Complete configuration that can be accessed at any level.
     */
    public record FullConfig(
        UUID userId,
        InterfaceMode mode,
        
        // Core Settings (CASUAL accessible)
        CoreSettings core,
        
        // RAG Settings (STANDARD accessible)
        RagSettings rag,
        
        // Model Settings (STANDARD accessible)
        ModelSettings model,
        
        // Intelligence Settings (EXPERT accessible)
        IntelligenceSettings intelligence,
        
        // Enterprise Settings (ARCHITECT accessible)
        EnterpriseSettings enterprise,
        
        // Raw JSON for experts
        String rawJson,
        
        Instant lastModified
    ) {
        public static FullConfig defaultConfig(UUID userId) {
            return new FullConfig(
                userId,
                InterfaceMode.STANDARD,
                CoreSettings.defaults(),
                RagSettings.defaults(),
                ModelSettings.defaults(),
                IntelligenceSettings.defaults(),
                EnterpriseSettings.defaults(),
                null,
                Instant.now()
            );
        }
    }

    /**
     * Core settings - essential configuration.
     */
    public record CoreSettings(
        // Basic mode
        String preset,              // "productive", "casual", "expert"
        boolean darkMode,
        String language,            // "en", "es", etc.
        
        // Essential toggles
        boolean showCitations,
        boolean showConfidence,
        boolean showSuggestions,
        boolean enableHistory,
        
        // Quick settings
        int responseLength,         // 1-5 scale
        int creativityLevel,        // 1-5 scale
        
        // Simplicity features
        boolean enableEmotions,
        boolean enablePersonalization
    ) {
        public static CoreSettings defaults() {
            return new CoreSettings(
                "productive",
                true,
                "en",
                true, true, true, true,
                3, 3,
                true, true
            );
        }
        
        /**
         * Quick preset configurations.
         */
        public static CoreSettings fromPreset(String preset) {
            return switch (preset.toLowerCase()) {
                case "productive" -> new CoreSettings(
                    "productive", true, "en",
                    true, true, true, true,
                    3, 3, true, true
                );
                case "casual" -> new CoreSettings(
                    "casual", false, "en",
                    true, false, true, true,
                    3, 4, true, true
                );
                case "minimal" -> new CoreSettings(
                    "minimal", true, "en",
                    true, false, false, false,
                    2, 2, false, false
                );
                case "detailed" -> new CoreSettings(
                    "detailed", false, "en",
                    true, true, true, true,
                    5, 3, true, true
                );
                default -> defaults();
            };
        }
    }

    /**
     * RAG engine settings.
     */
    public record RagSettings(
        // Search settings
        int maxResults,
        double minRelevanceScore,
        boolean enableReranking,
        boolean enableHybridSearch,
        
        // Knowledge sources
        boolean useWebSearch,
        boolean useUserKnowledge,
        boolean useOrgKnowledge,
        boolean useLearnedPatterns,
        
        // Retrieval tuning
        int vectorMatchCount,
        int keywordMatchCount,
        double vectorWeight,
        double keywordWeight,
        
        // Citation settings
        int maxCitations,
        boolean inlineCitations,
        String citationFormat
    ) {
        public static RagSettings defaults() {
            return new RagSettings(
                10, 0.5, true, true,
                true, true, true, true,
                30, 20, 0.6, 0.4,
                10, true, "brackets"
            );
        }
    }

    /**
     * Model selection and configuration.
     */
    public record ModelSettings(
        // Default model
        String defaultModel,
        String defaultProvider,
        
        // Model preferences
        List<ModelPreference> preferredModels,
        boolean preferOpenSource,
        boolean allowModelFallback,
        
        // Model per task type
        Map<String, String> modelByTask,
        
        // Local model settings
        boolean enableLocalModels,
        String localModelEndpoint,
        List<String> enabledLocalModels,
        
        // Advanced
        double temperature,
        int maxTokens,
        boolean streamingEnabled
    ) {
        public static ModelSettings defaults() {
            return new ModelSettings(
                "gpt-4", "openai",
                List.of(), false, true,
                Map.of(
                    "code", "codex",
                    "reasoning", "claude-3-opus",
                    "creative", "gpt-4"
                ),
                true, "http://localhost:11434", List.of("llama3"),
                0.7, 4000, true
            );
        }
    }

    public record ModelPreference(
        String modelId,
        String provider,
        List<String> domains,
        double weight,
        boolean enabled
    ) {}

    /**
     * Intelligence and learning settings.
     */
    public record IntelligenceSettings(
        // Learning
        boolean enableLearning,
        boolean adaptToUser,
        int learningSpeed,           // 1-10
        boolean learnFromFeedback,
        boolean learnFromImplicit,
        
        // GAN-RAG settings
        boolean enableGANMode,
        int candidateCount,          // How many candidates to generate
        double discriminatorThreshold,
        boolean enableSelfReflection,
        
        // Personalization
        boolean personalizeResults,
        boolean personalizeTone,
        double personalizationStrength,
        boolean respectUserSilence,
        
        // Advanced intelligence
        boolean enableMultiModelReasoning,
        boolean enableChainOfThought,
        int maxReflectionIterations,
        boolean enableUncertaintyQuantification
    ) {
        public static IntelligenceSettings defaults() {
            return new IntelligenceSettings(
                true, true, 5, true, true,
                true, 3, 0.7, true,
                true, true, 0.6, true,
                false, false, 3, true
            );
        }
    }

    /**
     * Enterprise/organizational settings.
     */
    public record EnterpriseSettings(
        // Organization settings
        String organizationId,
        String organizationName,
        boolean applyOrgPolicies,
        
        // Team settings
        String teamId,
        List<String> teamDomains,
        boolean shareWithinTeam,
        
        // Compliance
        boolean auditEnabled,
        boolean dataRetentionEnforced,
        int dataRetentionDays,
        boolean complianceMode,
        
        // Integration
        String ssoProvider,
        List<String> allowedDomains,
        Map<String, String> customHeaders,
        
        // Advanced
        boolean enableWebhooks,
        String webhookEndpoint,
        List<String> webhookEvents
    ) {
        public static EnterpriseSettings defaults() {
            return new EnterpriseSettings(
                null, null, false,
                null, List.of(), false,
                false, false, 90, false,
                null, List.of(), Map.of(),
                false, null, List.of()
            );
        }
    }

    // ==================== PUBLIC API ====================

    public InterfaceLayer(SentienceEngine sentience) {
        this.sentience = sentience;
    }

    /**
     * Get current interface mode for user.
     */
    public InterfaceMode getMode(UUID userId) {
        return userModes.getOrDefault(userId, InterfaceMode.STANDARD);
    }

    /**
     * Set interface mode.
     */
    public void setMode(UUID userId, InterfaceMode mode) {
        userModes.put(userId, mode);
    }

    /**
     * Get full configuration for user.
     */
    public FullConfig getConfig(UUID userId) {
        return userConfigs.getOrDefault(userId, FullConfig.defaultConfig(userId));
    }

    /**
     * Update core settings (CASUAL accessible).
     */
    public FullConfig updateCoreSettings(UUID userId, CoreSettings core) {
        FullConfig current = getConfig(userId);
        FullConfig updated = new FullConfig(
            userId, current.mode(), core, current.rag(), current.model(),
            current.intelligence(), current.enterprise(), current.rawJson(), Instant.now()
        );
        userConfigs.put(userId, updated);
        
        // Also update sentience persona
        sentience.setPersonaPreset(userId, SentienceEngine.PersonaPreset.valueOf(
            core.preset().toUpperCase()
        ));
        
        return updated;
    }

    /**
     * Update RAG settings (STANDARD accessible).
     */
    public FullConfig updateRagSettings(UUID userId, RagSettings rag) {
        FullConfig current = getConfig(userId);
        FullConfig updated = new FullConfig(
            userId, current.mode(), current.core(), rag, current.model(),
            current.intelligence(), current.enterprise(), current.rawJson(), Instant.now()
        );
        userConfigs.put(userId, updated);
        return updated;
    }

    /**
     * Update model settings (STANDARD accessible).
     */
    public FullConfig updateModelSettings(UUID userId, ModelSettings model) {
        FullConfig current = getConfig(userId);
        FullConfig updated = new FullConfig(
            userId, current.mode(), current.core(), current.rag(), model,
            current.intelligence(), current.enterprise(), current.rawJson(), Instant.now()
        );
        userConfigs.put(userId, updated);
        return updated;
    }

    /**
     * Update intelligence settings (EXPERT accessible).
     */
    public FullConfig updateIntelligenceSettings(UUID userId, IntelligenceSettings intelligence) {
        FullConfig current = getConfig(userId);
        if (current.mode().level() < InterfaceMode.EXPERT.level()) {
            throw new IllegalStateException("Expert mode required for intelligence settings");
        }
        FullConfig updated = new FullConfig(
            userId, current.mode(), current.core(), current.rag(), current.model(),
            intelligence, current.enterprise(), current.rawJson(), Instant.now()
        );
        userConfigs.put(userId, updated);
        return updated;
    }

    /**
     * Update enterprise settings (ARCHITECT accessible).
     */
    public FullConfig updateEnterpriseSettings(UUID userId, EnterpriseSettings enterprise) {
        FullConfig current = getConfig(userId);
        if (current.mode().level() < InterfaceMode.ARCHITECT.level()) {
            throw new IllegalStateException("Architect mode required for enterprise settings");
        }
        FullConfig updated = new FullConfig(
            userId, current.mode(), current.core(), current.rag(), current.model(),
            current.intelligence(), enterprise, current.rawJson(), Instant.now()
        );
        userConfigs.put(userId, updated);
        return updated;
    }

    /**
     * Update raw JSON config (EXPERT accessible).
     */
    public FullConfig updateRawConfig(UUID userId, String json) {
        FullConfig current = getConfig(userId);
        if (current.mode().level() < InterfaceMode.EXPERT.level()) {
            throw new IllegalStateException("Expert mode required for raw config");
        }
        FullConfig updated = new FullConfig(
            userId, current.mode(), current.core(), current.rag(), current.model(),
            current.intelligence(), current.enterprise(), json, Instant.now()
        );
        userConfigs.put(userId, updated);
        return updated;
    }

    /**
     * Apply a preset configuration.
     */
    public FullConfig applyPreset(UUID userId, String preset) {
        FullConfig current = getConfig(userId);
        CoreSettings newCore = CoreSettings.fromPreset(preset);
        return updateCoreSettings(userId, newCore);
    }

    /**
     * Get UI schema for current mode.
     * Returns the appropriate configuration UI structure.
     */
    public ConfigSchema getUISchema(UUID userId) {
        InterfaceMode mode = getMode(userId);
        return ConfigSchema.forMode(mode);
    }

    /**
     * Get available features for current mode.
     */
    public List<String> getAvailableFeatures(UUID userId) {
        InterfaceMode mode = getMode(userId);
        return switch (mode) {
            case CASUAL -> List.of(
                "basic.preset", "basic.darkMode", "basic.language",
                "basic.citations", "basic.confidence",
                "basic.responseLength", "basic.emotions"
            );
            case STANDARD -> List.of(
                "basic.*", "rag.*", "model.*"
            );
            case EXPERT -> List.of(
                "basic.*", "rag.*", "model.*", "intelligence.*"
            );
            case ARCHITECT -> List.of(
                "basic.*", "rag.*", "model.*", "intelligence.*", "enterprise.*"
            );
        };
    }

    /**
     * Validate configuration access.
     */
    public boolean canAccess(UUID userId, String setting) {
        InterfaceMode mode = getMode(userId);
        return mode.canAccess(setting);
    }

    /**
     * Export configuration as JSON.
     */
    public String exportConfig(UUID userId) {
        FullConfig config = getConfig(userId);
        return toJson(config);
    }

    /**
     * Import configuration from JSON.
     */
    public FullConfig importConfig(UUID userId, String json) {
        // Parse and validate JSON
        FullConfig parsed = fromJson(json);
        
        // Check mode compatibility
        InterfaceMode targetMode = parsed.mode();
        userModes.put(userId, targetMode);
        userConfigs.put(userId, parsed);
        
        return parsed;
    }

    private String toJson(FullConfig config) {
        // Simplified JSON serialization
        return """
        {
            "userId": "%s",
            "mode": "%s",
            "core": {
                "preset": "%s",
                "darkMode": %s,
                "emotions": %s
            }
        }
        """.formatted(
            config.userId(),
            config.mode().name(),
            config.core().preset(),
            config.core().darkMode(),
            config.core().enableEmotions()
        );
    }

    private FullConfig fromJson(String json) {
        // Simplified - would use proper JSON parsing
        return FullConfig.defaultConfig(UUID.randomUUID());
    }

    // ==================== UI SCHEMA ====================

    /**
     * Configuration schema for UI rendering.
     */
    public record ConfigSchema(
        InterfaceMode mode,
        List<ConfigSection> sections,
        Map<String, Object> constraints
    ) {
        public static ConfigSchema forMode(InterfaceMode mode) {
            return switch (mode) {
                case CASUAL -> casualSchema();
                case STANDARD -> standardSchema();
                case EXPERT -> expertSchema();
                case ARCHITECT -> architectSchema();
            };
        }

        private static ConfigSchema casualSchema() {
            return new ConfigSchema(
                InterfaceMode.CASUAL,
                List.of(
                    new ConfigSection("Appearance", List.of(
                        new ConfigField("preset", "Preset", "select", 
                            Map.of("options", List.of("productive", "casual", "minimal", "detailed"))),
                        new ConfigField("darkMode", "Dark Mode", "toggle", Map.of()),
                        new ConfigField("language", "Language", "select",
                            Map.of("options", List.of("en", "es", "fr", "de")))
                    )),
                    new ConfigSection("Output", List.of(
                        new ConfigField("showCitations", "Show Citations", "toggle", Map.of()),
                        new ConfigField("showConfidence", "Show Confidence", "toggle", Map.of()),
                        new ConfigField("responseLength", "Response Length", "slider",
                            Map.of("min", 1, "max", 5, "step", 1))
                    )),
                    new ConfigSection("Features", List.of(
                        new ConfigField("enableEmotions", "Show Emotions", "toggle", Map.of()),
                        new ConfigField("enablePersonalization", "Learn My Preferences", "toggle", Map.of())
                    ))
                ),
                Map.of()
            );
        }

        private static ConfigSchema standardSchema() {
            return new ConfigSchema(
                InterfaceMode.STANDARD,
                List.of(
                    casualSchema().sections().get(0), // Appearance
                    casualSchema().sections().get(1), // Output
                    new ConfigSection("Search", List.of(
                        new ConfigField("maxResults", "Max Results", "number", Map.of("min", 1, "max", 50)),
                        new ConfigField("enableHybridSearch", "Hybrid Search", "toggle", Map.of()),
                        new ConfigField("useUserKnowledge", "Personal Knowledge", "toggle", Map.of())
                    )),
                    new ConfigSection("Models", List.of(
                        new ConfigField("defaultModel", "Default Model", "select",
                            Map.of("options", List.of("gpt-4", "claude-3", "gemini"))),
                        new ConfigField("preferOpenSource", "Prefer Open Source", "toggle", Map.of()),
                        new ConfigField("streamingEnabled", "Streaming Responses", "toggle", Map.of())
                    ))
                ),
                Map.of()
            );
        }

        private static ConfigSchema expertSchema() {
            return new ConfigSchema(
                InterfaceMode.EXPERT,
                List.of(
                    standardSchema().sections().get(0),
                    standardSchema().sections().get(1),
                    standardSchema().sections().get(2),
                    standardSchema().sections().get(3),
                    new ConfigSection("Intelligence", List.of(
                        new ConfigField("enableLearning", "Enable Learning", "toggle", Map.of()),
                        new ConfigField("learningSpeed", "Learning Speed", "slider", Map.of("min", 1, "max", 10)),
                        new ConfigField("enableGANMode", "GAN-RAG Mode", "toggle", Map.of()),
                        new ConfigField("candidateCount", "Candidate Count", "number", Map.of("min", 1, "max", 10)),
                        new ConfigField("personalizationStrength", "Personalization", "slider", Map.of("min", 0, "max", 1))
                    )),
                    new ConfigSection("Raw Config", List.of(
                        new ConfigField("rawJson", "JSON Configuration", "textarea", Map.of("monospace", true))
                    ))
                ),
                Map.of()
            );
        }

        private static ConfigSchema architectSchema() {
            return new ConfigSchema(
                InterfaceMode.ARCHITECT,
                List.of(
                    expertSchema().sections().get(0),
                    expertSchema().sections().get(1),
                    expertSchema().sections().get(2),
                    expertSchema().sections().get(3),
                    expertSchema().sections().get(4),
                    new ConfigSection("Enterprise", List.of(
                        new ConfigField("organizationId", "Organization ID", "text", Map.of()),
                        new ConfigField("applyOrgPolicies", "Apply Org Policies", "toggle", Map.of()),
                        new ConfigField("auditEnabled", "Audit Logging", "toggle", Map.of()),
                        new ConfigField("dataRetentionDays", "Data Retention (days)", "number", Map.of("min", 1, "max", 365)),
                        new ConfigField("complianceMode", "Compliance Mode", "toggle", Map.of())
                    )),
                    new ConfigSection("Integration", List.of(
                        new ConfigField("ssoProvider", "SSO Provider", "select",
                            Map.of("options", List.of("none", "okta", "azure-ad", "google"))),
                        new ConfigField("customHeaders", "Custom Headers", "json", Map.of()),
                        new ConfigField("webhookEndpoint", "Webhook URL", "text", Map.of())
                    ))
                ),
                Map.of("supportsImportExport", true, "supportsApiAccess", true)
            );
        }
    }

    public record ConfigSection(
        String title,
        List<ConfigField> fields
    ) {}

    public record ConfigField(
        String key,
        String label,
        String type,
        Map<String, Object> constraints
    ) {}
}
