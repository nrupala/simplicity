package com.simplicity.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Query and response models for Simplicity's API.
 * 
 * Includes full customization support for:
 * - Intent commands (/research, /code, etc.)
 * - Response configuration (type, format, length, tone)
 * - Context injection (persistent or session)
 * - Model selection (user's preferred models)
 */
public sealed class QueryModels permits QueryRequest, QueryResponse, SearchResult, Citation, QueryContext {

    /**
     * A query request from a user.
     * 
     * Supports:
     * - Natural language queries
     * - Intent commands (/command syntax)
     * - Inline context injection
     * - Full customization options
     */
    public record QueryRequest(
        UUID queryId,
        String query,
        UUID userId,
        QueryOptions options,
        QueryCustomization customization
    ) {
        /**
         * Query execution options.
         */
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
                QueryType.AUTO,
                QueryDepth.STANDARD,
                10,
                true,
                true,
                List.of(),
                Instant.now().plusSeconds(30)
            );
        }

        public enum QueryType {
            AUTO,       // System determines type
            FACTUAL,    // Seeking facts
            COMPARISON, // Comparing options
            HOW_TO,     // Step-by-step guidance
            OPINION,    // Seeking opinions
            CODE,       // Technical/code questions
            RESEARCH    // In-depth research
        }

        public enum QueryDepth {
            BRIEF(100),      // Quick answer
            STANDARD(500),   // Normal depth
            DEEP(2000),      // Comprehensive
            EXHAUSTIVE(5000);// Complete analysis
            ;
            private final int targetTokens;
            QueryDepth(int targetTokens) { this.targetTokens = targetTokens; }
            public int targetTokens() { return targetTokens; }
        }

        /**
         * Create a simple query with defaults.
         */
        public static QueryRequest simple(String query, UUID userId) {
            return new QueryRequest(
                UUID.randomUUID(), query, userId,
                QueryOptions.DEFAULT, QueryCustomization.DEFAULT
            );
        }

        /**
         * Create a query with intent command.
         */
        public static QueryRequest withCommand(String query, UUID userId, String command) {
            return new QueryRequest(
                UUID.randomUUID(), query, userId,
                QueryOptions.DEFAULT, QueryCustomization.withIntent(command)
            );
        }

        /**
         * Create a query with context injection.
         */
        public static QueryRequest withContext(String query, UUID userId, String contextContent) {
            return new QueryRequest(
                UUID.randomUUID(), query, userId,
                QueryOptions.DEFAULT, QueryCustomization.withContext(contextContent)
            );
        }
    }

    /**
     * Query customization options.
     * 
     * Users can fully customize:
     * - Intent commands (system behaviors)
     * - Response type, format, length, tone
     * - Model selection (preferred AI models)
     * - Context injection (add context to this query)
     * - Citation preferences
     */
    public record QueryCustomization(
        String intentCommand,          // e.g., "/research", "/code"
        ResponseSpec response,         // Response type, format, length, tone
        ModelSpec model,               // Preferred models for this query
        String contextInjection,       // Additional context for this query
        List<UUID> contextIds,         // Reference existing contexts
        CitationSpec citations,        // Citation preferences
        Map<String, String> parameters // Custom parameters
    ) {
        /**
         * Response specification.
         */
        public record ResponseSpec(
            ResponseType type,
            FormatStyle format,
            LengthPreference length,
            TonePreference tone,
            boolean includeSummary,
            boolean includeToc,
            boolean includeCodeBlocks
        ) {
            public enum ResponseType {
                AUTO, TEXT, MARKDOWN, JSON, HTML, XML, 
                CODE, TABLE, BULLETS, NUMBERED, CONVERSATIONAL
            }
            public enum FormatStyle { AUTO, CONCISE, DETAILED, STRUCTURED, FLOWING }
            public enum LengthPreference { AUTO, SHORT, MEDIUM, LONG, COMPREHENSIVE }
            public enum TonePreference { AUTO, PROFESSIONAL, CASUAL, ACADEMIC, TECHNICAL, SIMPLE }

            public static ResponseSpec DEFAULT = new ResponseSpec(
                ResponseType.AUTO, FormatStyle.AUTO, LengthPreference.AUTO,
                TonePreference.AUTO, true, false, true
            );
        }

        /**
         * Model specification.
         */
        public record ModelSpec(
            UUID preferredModelId,
            String preferredProvider,   // e.g., "openai", "anthropic", "local"
            List<String> preferredCapabilities, // e.g., ["code", "reasoning"]
            boolean allowFallback,      // Fall back to other models if preferred unavailable
            boolean preferOpenSource    // Prefer open source models
        ) {
            public static ModelSpec DEFAULT = new ModelSpec(
                null, null, List.of(), true, false
            );
        }

        /**
         * Citation specification.
         */
        public record CitationSpec(
            boolean enabled,
            int maxCitations,
            CitationStyle style,
            boolean showConfidence,
            boolean showHoverPreview
        ) {
            public enum CitationStyle { BRACKETS, SUPERSCRIPT, FOOTNOTES, LINKED }
            public static CitationSpec DEFAULT = new CitationSpec(true, 10, CitationStyle.BRACKETS, true, true);
        }

        public static QueryCustomization DEFAULT = new QueryCustomization(
            null, ResponseSpec.DEFAULT, ModelSpec.DEFAULT, null, List.of(),
            CitationSpec.DEFAULT, Map.of()
        );

        /**
         * Add an intent command.
         */
        public static QueryCustomization withIntent(String command) {
            return new QueryCustomization(
                command, ResponseSpec.DEFAULT, ModelSpec.DEFAULT, null, List.of(),
                CitationSpec.DEFAULT, Map.of()
            );
        }

        /**
         * Add context injection.
         */
        public static QueryCustomization withContext(String context) {
            return new QueryCustomization(
                null, ResponseSpec.DEFAULT, ModelSpec.DEFAULT, context, List.of(),
                CitationSpec.DEFAULT, Map.of()
            );
        }

        /**
         * Set preferred model.
         */
        public QueryCustomization withModel(UUID modelId, String provider) {
            return new QueryCustomization(
                intentCommand(), response(), 
                new ModelSpec(modelId, provider, List.of(), true, false),
                contextInjection(), contextIds(), citations(), parameters()
            );
        }

        /**
         * Set response type.
         */
        public QueryCustomization withResponseType(ResponseSpec.ResponseType type) {
            return new QueryCustomization(
                intentCommand(),
                new ResponseSpec(type, response().format(), response().length(),
                    response().tone(), response().includeSummary(), 
                    response().includeToc(), response().includeCodeBlocks()),
                model(), contextInjection(), contextIds(), citations(), parameters()
            );
        }

        /**
         * Set response length.
         */
        public QueryCustomization withLength(ResponseSpec.LengthPreference length) {
            return new QueryCustomization(
                intentCommand(),
                new ResponseSpec(response().type(), response().format(), length,
                    response().tone(), response().includeSummary(),
                    response().includeToc(), response().includeCodeBlocks()),
                model(), contextInjection(), contextIds(), citations(), parameters()
            );
        }
    }

    /**
     * Query context (combined user context + request context).
     */
    public record QueryContext(
        QueryRequest request,
        QueryCustomization.ResponseSpec effectiveResponseSpec,
        QueryCustomization.ModelSpec effectiveModelSpec,
        String combinedContext,
        List<String> systemPrompts,
        Instant createdAt
    ) {}

    /**
     * A query response with answer and citations.
     */
    public record QueryResponse(
        UUID queryId,
        String answer,
        List<Citation> citations,
        double confidence,
        List<String> suggestions,
        Instant generatedAt,
        DurationMetrics metrics
    ) {
        public record DurationMetrics(
            long queryParsingMs,
            long retrievalMs,
            long rerankingMs,
            long generationMs,
            long totalMs
        ) {}
    }

    /**
     * A search result from the RAG engine.
     */
    public record SearchResult(
        Document document,
        double score,
        double personalScore,
        double combinedScore,
        List<String> matchedTerms,
        String highlight
    ) implements Comparable<SearchResult> {
        
        @Override
        public int compareTo(SearchResult other) {
            return Double.compare(other.combinedScore(), this.combinedScore());
        }
    }

    /**
     * A citation/reference to a source document.
     */
    public record Citation(
        int index,
        String claim,
        UUID documentId,
        String sourceUrl,
        String sourceTitle,
        String snippet,
        double confidence
    ) {}

    /**
     * User context for personalization.
     * Built from the User Knowledge Graph.
     */
    public record UserContext(
        UUID userId,
        UserProfile profile,
        List<Feature> features,
        List<Interest> interests,
        OrganizationalContext orgContext,
        BehavioralSignals signals
    ) {
        public record UserProfile(
            String name,
            String email,
            UUID organizationId
        ) {}

        public record OrganizationalContext(
            UUID teamId,
            String teamName,
            List<UUID> domainIds,
            List<String> domainNames,
            String accessLevel
        ) {}

        public record BehavioralSignals(
            int searchCountToday,
            int avgSessionLength,
            List<String> recentQueries,
            double clickThroughRate,
            Instant lastActive
        ) {
            public static BehavioralSignals EMPTY = new BehavioralSignals(
                0, 0, List.of(), 0.0, Instant.now()
            );
        }
    }
}
