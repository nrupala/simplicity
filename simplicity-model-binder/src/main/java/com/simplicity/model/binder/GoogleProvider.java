package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class GoogleProvider implements ModelBinder.ModelProvider {

    private final String apiKey;
    private final String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "google";
    private static final String PROVIDER_NAME = "Google (Gemini)";

    public GoogleProvider(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeModels();
    }

    private void initializeModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("gemini-2.0-flash-exp", new ModelBinder.ModelInfo(
                "gemini-2.0-flash-exp", PROVIDER_ID, "Gemini 2.0 Flash (Experimental)", 
                "Latest experimental model", "multimodal", 1000000, true, 80.0
            )),
            new AbstractMap.SimpleEntry<>("gemini-1.5-flash-8b", new ModelBinder.ModelInfo(
                "gemini-1.5-flash-8b", PROVIDER_ID, "Gemini 1.5 Flash 8B", 
                "Fast, affordable multimodal", "multimodal", 1000000, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("gemini-1.5-flash", new ModelBinder.ModelInfo(
                "gemini-1.5-flash", PROVIDER_ID, "Gemini 1.5 Flash", 
                "Balanced multimodal model", "multimodal", 1000000, true, 60.0
            )),
            new AbstractMap.SimpleEntry<>("gemini-1.5-pro", new ModelBinder.ModelInfo(
                "gemini-1.5-pro", PROVIDER_ID, "Gemini 1.5 Pro", 
                "Most capable Gemini model", "multimodal", 2000000, true, 150.0
            )),
            new AbstractMap.SimpleEntry<>("gemini-1.0-pro", new ModelBinder.ModelInfo(
                "gemini-1.0-pro", PROVIDER_ID, "Gemini 1.0 Pro", 
                "Original Gemini Pro", "text", 30720, true, 80.0
            )),
            new AbstractMap.SimpleEntry<>("gemini-1.0-pro-vision", new ModelBinder.ModelInfo(
                "gemini-1.0-pro-vision", PROVIDER_ID, "Gemini 1.0 Pro Vision", 
                "Original Gemini with vision", "multimodal", 12288, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("gemini-pro", new ModelBinder.ModelInfo(
                "gemini-pro", PROVIDER_ID, "Gemini Pro (Legacy)", 
                "Legacy Gemini Pro", "text", 30720, true, 70.0
            )),
            new AbstractMap.SimpleEntry<>("gemini-pro-vision", new ModelBinder.ModelInfo(
                "gemini-pro-vision", PROVIDER_ID, "Gemini Pro Vision (Legacy)", 
                "Legacy Gemini Pro Vision", "multimodal", 12288, true, 90.0
            )),
            new AbstractMap.SimpleEntry<>("text-embedding-004", new ModelBinder.ModelInfo(
                "text-embedding-004", PROVIDER_ID, "Text Embedding 004", 
                "Google's latest embeddings", "embedding", 2048, true, 30.0
            )),
            new AbstractMap.SimpleEntry<>("embedding-001", new ModelBinder.ModelInfo(
                "embedding-001", PROVIDER_ID, "Embedding 001", 
                "Original Gemini embeddings", "embedding", 2048, true, 20.0
            ))
        ));
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public List<ModelBinder.ModelInfo> getAvailableModels() {
        return new ArrayList<>(availableModels.values());
    }

    @Override
    public List<ModelBinder.ModelInfo> discoverModels() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/models?key=" + apiKey))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseModelsResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("Google model discovery failed: " + e.getMessage());
        }
        return getAvailableModels();
    }

    private List<ModelBinder.ModelInfo> parseModelsResponse(String json) {
        List<ModelBinder.ModelInfo> models = new ArrayList<>();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(json);
            var modelsNode = node.get("models");
            
            if (modelsNode != null && modelsNode.isArray()) {
                for (var modelNode : modelsNode) {
                    String name = modelNode.get("name").asText();
                    String displayName = modelNode.get("displayName").asText();
                    String description = modelNode.has("description") ? modelNode.get("description").asText() : "";
                    int ctxLen = modelNode.has("inputTokenLimit") ? modelNode.get("inputTokenLimit").asInt() : 32768;
                    
                    models.add(new ModelBinder.ModelInfo(
                        name.replace("models/", ""),
                        PROVIDER_ID,
                        displayName,
                        description,
                        "text",
                        ctxLen,
                        true,
                        100.0
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Google models: " + e.getMessage());
        }
        return models;
    }

    @Override
    public CompletableFuture<ModelBinder.ModelResponse> generate(
            String modelId, String prompt, ModelBinder.GenerationOptions options) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                long start = System.currentTimeMillis();
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
                
                if (options != null) {
                    Map<String, Object> generationConfig = new HashMap<>();
                    if (options.temperature() != null) {
                        generationConfig.put("temperature", options.temperature());
                    }
                    if (options.maxTokens() != null) {
                        generationConfig.put("maxOutputTokens", options.maxTokens());
                    }
                    if (options.topP() != null) {
                        generationConfig.put("topP", options.topP());
                    }
                    if (!generationConfig.isEmpty()) {
                        requestBody.put("generationConfig", generationConfig);
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                String url = baseUrl + "/models/" + modelId + ":generateContent?key=" + apiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Google request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Google generation failed", e);
            }
        });
    }

    @Override
    public CompletableFuture<ModelBinder.ModelResponse> chat(
            String modelId, List<ModelBinder.ChatMessage> messages, ModelBinder.GenerationOptions options) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                long start = System.currentTimeMillis();
                
                List<Map<String, Object>> contents = new ArrayList<>();
                for (ModelBinder.ChatMessage msg : messages) {
                    String role = "user".equals(msg.role()) ? "user" : "model";
                    contents.add(Map.of("role", role, "parts", List.of(Map.of("text", msg.content()))));
                }
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("contents", contents);
                
                if (options != null) {
                    Map<String, Object> generationConfig = new HashMap<>();
                    if (options.temperature() != null) {
                        generationConfig.put("temperature", options.temperature());
                    }
                    if (options.maxTokens() != null) {
                        generationConfig.put("maxOutputTokens", options.maxTokens());
                    }
                    if (options.topP() != null) {
                        generationConfig.put("topP", options.topP());
                    }
                    if (!generationConfig.isEmpty()) {
                        requestBody.put("generationConfig", generationConfig);
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                String url = baseUrl + "/models/" + modelId + ":generateContent?key=" + apiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Google chat failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Google chat failed", e);
            }
        });
    }

    private ModelBinder.ModelResponse parseResponse(String json, String modelId, long start) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(json);
        
        StringBuilder content = new StringBuilder();
        var candidates = node.get("candidates");
        if (candidates != null && candidates.isArray() && candidates.size() > 0) {
            var contentObj = candidates.get(0).get("content");
            if (contentObj != null) {
                var parts = contentObj.get("parts");
                if (parts != null && parts.isArray()) {
                    for (var part : parts) {
                        if (part.has("text")) {
                            content.append(part.get("text").asText());
                        }
                    }
                }
            }
        }
        
        int tokens = 0;
        var usageMetadata = node.get("usageMetadata");
        if (usageMetadata != null && usageMetadata.has("totalTokenCount")) {
            tokens = usageMetadata.get("totalTokenCount").asInt();
        }
        
        return new ModelBinder.ModelResponse(
            content.toString(),
            modelId,
            tokens,
            System.currentTimeMillis() - start,
            Map.of("provider", PROVIDER_ID)
        );
    }

    @Override
    public Flux<ModelBinder.StreamingChunk> stream(
            String modelId, String prompt, ModelBinder.GenerationOptions options) {
        
        return Flux.create(sink -> {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
                requestBody.put("generationConfig", Map.of("responseModalities", List.of("TEXT")));
                
                if (options != null) {
                    if (options.temperature() != null) {
                        ((Map<String, Object>)requestBody.get("generationConfig")).put("temperature", options.temperature());
                    }
                    if (options.maxTokens() != null) {
                        ((Map<String, Object>)requestBody.get("generationConfig")).put("maxOutputTokens", options.maxTokens());
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                String url = baseUrl + "/models/" + modelId + ":generateContent?key=" + apiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        try {
                            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            response.body().forEach(line -> {
                                if (!line.isBlank() && !line.startsWith(":")) {
                                    try {
                                        var node = mapper.readTree(line);
                                        var candidates = node.get("candidates");
                                        if (candidates != null && candidates.size() > 0) {
                                            var parts = candidates.get(0).get("content").get("parts");
                                            if (parts != null && parts.size() > 0) {
                                                String content = parts.get(0).has("text") 
                                                    ? parts.get(0).get("text").asText() : "";
                                                
                                                sink.next(new ModelBinder.StreamingChunk(content, false, null));
                                            }
                                        }
                                        
                                        var usageMetadata = node.get("usageMetadata");
                                        if (usageMetadata != null && usageMetadata.has("totalTokenCount")) {
                                            sink.complete();
                                        }
                                    } catch (Exception e) {
                                        // Skip malformed lines
                                    }
                                }
                            });
                            sink.complete();
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    })
                    .exceptionally(e -> {
                        sink.error(e);
                        return null;
                    });
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public ModelBinder.ProviderHealth healthCheck() {
        try {
            long start = System.currentTimeMillis();
            
            String url = baseUrl + "/models/gemini-1.5-flash?key=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;
            
            if (response.statusCode() == 200) {
                return new ModelBinder.ProviderHealth(
                    PROVIDER_ID,
                    true,
                    "Connected - " + availableModels.size() + " models",
                    (double) latency,
                    null
                );
            } else if (response.statusCode() == 403) {
                return new ModelBinder.ProviderHealth(
                    PROVIDER_ID,
                    false,
                    "Invalid API key",
                    (double) latency,
                    "Authentication failed"
                );
            } else {
                return new ModelBinder.ProviderHealth(
                    PROVIDER_ID,
                    false,
                    "Unexpected response: " + response.statusCode(),
                    (double) latency,
                    null
                );
            }
        } catch (Exception e) {
            return new ModelBinder.ProviderHealth(
                PROVIDER_ID,
                false,
                "Connection failed",
                null,
                e.getMessage()
            );
        }
    }
}
