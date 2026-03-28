package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class AnthropicProvider implements ModelBinder.ModelProvider {

    private final String apiKey;
    private final String baseUrl = "https://api.anthropic.com/v1";
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "anthropic";
    private static final String PROVIDER_NAME = "Anthropic (Claude)";

    public AnthropicProvider(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeModels();
    }

    private void initializeModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("claude-opus-4-5", new ModelBinder.ModelInfo(
                "claude-opus-4-5", PROVIDER_ID, "Claude Opus 4.5", 
                "Anthropic's most capable model", "text", 200000, true, 300.0
            )),
            new AbstractMap.SimpleEntry<>("claude-sonnet-4-5", new ModelBinder.ModelInfo(
                "claude-sonnet-4-5", PROVIDER_ID, "Claude Sonnet 4.5", 
                "Balanced performance and speed", "text", 200000, true, 150.0
            )),
            new AbstractMap.SimpleEntry<>("claude-haiku-4-5", new ModelBinder.ModelInfo(
                "claude-haiku-4-5", PROVIDER_ID, "Claude Haiku 4.5", 
                "Fast, affordable model", "text", 200000, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("claude-3-5-sonnet-latest", new ModelBinder.ModelInfo(
                "claude-3-5-sonnet-latest", PROVIDER_ID, "Claude 3.5 Sonnet (Latest)", 
                "Latest Claude 3.5 Sonnet", "text", 200000, true, 120.0
            )),
            new AbstractMap.SimpleEntry<>("claude-3-5-haiku-latest", new ModelBinder.ModelInfo(
                "claude-3-5-haiku-latest", PROVIDER_ID, "Claude 3.5 Haiku (Latest)", 
                "Latest Claude 3.5 Haiku", "text", 200000, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("claude-3-opus", new ModelBinder.ModelInfo(
                "claude-3-opus", PROVIDER_ID, "Claude 3 Opus", 
                "Original Claude 3 Opus", "text", 200000, true, 250.0
            )),
            new AbstractMap.SimpleEntry<>("claude-3-sonnet", new ModelBinder.ModelInfo(
                "claude-3-sonnet", PROVIDER_ID, "Claude 3 Sonnet", 
                "Original Claude 3 Sonnet", "text", 200000, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("claude-3-haiku", new ModelBinder.ModelInfo(
                "claude-3-haiku", PROVIDER_ID, "Claude 3 Haiku", 
                "Original Claude 3 Haiku", "text", 200000, true, 30.0
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
        return getAvailableModels();
    }

    @Override
    public CompletableFuture<ModelBinder.ModelResponse> generate(
            String modelId, String prompt, ModelBinder.GenerationOptions options) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                long start = System.currentTimeMillis();
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelId);
                requestBody.put("prompt", prompt);
                requestBody.put("max_tokens", options != null && options.maxTokens() != null 
                    ? options.maxTokens() : 4096);
                
                if (options != null) {
                    if (options.temperature() != null) {
                        requestBody.put("temperature", options.temperature());
                    }
                    if (options.topP() != null) {
                        requestBody.put("top_p", options.topP());
                    }
                    if (options.stopSequences() != null && !options.stopSequences().isEmpty()) {
                        requestBody.put("stop_sequences", new ArrayList<>(options.stopSequences().keySet()));
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/complete"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Anthropic request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Anthropic generation failed", e);
            }
        });
    }

    @Override
    public CompletableFuture<ModelBinder.ModelResponse> chat(
            String modelId, List<ModelBinder.ChatMessage> messages, ModelBinder.GenerationOptions options) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                long start = System.currentTimeMillis();
                
                List<Map<String, String>> anthropicMessages = new ArrayList<>();
                String systemPrompt = null;
                
                for (ModelBinder.ChatMessage msg : messages) {
                    if ("system".equals(msg.role())) {
                        systemPrompt = msg.content();
                    } else {
                        anthropicMessages.add(Map.of(
                            "role", msg.role().equals("assistant") ? "assistant" : "user",
                            "content", msg.content()
                        ));
                    }
                }
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelId);
                requestBody.put("messages", anthropicMessages);
                requestBody.put("max_tokens", options != null && options.maxTokens() != null 
                    ? options.maxTokens() : 4096);
                
                if (systemPrompt != null) {
                    requestBody.put("system", systemPrompt);
                }
                
                if (options != null) {
                    if (options.temperature() != null) {
                        requestBody.put("temperature", options.temperature());
                    }
                    if (options.topP() != null) {
                        requestBody.put("top_p", options.topP());
                    }
                    if (options.stopSequences() != null && !options.stopSequences().isEmpty()) {
                        requestBody.put("stop_sequences", new ArrayList<>(options.stopSequences().keySet()));
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/messages"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("anthropic-dangerous-direct-browser-access", "true")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseMessageResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Anthropic chat failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Anthropic chat failed", e);
            }
        });
    }

    private ModelBinder.ModelResponse parseResponse(String json, String modelId, long start) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(json);
        
        String content = node.get("completion").asText();
        var usage = node.get("usage");
        int tokens = usage.get("tokens").asInt();
        
        return new ModelBinder.ModelResponse(
            content,
            modelId,
            tokens,
            System.currentTimeMillis() - start,
            Map.of("provider", PROVIDER_ID)
        );
    }

    private ModelBinder.ModelResponse parseMessageResponse(String json, String modelId, long start) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(json);
        
        StringBuilder content = new StringBuilder();
        var contentBlocks = node.get("content");
        if (contentBlocks != null && contentBlocks.isArray()) {
            for (var block : contentBlocks) {
                if ("text".equals(block.get("type").asText())) {
                    content.append(block.get("text").asText());
                }
            }
        }
        
        var usage = node.get("usage");
        int tokens = usage.get("output_tokens").asInt();
        
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
                requestBody.put("model", modelId);
                requestBody.put("prompt", prompt);
                requestBody.put("max_tokens", options != null && options.maxTokens() != null 
                    ? options.maxTokens() : 4096);
                requestBody.put("stream", true);
                
                if (options != null && options.temperature() != null) {
                    requestBody.put("temperature", options.temperature());
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/complete"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        try {
                            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            response.body().forEach(line -> {
                                if (line.startsWith("data: ")) {
                                    try {
                                        String data = line.substring(6);
                                        
                                        var node = mapper.readTree(data);
                                        String content = node.get("completion").asText();
                                        boolean done = node.has("stop_reason");
                                        
                                        sink.next(new ModelBinder.StreamingChunk(content, done, null));
                                        
                                        if (done) {
                                            sink.complete();
                                        }
                                    } catch (Exception e) {
                                        sink.error(e);
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
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/messages"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("anthropic-dangerous-direct-browser-access", "true")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "{\"model\":\"claude-haiku-4-5\",\"messages\":[{\"role\":\"user\",\"content\":\"ping\"}],\"max_tokens\":10}"))
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
            } else if (response.statusCode() == 401) {
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
