package com.simplicity.api;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Main entry point for Simplicity API server.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Determine index path
        Path indexPath = args.length > 0 
            ? Path.of(args[0]) 
            : Path.of(System.getProperty("java.io.tmpdir"), "simplicity-index");

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    Simplicity Server                        ║");
        System.out.println("║         AI Answer Engine with Personal Knowledge Graphs       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Index path: " + indexPath);
        System.out.println();

        // Create server
        SimplicityServer server = SimplicityServer.create(indexPath);
        
        // Configure routing
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        HttpRouting.Builder routing = HttpRouting.builder();
        
        // Query endpoint
        routing.post("/api/v1/query", (req, res) -> {
            try {
                var request = req.content().get(com.simplicity.core.domain.QueryModels.QueryRequest.class);
                var response = server.handleQuery(request);
                res.send(response);
            } catch (Exception e) {
                res.status(500).send("Error: " + e.getMessage());
            }
        });

        // Ingest endpoint
        routing.post("/api/v1/ingest", (req, res) -> {
            try {
                var request = req.content().get(SimplicityServer.IngestRequest.class);
                server.handleIngest(request);
                res.send("{\"status\": \"ok\"}");
            } catch (Exception e) {
                res.status(500).send("Error: " + e.getMessage());
            }
        });

        // User graph endpoint
        routing.get("/api/v1/user/{userId}/graph", (req, res) -> {
            UUID userId = UUID.fromString(req.path().pathParameters().get("userId"));
            var context = server.getUserGraph(userId);
            res.send(context);
        });

        // Feedback endpoint
        routing.put("/api/v1/user/{userId}/feedback", (req, res) -> {
            UUID userId = UUID.fromString(req.path().pathParameters().get("userId"));
            var feedback = req.content().get(SimplicityServer.FeedbackRequest.class);
            server.handleFeedback(userId, feedback);
            res.send("{\"status\": \"ok\"}");
        });

        // Feature endpoint
        routing.post("/api/v1/user/{userId}/features", (req, res) -> {
            UUID userId = UUID.fromString(req.path().pathParameters().get("userId"));
            var feature = req.content().get(SimplicityServer.FeatureRequest.class);
            server.addUserFeature(userId, feature);
            res.send("{\"status\": \"ok\"}");
        });

        // Health check
        routing.get("/health", (req, res) -> {
            res.send("{\"status\": \"healthy\", \"version\": \"0.1.0\"}");
        });

        // Build and start
        WebServer server2 = WebServer.builder()
            .port(8080)
            .routing(routing)
            .build();

        server2.start();

        System.out.println("Simplicity server started on http://localhost:8080");
        System.out.println();
        System.out.println("Available endpoints:");
        System.out.println("  POST   /api/v1/query         - Query the answer engine");
        System.out.println("  POST   /api/v1/ingest         - Ingest documents");
        System.out.println("  GET    /api/v1/user/{id}/graph - Get user knowledge graph");
        System.out.println("  PUT    /api/v1/user/{id}/feedback - Submit feedback");
        System.out.println("  POST   /api/v1/user/{id}/features - Add user feature");
        System.out.println("  GET    /health                - Health check");
        System.out.println();
        
        // Keep running
        Thread.currentThread().join();
    }
}
