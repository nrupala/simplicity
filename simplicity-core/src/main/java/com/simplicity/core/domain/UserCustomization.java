package com.simplicity.core.domain;

import java.time.Instant;
import java.util.*;

/**
 * User Customization domain models.
 * 
 * Provides full user control over:
 * - Context management (persistent and session context)
 * - System intent commands
 * - Response type and format preferences
 * - Context length management
 * - Interface customization across all API layers
 */
public sealed class UserCustomization permits 
        UserContext, IntentCommand, ResponseConfig, InterfaceConfig, CustomFeature {

    /**
     * User context for query personalization.
     * Can be persistent (saved) or session-based (temporary).
     */
    public record UserContext(
        UUID contextId,
        UUID userId,
        ContextType type,
        String title,
        String content,
        List<String> tags,
        Instant createdAt,
        Instant expiresAt,
        boolean isActive
    ) {
        public enum ContextType {
            PERSISTENT("Saved across sessions"),
            SESSION("Temporary, cleared on logout"),
            PROJECT("Tied to a specific project/team"),
            DOCUMENT("Attached to a specific document"),
            CONVERSATION("Part of a conversation thread");

            private final String description;
            ContextType(String description) {
                this.description = description;
            }
            public String description() { return description; }
        }

        /**
         * Create a persistent context.
         */
        public static UserContext persistent(UUID userId, String title, String content) {
            return new UserContext(
                UUID.randomUUID(), userId, ContextType.PERSISTENT,
                title, content, List.of(), Instant.now(), null, true
            );
        }

        /**
         * Create a session context with expiration.
         */
        public static UserContext session(UUID userId, String title, String content, int ttlMinutes) {
            return new UserContext(
                UUID.randomUUID(), userId, ContextType.SESSION,
                title, content, List.of(), Instant.now(), 
                Instant.now().plusSeconds(ttlMinutes * 60L), true
            );
        }

        public boolean isExpired() {
            return expiresAt != null && Instant.now().isAfter(expiresAt);
        }
    }

    /**
     * System intent command for controlling AI behavior.
     */
    public record IntentCommand(
        UUID commandId,
        UUID userId,
        String name,
        String command,          // e.g., "/research", "/code", "/explain"
        String description,
        String systemPrompt,    // The system instruction to inject
        List<String> aliases,
        boolean enabled,
        Map<String, String> parameters,  // Named parameters with defaults
        Instant createdAt,
        int usageCount
    ) {
        /**
         * Built-in intent commands.
         */
        public static class BuiltIn {
            public static final IntentCommand RESEARCH = new IntentCommand(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                null, "research", "/research",
                "Perform deep research with citations",
                "You are a research assistant. Provide thorough, well-cited answers. " +
                "Include sources for every claim. Structure your response with sections.",
                List.of("/deep", "/investigate"),
                true, Map.of(
                    "depth", "comprehensive",
                    "citations", "5-10",
                    "structure", "formal"
                ), Instant.now(), 0
            );

            public static final IntentCommand CODE = new IntentCommand(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                null, "code", "/code",
                "Write, explain, or debug code",
                "You are an expert programmer. Provide clean, well-documented code. " +
                "Include comments explaining complex logic. Mention any caveats or limitations.",
                List.of("/program", "/debug"),
                true, Map.of(
                    "language", "auto",
                    "style", "clean",
                    "include_tests", "true"
                ), Instant.now(), 0
            );

            public static final IntentCommand EXPLAIN = new IntentCommand(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                null, "explain", "/explain",
                "Explain complex topics simply",
                "You are a patient teacher. Break down complex topics into digestible pieces. " +
                "Use analogies and examples. Check understanding before proceeding.",
                List.of("/whatis", "/learn"),
                true, Map.of(
                    "simplicity", "high",
                    "examples", "3",
                    "analogies", "true"
                ), Instant.now(), 0
            );

            public static final IntentCommand COMPARE = new IntentCommand(
                UUID.fromString("00000000-0000-0000-0000-000000000004"),
                null, "compare", "/compare",
                "Compare options or alternatives",
                "You are an analyst. Provide balanced comparisons with pros and cons. " +
                "Use tables for clarity. Include relevant criteria for decision-making.",
                List.of("/versus", "/vs"),
                true, Map.of(
                    "format", "table",
                    "criteria", "5",
                    "recommendation", "optional"
                ), Instant.now(), 0
            );

            public static final IntentCommand SUMMARY = new IntentCommand(
                UUID.fromString("00000000-0000-0000-0000-000000000005"),
                null, "summarize", "/summarize",
                "Summarize documents or topics",
                "You are a summarizer. Provide concise, accurate summaries. " +
                "Include key points and main conclusions. Flag any important caveats.",
                List.of("/short", "/tl;dr"),
                true, Map.of(
                    "length", "brief",
                    "key_points", "5",
                    "conclusion", "true"
                ), Instant.now(), 0
            );

            public static final IntentCommand BRAINSTORM = new IntentCommand(
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                null, "brainstorm", "/brainstorm",
                "Generate creative ideas",
                "You are a creative partner. Generate diverse ideas without judgment. " +
                "Build on previous ideas. Present in an organized, scannable format.",
                List.of("/ideas", "/creative"),
                true, Map.of(
                    "quantity", "10",
                    "categories", "diverse",
                    "wild_ideas", "allowed"
                ), Instant.now(), 0
            );

            public static List<IntentCommand> all() {
                return List.of(RESEARCH, CODE, EXPLAIN, COMPARE, SUMMARY, BRAINSTORM);
            }
        }

        /**
         * Create a custom command.
         */
        public static IntentCommand custom(UUID userId, String name, String command, 
                String description, String systemPrompt) {
            return new IntentCommand(
                UUID.randomUUID(), userId, name, command, description,
                systemPrompt, List.of(), true, Map.of(), Instant.now(), 0
            );
        }
    }

    /**
     * Response configuration for output customization.
     */
    public record ResponseConfig(
        UUID configId,
        UUID userId,
        ResponseType type,
        FormatStyle format,
        LengthPreference length,
        CitationConfig citations,
        LanguageConfig language,
        ToneConfig tone
    ) {
        public enum ResponseType {
            TEXT("Plain text paragraphs"),
            MARKDOWN("Markdown formatted"),
            JSON("Structured JSON"),
            HTML("HTML formatted"),
            XML("XML structured"),
            CODE_BLOCK("Code with syntax highlighting"),
            TABLE("Tabular format"),
            BULLET_POINTS("Bullet list"),
            NUMBERED_LIST("Numbered list"),
            CONVERSATIONAL("Natural conversation"),
            FORMAL_REPORT("Formal report format"),
            PRESENTATION("Slide-ready format"),
            CUSTOM("Custom format defined by user");

            private final String description;
            ResponseType(String description) {
                this.description = description;
            }
            public String description() { return description; }
        }

        public enum FormatStyle {
            CONCISE("Brief and to the point"),
            DETAILED("Comprehensive coverage"),
            STRUCTURED("Headers and sections"),
            FLOWING("Natural paragraphs"),
            HYBRID("Mixed structure")
        }

        public enum LengthPreference {
            SHORT(100, "1-2 sentences"),
            BRIEF(250, "1 paragraph"),
            MEDIUM(500, "2-3 paragraphs"),
            STANDARD(1000, "1 page"),
            LONG(2000, "2-3 pages"),
            COMPREHENSIVE(5000, "Detailed report"),
            UNLIMITED(Integer.MAX_VALUE, "As needed");

            private final int maxTokens;
            private final String description;
            LengthPreference(int maxTokens, String description) {
                this.maxTokens = maxTokens;
                this.description = description;
            }
            public int maxTokens() { return maxTokens; }
            public String description() { return description; }
        }

        /**
         * Citation configuration.
         */
        public record CitationConfig(
            boolean enabled,
            CitationStyle style,
            int maxCitations,
            boolean inlineNumbers,
            boolean hoverPreview,
            boolean showConfidence
        ) {
            public enum CitationStyle {
                BRACKET_NUMBERS("[1], [2]"),
                SUPERSCRIPT("¹, ², ³"),
                FOOTNOTES("See footnote [1]"),
                PARENTHETICAL("(Source: Title)"),
                LINKED("[Title ↗]"),
                NONE("No citations")
            }

            public static CitationConfig DEFAULT = new CitationConfig(
                true, CitationStyle.BRACKET_NUMBERS, 10, true, true, false
            );
        }

        /**
         * Language configuration.
         */
        public record LanguageConfig(
            String language,       // BCP 47: "en", "es", "zh-CN"
            String variant,        // Regional variant
            boolean formalRegister,
            boolean useTechnicalTerms
        ) {
            public static LanguageConfig ENGLISH = new LanguageConfig("en", "US", false, true);
            public static LanguageConfig DEFAULT = ENGLISH;
        }

        /**
         * Tone configuration.
         */
        public record ToneConfig(
            ToneType tone,
            String customInstructions
        ) {
            public enum ToneType {
                PROFESSIONAL("Business-appropriate, objective"),
                CASUAL("Conversational, friendly"),
                ACADEMIC("Scholarly, precise"),
                TECHNICAL("Expert-level detail"),
                SIMPLE("Easy to understand"),
                CREATIVE("Imaginative, engaging"),
                HUMOROUS("Light, with wit"),
                DIRECT("No fluff, straight answers"),
                EMPATHETIC("Understanding, supportive"),
                FORMAL("Ceremonial, proper")
            }

            public static ToneConfig DEFAULT = new ToneConfig(ToneType.PROFESSIONAL, "");
        }

        /**
         * Default configuration.
         */
        public static ResponseConfig DEFAULT = new ResponseConfig(
            null, null,
            ResponseType.MARKDOWN,
            FormatStyle.HYBRID,
            LengthPreference.STANDARD,
            CitationConfig.DEFAULT,
            LanguageConfig.DEFAULT,
            ToneConfig.DEFAULT
        );
    }

    /**
     * Interface customization for API layers.
     */
    public record InterfaceConfig(
        UUID configId,
        UUID userId,
        APIPreferences api,
        UIPreferences ui,
        NotificationConfig notifications,
        PrivacyConfig privacy,
        AccessibilityConfig accessibility
    ) {
        /**
         * API layer preferences.
         */
        public record APIPreferences(
            boolean streamingEnabled,
            boolean batchModeEnabled,
            int maxConcurrentRequests,
            int timeoutSeconds,
            String defaultFormat,
            boolean debugMode,
            Map<String, String> customHeaders
        ) {
            public static APIPreferences DEFAULT = new APIPreferences(
                true, false, 5, 60, "json", false, Map.of()
            );
        }

        /**
         * UI preferences.
         */
        public record UIPreferences(
            Theme theme,
            FontSize fontSize,
            int lineHeight,
            boolean compactMode,
            List<String> visiblePanels,
            boolean showTimestamps,
            boolean showTokens,
            String dateFormat
        ) {
            public enum Theme {
                LIGHT, DARK, SYSTEM, CUSTOM
            }
            public enum FontSize {
                SMALL(12), MEDIUM(14), LARGE(16), EXTRA_LARGE(18);
                private final int px;
                FontSize(int px) { this.px = px; }
                public int px() { return px; }
            }

            public static UIPreferences DEFAULT = new UIPreferences(
                Theme.SYSTEM, FontSize.MEDIUM, 1.6, false,
                List.of("chat", "sources", "context"), true, true, "MMM d, yyyy"
            );
        }

        /**
         * Notification preferences.
         */
        public record NotificationConfig(
            boolean emailDigest,
            boolean pushEnabled,
            boolean desktopNotifications,
            List<String> notifyOn,
            int digestFrequencyMinutes
        ) {
            public static NotificationConfig DEFAULT = new NotificationConfig(
                true, false, false, List.of("errors", "completions"), 1440
            );
        }

        /**
         * Privacy settings.
         */
        public record PrivacyConfig(
            boolean allowAnalytics,
            boolean shareWithOrg,
            boolean publicProfile,
            boolean saveConversationHistory,
            boolean allowDataExport,
            DataRetentionPeriod retention,
            List<String> blockedDomains
        ) {
            public enum DataRetentionPeriod {
                SESSION(0), ONE_DAY(1), ONE_WEEK(7), ONE_MONTH(30), THREE_MONTHS(90), FOREVER(-1);
                private final int days;
                DataRetentionPeriod(int days) { this.days = days; }
                public int days() { return days; }
            }

            public static PrivacyConfig DEFAULT = new PrivacyConfig(
                true, true, false, true, true, DataRetentionPeriod.ONE_MONTH, List.of()
            );
        }

        /**
         * Accessibility settings.
         */
        public record AccessibilityConfig(
            boolean highContrast,
            boolean screenReaderOptimized,
            boolean reducedMotion,
            boolean largeClickTargets,
            String colorBlindMode  // none, deuteranopia, protanopia, tritanopia
        ) {
            public static AccessibilityConfig DEFAULT = new AccessibilityConfig(
                false, false, false, false, "none"
            );
        }

        public static InterfaceConfig DEFAULT = new InterfaceConfig(
            null, null,
            APIPreferences.DEFAULT,
            UIPreferences.DEFAULT,
            NotificationConfig.DEFAULT,
            PrivacyConfig.DEFAULT,
            AccessibilityConfig.DEFAULT
        );
    }

    /**
     * Custom feature for user profiles.
     */
    public record CustomFeature(
        UUID featureId,
        UUID userId,
        String category,
        String name,
        String value,
        DataType dataType,
        boolean isVisible,
        boolean isSearchable,
        Instant createdAt,
        Map<String, String> metadata
    ) {
        public enum DataType {
            STRING, NUMBER, BOOLEAN, LIST, DATE, URL, FILE, TAG
        }

        /**
         * Create a custom string feature.
         */
        public static CustomFeature string(UUID userId, String category, String name, String value) {
            return new CustomFeature(
                UUID.randomUUID(), userId, category, name, value,
                DataType.STRING, true, true, Instant.now(), Map.of()
            );
        }

        /**
         * Create a custom tag feature.
         */
        public static CustomFeature tag(UUID userId, String category, String name, List<String> tags) {
            return new CustomFeature(
                UUID.randomUUID(), userId, category, name, String.join(",", tags),
                DataType.TAG, true, true, Instant.now(), Map.of("count", String.valueOf(tags.size()))
            );
        }
    }

    /**
     * Context length management configuration.
     */
    public record ContextConfig(
        UUID configId,
        UUID userId,
        int maxContextTokens,
        int summaryThreshold,
        ContextStrategy strategy,
        List<String> preserveKeywords,
        boolean compressFootnotes,
        boolean truncateDuplicates
    ) {
        public enum ContextStrategy {
            FULL("Use full context, prioritize freshness"),
            SUMMARIZE("Summarize old context periodically"),
            PRIORITY("Keep recent + high-priority"),
            HYBRID("Smart compression with preservation"),
            MANUAL("User controls what stays")
        }

        public static ContextConfig DEFAULT = new ContextConfig(
            null, null,
            128000,     // 128k default
            100000,     // Summarize above 100k
            ContextStrategy.HYBRID,
            List.of("name", "date", "key finding"),
            true, true
        );
    }
}
