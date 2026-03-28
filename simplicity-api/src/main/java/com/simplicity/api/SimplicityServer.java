package com.simplicity.api;

import com.simplicity.core.domain.DomainModels.*;
import com.simplicity.core.domain.QueryModels.*;
import com.simplicity.rag.SimplicitySearchEngine;
import com.simplicity.kg.UserKnowledgeGraph;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Main API server for Simplicity.
 * 
 * Provides REST endpoints for:
 * - Query processing with personalization
 * - Document ingestion
 * - User knowledge graph management
 * - Feedback and learning
 */
public class SimplicityServer {

    private final SimplicitySearchEngine searchEngine;
    private final UserKnowledgeGraph knowledgeGraph;
    private final ObjectMapper objectMapper;
    private final AtomicLong queryCounter = new AtomicLong(0);

    public SimplicityServer(Path indexPath) throws IOException {
        this.searchEngine = new SimplicitySearchEngine(indexPath);
        this.knowledgeGraph = new UserKnowledgeGraph();
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Handle query request.
     * POST /api/v1/query
     */
    public QueryResponse handleQuery(QueryRequest request) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Build user context from knowledge graph
        UserContext userContext = knowledgeGraph.buildUserContext(request.userId());
        
        // Record interest in query topic
        if (request.userId() != null) {
            knowledgeGraph.recordInterest(request.userId(), request.query());
        }
        
        // Execute search with personalization
        long retrievalStart = System.currentTimeMillis();
        List<SearchResult> results = searchEngine.search(request, userContext);
        long retrievalMs = System.currentTimeMillis() - retrievalStart;
        
        // Build response
        long generationStart = System.currentTimeMillis();
        String answer = generateAnswer(request.query(), results);
        List<Citation> citations = buildCitations(results);
        long generationMs = System.currentTimeMillis() - generationStart;
        
        long totalMs = System.currentTimeMillis() - startTime;
        
        return new QueryResponse(
            new UUID(0, queryCounter.incrementAndGet()),
            answer,
            citations,
            calculateConfidence(results),
            generateSuggestions(request.query(), results),
            Instant.now(),
            new QueryResponse.DurationMetrics(5, retrievalMs, 10, generationMs, totalMs)
        );
    }

    /**
     * Handle document ingestion.
     * POST /api/v1/ingest
     */
    public void handleIngest(IngestRequest request) throws IOException {
        List<Document> docs = request.documents().stream()
            .map(d -> new Document(
                UUID.randomUUID(),
                d.title(),
                d.content(),
                d.source(),
                d.url(),
                d.authors(),
                d.publishedAt(),
                Instant.now(),
                d.metadata(),
                d.tags(),
                0.0
            ))
            .toList();
        
        searchEngine.indexDocuments(docs);
    }

    /**
     * Handle user feedback.
     * PUT /api/v1/user/{userId}/feedback
     */
    public void handleFeedback(UUID userId, FeedbackRequest feedback) {
        knowledgeGraph.learnFromInteraction(
            userId,
            feedback.query(),
            feedback.clickedTags(),
            feedback.timeOnResult(),
            feedback.wasHelpful()
        );
    }

    /**
     * Get user knowledge graph.
     * GET /api/v1/user/{userId}/graph
     */
    public UserContext getUserGraph(UUID userId) {
        return knowledgeGraph.buildUserContext(userId);
    }

    /**
     * Add a feature to user.
     * POST /api/v1/user/{userId}/features
     */
    public void addUserFeature(UUID userId, FeatureRequest request) {
        Feature feature = new Feature(
            UUID.randomUUID(),
            userId,
            Feature.FeatureType.valueOf(request.type()),
            request.value(),
            request.confidence(),
            Feature.SourceType.EXPLICIT
        );
        knowledgeGraph.addFeature(userId, feature);
    }

    // --- Response generation methods ---

    private String generateAnswer(String query, List<SearchResult> results) {
        if (results.isEmpty()) {
            return "I couldn't find relevant information to answer your query. " +
                   "Could you try rephrasing or being more specific?";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Based on my search, here's what I found:\n\n");
        
        int count = 0;
        for (SearchResult result : results) {
            if (count >= 3) break;
            sb.append("[").append(count + 1).append("] ");
            sb.append(result.document().title());
            sb.append("\n");
            sb.append(result.highlight());
            sb.append("\n\n");
            count++;
        }
        
        sb.append("I've cited the sources above. Would you like me to provide more details on any of these topics?");
        
        return sb.toString();
    }

    private List<Citation> buildCitations(List<SearchResult> results) {
        return results.stream()
            .limit(10)
            .map(r -> new Citation(
                results.indexOf(r) + 1,
                r.highlight(),
                r.document().id(),
                r.document().url(),
                r.document().title(),
                r.document().preview(200),
                r.combinedScore()
            ))
            .toList();
    }

    private double calculateConfidence(List<SearchResult> results) {
        if (results.isEmpty()) return 0.0;
        double avgScore = results.stream()
            .mapToDouble(SearchResult::combinedScore)
            .average()
            .orElse(0.0);
        return Math.min(1.0, avgScore);
    }

    private List<String> generateSuggestions(String query, List<SearchResult> results) {
        return List.of(
            "Related: " + results.stream()
                .findFirst()
                .map(r -> r.document().title())
                .orElse("topics"),
            "Learn more about: " + results.stream()
                .skip(1)
                .findFirst()
                .map(r -> r.document().tags().stream().findFirst().orElse(""))
                .orElse("")
        );
    }

    // --- Request/Response records ---

    public record IngestRequest(List<IngestDocument> documents) {
        public record IngestDocument(
            String title,
            String content,
            String source,
            String url,
            List<String> authors,
            Instant publishedAt,
            Map<String, String> metadata,
            List<String> tags
        ) {}
    }

    public record FeedbackRequest(
        String query,
        List<String> clickedTags,
        double timeOnResult,
        boolean wasHelpful
    ) {}

    public record FeatureRequest(
        String type,
        String value,
        double confidence
    ) {}

    /**
     * Create and start the server.
     */
    public static SimplicityServer create(Path indexPath) throws IOException {
        return new SimplicityServer(indexPath);
    }

    public void shutdown() throws IOException {
        searchEngine.close();
    }

    public UserKnowledgeGraph getKnowledgeGraph() {
        return knowledgeGraph;
    }
}
