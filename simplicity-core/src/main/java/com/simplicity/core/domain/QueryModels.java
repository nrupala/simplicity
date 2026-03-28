package com.simplicity.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class QueryModels {
    private QueryModels() {}

    public record QueryRequest(
        UUID queryId,
        String query,
        UUID userId,
        QueryOptions options,
        QueryCustomization customization
    ) {
        public record QueryOptions(
            QueryType type,
            QueryDepth depth,
            int maxResults,
            boolean includeReranking,
            boolean includeKnowledgeGraph,
            List<String> filters,
            Instant timeout
        ) {
            public static QueryOptions DEFAULT = new QueryOptions(
                QueryType.AUTO, QueryDepth.STANDARD, 10, true, true, List.of(), Instant.now().plusSeconds(30)
            );
        }

        public enum QueryType { AUTO, FACTUAL, COMPARISON, HOW_TO, OPINION, CODE, RESEARCH }
        public enum QueryDepth {
            BRIEF(100), STANDARD(500), DEEP(2000), EXHAUSTIVE(5000);
            private final int targetTokens;
            QueryDepth(int t) { this.targetTokens = t; }
            public int targetTokens() { return targetTokens; }
        }

        public static QueryRequest simple(String query, UUID userId) {
            return new QueryRequest(UUID.randomUUID(), query, userId, QueryOptions.DEFAULT, QueryCustomization.DEFAULT);
        }
    }

    public record QueryCustomization(
        String intentCommand,
        ResponseSpec response,
        ModelSpec model,
        String contextInjection,
        List<UUID> contextIds,
        CitationSpec citations,
        Map<String, String> parameters
    ) {
        public record ResponseSpec(
            ResponseType type, FormatStyle format, LengthPreference length,
            TonePreference tone, boolean includeSummary, boolean includeToc, boolean includeCodeBlocks
        ) {
            public enum ResponseType { AUTO, TEXT, MARKDOWN, JSON, CODE, TABLE, BULLETS }
            public enum FormatStyle { AUTO, CONCISE, DETAILED, STRUCTURED }
            public enum LengthPreference { AUTO, SHORT, MEDIUM, LONG }
            public enum TonePreference { AUTO, PROFESSIONAL, CASUAL, TECHNICAL }
            public static ResponseSpec DEFAULT = new ResponseSpec(ResponseType.AUTO, FormatStyle.AUTO, LengthPreference.AUTO, TonePreference.AUTO, true, false, true);
        }

        public record ModelSpec(UUID preferredModelId, String preferredProvider, List<String> preferredCapabilities, boolean allowFallback, boolean preferOpenSource) {
            public static ModelSpec DEFAULT = new ModelSpec(null, null, List.of(), true, false);
        }

        public record CitationSpec(boolean enabled, int maxCitations, CitationStyle style, boolean showConfidence, boolean showHoverPreview) {
            public enum CitationStyle { BRACKETS, SUPERSCRIPT, FOOTNOTES }
            public static CitationSpec DEFAULT = new CitationSpec(true, 10, CitationStyle.BRACKETS, true, true);
        }

        public static QueryCustomization DEFAULT = new QueryCustomization(null, ResponseSpec.DEFAULT, ModelSpec.DEFAULT, null, List.of(), CitationSpec.DEFAULT, Map.of());
        public static QueryCustomization withIntent(String command) { return new QueryCustomization(command, ResponseSpec.DEFAULT, ModelSpec.DEFAULT, null, List.of(), CitationSpec.DEFAULT, Map.of()); }
        public static QueryCustomization withContext(String context) { return new QueryCustomization(null, ResponseSpec.DEFAULT, ModelSpec.DEFAULT, context, List.of(), CitationSpec.DEFAULT, Map.of()); }
    }

    public record QueryContext(
        QueryRequest request,
        QueryCustomization.ResponseSpec effectiveResponseSpec,
        QueryCustomization.ModelSpec effectiveModelSpec,
        String combinedContext,
        List<String> systemPrompts,
        Instant createdAt
    ) {}

    public record QueryResponse(
        UUID queryId,
        String answer,
        List<Citation> citations,
        double confidence,
        List<String> suggestions,
        Instant generatedAt,
        DurationMetrics metrics
    ) {
        public record DurationMetrics(long queryParsingMs, long retrievalMs, long rerankingMs, long generationMs, long totalMs) {}
    }

    public record SearchResult(
        DomainModels.Document document,
        double score,
        double personalScore,
        double combinedScore,
        List<String> matchedTerms,
        String highlight
    ) implements Comparable<SearchResult> {
        @Override public int compareTo(SearchResult other) { return Double.compare(other.combinedScore(), this.combinedScore()); }
    }

    public record Citation(int index, String claim, UUID documentId, String sourceUrl, String sourceTitle, String snippet, double confidence) {}

    public record UserContext(
        UUID userId,
        UserProfile profile,
        List<DomainModels.Feature> features,
        List<DomainModels.Interest> interests,
        OrganizationalContext orgContext,
        BehavioralSignals signals
    ) {
        public record UserProfile(String name, String email, UUID organizationId) {}
        public record OrganizationalContext(UUID teamId, String teamName, List<UUID> domainIds, List<String> domainNames, String accessLevel) {}
        public record BehavioralSignals(int searchCountToday, int avgSessionLength, List<String> recentQueries, double clickThroughRate, Instant lastActive) {
            public static BehavioralSignals EMPTY = new BehavioralSignals(0, 0, List.of(), 0.0, Instant.now());
        }
    }
}
