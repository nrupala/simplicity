package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class OpenAIProvider implements ModelBinder.ModelProvider {

    private final String apiKey;
    private final String baseUrl = "https://api.openai.com/v1";
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "openai";
    private static final String PROVIDER_NAME = "OpenAI";

    public OpenAIProvider(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeModels();
    }

    private void initializeModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("gpt-4o", new ModelBinder.ModelInfo(
                "gpt-4o", PROVIDER_ID, "GPT-4o", 
                "OpenAI's flagship multimodal model", "multimodal", 128000, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-4o-mini", new ModelBinder.ModelInfo(
                "gpt-4o-mini", PROVIDER_ID, "GPT-4o Mini", 
                "Fast, affordable GPT-4o", "multimodal", 128000, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-4-turbo", new ModelBinder.ModelInfo(
                "gpt-4-turbo", PROVIDER_ID, "GPT-4 Turbo", 
                "Fast GPT-4 with vision", "multimodal", 128000, true, 80.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-4", new ModelBinder.ModelInfo(
                "gpt-4", PROVIDER_ID, "GPT-4", 
                "Original GPT-4 model", "text", 128000, true, 120.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-3.5-turbo", new ModelBinder.ModelInfo(
                "gpt-3.5-turbo", PROVIDER_ID, "GPT-3.5 Turbo", 
                "Fast, affordable model", "text", 16385, true, 30.0
            )),
            new AbstractMap.SimpleEntry<>("o1", new ModelBinder.ModelInfo(
                "o1", PROVIDER_ID, "o1", 
                "OpenAI's reasoning model", "text", 65536, true, 500.0
            )),
            new AbstractMap.SimpleEntry<>("o1-mini", new ModelBinder.ModelInfo(
                "o1-mini", PROVIDER_ID, "o1 Mini", 
                "Fast reasoning model", "text", 65536, true, 200.0
            )),
            new AbstractMap.SimpleEntry<>("o1-preview", new ModelBinder.ModelInfo(
                "o1-preview", PROVIDER_ID, "o1 Preview", 
                "Extended reasoning preview", "text", 32768, true, 400.0
            )),
            new AbstractMap.SimpleEntry<>("text-embedding-3-small", new ModelBinder.ModelInfo(
                "text-embedding-3-small", PROVIDER_ID, "Embedding 3 Small", 
                "Efficient embeddings", "embedding", 8191, true, 20.0
            )),
            new AbstractMap.SimpleEntry<>("text-embedding-3-large", new ModelBinder.ModelInfo(
                "text-embedding-3-large", PROVIDER_ID, "Embedding 3 Large", 
                "High-quality embeddings", "embedding", 8191, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("text-embedding-ada-002", new ModelBinder.ModelInfo(
                "text-embedding-ada-002", PROVIDER_ID, "Embedding Ada v2", 
                "Classic embedding model", "embedding", 8191, true, 15.0
            )),
            new AbstractMap.SimpleEntry<>("dall-e-3", new ModelBinder.ModelInfo(
                "dall-e-3", PROVIDER_ID, "DALL-E 3", 
                "Latest image generation", "image", 4000, true, 200.0
            )),
            new AbstractMap.SimpleEntry<>("dall-e-2", new ModelBinder.ModelInfo(
                "dall-e-2", PROVIDER_ID, "DALL-E 2", 
                "Previous image generation", "image", 1000, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("tts-1", new ModelBinder.ModelInfo(
                "tts-1", PROVIDER_ID, "TTS-1", 
                "Text-to-speech", "audio", 4096, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("whisper-1", new ModelBinder.ModelInfo(
                "whisper-1", PROVIDER_ID, "Whisper", 
                "Speech recognition", "audio", 0, true, 80.0
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
                .uri(URI.create(baseUrl + "/models"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseModelsResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("OpenAI model discovery failed: " + e.getMessage());
        }
        return getAvailableModels();
    }

    private List<ModelBinder.ModelInfo> parseModelsResponse(String json) {
        List<ModelBinder.ModelInfo> models = new ArrayList<>();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(json);
            var dataNode = node.get("data");
            
            if (dataNode != null && dataNode.isArray()) {
                for (var modelNode : dataNode) {
                    String id = modelNode.get("id").asText();
                    String ownedBy = modelNode.has("owned_by") ? modelNode.get("owned_by").asText() : "openai";
                    
                    int ctxLen = modelNode.has("context_window") ? modelNode.get("context_window").asInt() : 4096;
                    
                    models.add(new ModelBinder.ModelInfo(
                        id,
                        PROVIDER_ID,
                        formatModelName(id),
                        ownedBy + " model",
                        "text",
                        ctxLen,
                        true,
                        100.0
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse OpenAI models: " + e.getMessage());
        }
        return models;
    }

    private String formatModelName(String id) {
        return Arrays.stream(id.split("-"))
            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
            .reduce((a, b) -> a + " " + b)
            .orElse(id);
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
                requestBody.put("stream", false);
                
                applyOptions(requestBody, options);
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseCompletionResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("OpenAI request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("OpenAI generation failed", e);
            }
        });
    }

    @Override
    public CompletableFuture<ModelBinder.ModelResponse> chat(
            String modelId, List<ModelBinder.ChatMessage> messages, ModelBinder.GenerationOptions options) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                long start = System.currentTimeMillis();
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelId);
                requestBody.put("stream", false);
                
                List<Map<String, String>> chatMessages = new ArrayList<>();
                for (ModelBinder.ChatMessage msg : messages) {
                    chatMessages.add(Map.of("role", msg.role(), "content", msg.content()));
                }
                requestBody.put("messages", chatMessages);
                
                applyOptions(requestBody, options);
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseChatResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("OpenAI chat failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("OpenAI chat failed", e);
            }
        });
    }

    private void applyOptions(Map<String, Object> requestBody, ModelBinder.GenerationOptions options) {
        if (options == null) return;
        
        if (options.temperature() != null) {
            requestBody.put("temperature", options.temperature());
        }
        if (options.maxTokens() != null) {
            requestBody.put("max_tokens", options.maxTokens());
        }
        if (options.topP() != null) {
            requestBody.put("top_p", options.topP());
        }
        if (options.stopSequences() != null && !options.stopSequences().isEmpty()) {
            requestBody.put("stop", options.stopSequences().values().iterator().next());
        }
        if (options.frequencyPenalty() != null) {
            requestBody.put("frequency_penalty", options.frequencyPenalty());
        }
        if (options.presencePenalty() != null) {
            requestBody.put("presence_penalty", options.presencePenalty());
        }
    }

    private ModelBinder.ModelResponse parseCompletionResponse(String json, String modelId, long start) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(json);
        
        String content = node.get("choices").get(0).get("text").asText();
        var usage = node.get("usage");
        int tokens = usage.get("total_tokens").asInt();
        
        return new ModelBinder.ModelResponse(
            content,
            modelId,
            tokens,
            System.currentTimeMillis() - start,
            Map.of("provider", PROVIDER_ID)
        );
    }

    private ModelBinder.ModelResponse parseChatResponse(String json, String modelId, long start) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(json);
        
        String content = node.get("choices").get(0).get("message").get("content").asText();
        var usage = node.get("usage");
        int tokens = usage.get("total_tokens").asInt();
        String finishReason = node.get("choices").get(0).has("finish_reason") 
            ? node.get("choices").get(0).get("finish_reason").asText() 
            : "stop";
        
        return new ModelBinder.ModelResponse(
            content,
            modelId,
            tokens,
            System.currentTimeMillis() - start,
            Map.of("provider", PROVIDER_ID, "finish_reason", finishReason)
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
                requestBody.put("stream", true);
                
                applyOptions(requestBody, options);
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/completions"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
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
                                        if ("[DONE]".equals(data)) {
                                            sink.complete();
                                            return;
                                        }
                                        
                                        var node = mapper.readTree(data);
                                        String content = node.get("choices").get(0).get("text").asText();
                                        boolean done = node.get("choices").get(0).has("finish_reason");
                                        
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
                .uri(URI.create(baseUrl + "/models/gpt-4o"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + apiKey)
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
