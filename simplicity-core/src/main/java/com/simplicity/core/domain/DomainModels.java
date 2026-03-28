package com.simplicity.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core domain models for Simplicity's personalization engine.
 * These models form the foundation of the User Knowledge Graph.
 */
public sealed class DomainModels permits User, Feature, Interest, Document, Organization {
    
    /**
     * User identity within Simplicity.
     * The central node of the User Knowledge Graph.
     */
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

    /**
     * A feature attribute of a user.
     * Features are derived from explicit input, inference, or behavioral analysis.
     */
    public record Feature(
        UUID id,
        UUID userId,
        FeatureType type,
        String value,
        double confidence,
        SourceType source
    ) {
        public enum FeatureType {
            ROLE,           // Job role (e.g., "Software Engineer", "Product Manager")
            TEAM,           // Team membership (e.g., "Platform Team")
            DOMAIN,         // Technical domain (e.g., "Machine Learning", "Security")
            SKILL_LEVEL,    // Proficiency level (e.g., "Expert", "Intermediate")
            SENIORITY,      // Experience level (e.g., "Junior", "Senior", "Principal")
            LOCATION,       // Geographic context
            TIMEZONE,       // Working timezone
            LANGUAGE        // Preferred language
        }

        public enum SourceType {
            EXPLICIT,   // User-provided directly
            INFERRED,   // Derived from behavior
            DERIVED     // Computed from other features
        }
    }

    /**
     * A user interest/topic with associated metadata.
     * Interests drive content personalization.
     */
    public record Interest(
        UUID id,
        UUID userId,
        String topic,
        List<String> keywords,
        double strength,
        Instant recency,
        int interactions
    ) {
        /**
         * Creates a new interest with default values.
         */
        public static Interest of(UUID userId, String topic) {
            return new Interest(
                UUID.randomUUID(),
                userId,
                topic,
                List.of(),
                0.5,
                Instant.now(),
                1
            );
        }

        /**
         * Creates an updated interest with incremented interactions.
         */
        public Interest withInteraction() {
            return new Interest(
                id,
                userId,
                topic,
                keywords,
                Math.min(1.0, strength + 0.05),
                Instant.now(),
                interactions + 1
            );
        }
    }

    /**
     * An organizational entity containing users and domain expertise.
     */
    public record Organization(
        UUID id,
        String name,
        String description,
        Map<String, Object> metadata
    ) {}

    /**
     * A document in the search index.
     * Represents content that can be retrieved and cited.
     */
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
        /**
         * Returns a truncated content preview.
         */
        public String preview(int maxLength) {
            if (content == null) return "";
            return content.length() > maxLength 
                ? content.substring(0, maxLength) + "..."
                : content;
        }
    }
}
