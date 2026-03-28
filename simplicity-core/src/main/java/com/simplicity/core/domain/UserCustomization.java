package com.simplicity.core.domain;

import java.time.Instant;
import java.util.*;

public final class UserCustomization {
    private UserCustomization() {}

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
        public enum ContextType { PERSISTENT, SESSION, PROJECT, DOCUMENT, CONVERSATION }
        public static UserContext persistent(UUID userId, String title, String content) {
            return new UserContext(UUID.randomUUID(), userId, ContextType.PERSISTENT, title, content, List.of(), Instant.now(), null, true);
        }
        public static UserContext session(UUID userId, String title, String content, int ttlMinutes) {
            return new UserContext(UUID.randomUUID(), userId, ContextType.SESSION, title, content, List.of(), Instant.now(), Instant.now().plusSeconds(ttlMinutes * 60L), true);
        }
        public boolean isExpired() { return expiresAt != null && Instant.now().isAfter(expiresAt); }
    }

    public record IntentCommand(
        UUID commandId,
        UUID userId,
        String name,
        String command,
        String description,
        String systemPrompt,
        List<String> aliases,
        boolean enabled,
        Map<String, String> parameters,
        Instant createdAt,
        int usageCount
    ) {
        public static IntentCommand custom(UUID userId, String name, String cmd, String desc, String prompt) {
            return new IntentCommand(UUID.randomUUID(), userId, name, cmd, desc, prompt, List.of(), true, Map.of(), Instant.now(), 0);
        }
    }

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
        public enum ResponseType { TEXT, MARKDOWN, JSON, CODE_BLOCK, TABLE, BULLET_POINTS, CONVERSATIONAL }
        public enum FormatStyle { CONCISE, DETAILED, STRUCTURED, FLOWING }
        public enum LengthPreference { SHORT(100), BRIEF(250), MEDIUM(500), STANDARD(1000), LONG(2000), COMPREHENSIVE(5000);
            private final int maxTokens; LengthPreference(int t) { maxTokens = t; } public int maxTokens() { return maxTokens; }
        }
        public record CitationConfig(boolean enabled, CitationStyle style, int maxCitations, boolean inlineNumbers, boolean hoverPreview) {
            public enum CitationStyle { BRACKET_NUMBERS, SUPERSCRIPT, FOOTNOTES, LINKED }
            public static CitationConfig DEFAULT = new CitationConfig(true, CitationStyle.BRACKET_NUMBERS, 10, true, true);
        }
        public record LanguageConfig(String language, String variant, boolean formalRegister) {
            public static LanguageConfig DEFAULT = new LanguageConfig("en", "US", false);
        }
        public record ToneConfig(ToneType tone, String customInstructions) {
            public enum ToneType { PROFESSIONAL, CASUAL, ACADEMIC, TECHNICAL, SIMPLE }
            public static ToneConfig DEFAULT = new ToneConfig(ToneType.PROFESSIONAL, "");
        }
        public static ResponseConfig DEFAULT = new ResponseConfig(null, null, ResponseType.MARKDOWN, FormatStyle.STRUCTURED, LengthPreference.STANDARD, CitationConfig.DEFAULT, LanguageConfig.DEFAULT, ToneConfig.DEFAULT);
    }

    public record InterfaceConfig(
        UUID configId,
        UUID userId,
        APIPreferences api,
        UIPreferences ui,
        NotificationConfig notifications,
        PrivacyConfig privacy,
        AccessibilityConfig accessibility
    ) {
        public record APIPreferences(boolean streamingEnabled, boolean batchModeEnabled, int maxConcurrentRequests, int timeoutSeconds) {
            public static APIPreferences DEFAULT = new APIPreferences(true, false, 5, 60);
        }
        public record UIPreferences(Theme theme, FontSize fontSize, boolean compactMode) {
            public enum Theme { LIGHT, DARK, SYSTEM }
            public enum FontSize { SMALL(12), MEDIUM(14), LARGE(16); private final int px; FontSize(int p) { px = p; } public int px() { return px; } }
            public static UIPreferences DEFAULT = new UIPreferences(Theme.SYSTEM, FontSize.MEDIUM, false);
        }
        public record NotificationConfig(boolean emailDigest, boolean pushEnabled) {
            public static NotificationConfig DEFAULT = new NotificationConfig(true, false);
        }
        public record PrivacyConfig(boolean allowAnalytics, boolean saveConversationHistory, DataRetentionPeriod retention) {
            public enum DataRetentionPeriod { SESSION(0), ONE_DAY(1), ONE_MONTH(30), FOREVER(-1);
                private final int days; DataRetentionPeriod(int d) { days = d; } public int days() { return days; } }
            public static PrivacyConfig DEFAULT = new PrivacyConfig(true, true, DataRetentionPeriod.ONE_MONTH);
        }
        public record AccessibilityConfig(boolean highContrast, boolean reducedMotion) {
            public static AccessibilityConfig DEFAULT = new AccessibilityConfig(false, false);
        }
        public static InterfaceConfig DEFAULT = new InterfaceConfig(null, null, APIPreferences.DEFAULT, UIPreferences.DEFAULT, NotificationConfig.DEFAULT, PrivacyConfig.DEFAULT, AccessibilityConfig.DEFAULT);
    }

    public record CustomFeature(UUID featureId, UUID userId, String category, String name, String value, DataType dataType, boolean isVisible, boolean isSearchable, Instant createdAt) {
        public enum DataType { STRING, NUMBER, BOOLEAN, LIST, TAG }
        public static CustomFeature string(UUID userId, String cat, String name, String val) {
            return new CustomFeature(UUID.randomUUID(), userId, cat, name, val, DataType.STRING, true, true, Instant.now());
        }
    }

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
        public enum ContextStrategy { FULL, SUMMARIZE, PRIORITY, HYBRID, MANUAL }
        public static ContextConfig DEFAULT = new ContextConfig(null, null, 128000, 100000, ContextStrategy.HYBRID, List.of("name", "date"), true, true);
    }
}
