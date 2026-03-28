package com.simplicity.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DomainModels {
    private DomainModels() {}

    public record User(
        UUID id,
        String email,
        String name,
        UUID organizationId,
        Instant createdAt,
        Instant lastActiveAt,
        UserPreferences preferences
    ) {
        public record UserPreferences(
            boolean darkMode,
            List<String> preferredLanguages,
            int resultPageSize,
            boolean showCitations
        ) {
            public static UserPreferences DEFAULT = new UserPreferences(
                false, List.of("en"), 10, true
            );
        }
    }

    public record Feature(
        UUID id,
        UUID userId,
        FeatureType type,
        String value,
        double confidence,
        SourceType source
    ) {
        public enum FeatureType {
            ROLE, SKILL_LEVEL, SENIORITY, LOCATION, TIMEZONE, LANGUAGE, DOMAIN, TEAM
        }

        public enum SourceType {
            EXPLICIT, INFERRED, DERIVED
        }
    }

    public record Interest(
        UUID id,
        UUID userId,
        String topic,
        List<String> keywords,
        double strength,
        Instant recency,
        int interactions
    ) {
        public static Interest of(UUID userId, String topic) {
            return new Interest(UUID.randomUUID(), userId, topic, List.of(), 0.5, Instant.now(), 1);
        }

        public Interest withInteraction() {
            return new Interest(id, userId, topic, keywords, Math.min(1.0, strength + 0.05), Instant.now(), interactions + 1);
        }
    }

    public record Organization(
        UUID id,
        String name,
        String description,
        Map<String, Object> metadata
    ) {}

    public record Document(
        UUID id,
        String title,
        String content,
        String source,
        String url,
        List<String> authors,
        Instant publishedAt,
        Instant indexedAt,
        Map<String, String> metadata,
        List<String> tags,
        double relevanceScore
    ) {
        public String preview(int maxLength) {
            if (content == null) return "";
            return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
        }
    }
}
