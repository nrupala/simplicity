package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class GroqProvider implements ModelBinder.ModelProvider {

    private final String apiKey;
    private final String baseUrl = "https://api.groq.com/openai/v1";
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "groq";
    private static final String PROVIDER_NAME = "Groq";

    public GroqProvider(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeModels();
    }

    private void initializeModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("llama-3.3-70b-versatile", new ModelBinder.ModelInfo(
                "llama-3.3-70b-versatile", PROVIDER_ID, "Llama 3.3 70B Versatile", 
                "Groq's fastest large model", "text", 128000, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("llama-3.1-8b-instant", new ModelBinder.ModelInfo(
                "llama-3.1-8b-instant", PROVIDER_ID, "Llama 3.1 8B Instant", 
                "Groq's fastest small model", "text", 128000, true, 15.0
            )),
            new AbstractMap.SimpleEntry<>("llama-3.2-11b-vision-preview", new ModelBinder.ModelInfo(
                "llama-3.2-11b-vision-preview", PROVIDER_ID, "Llama 3.2 11B Vision Preview", 
                "Llama with vision support", "multimodal", 128000, true, 25.0
            )),
            new AbstractMap.SimpleEntry<>("llama-3.2-3b-preview", new ModelBinder.ModelInfo(
                "llama-3.2-3b-preview", PROVIDER_ID, "Llama 3.2 3B Preview", 
                "Fast Llama 3.2", "text", 128000, true, 12.0
            )),
            new AbstractMap.SimpleEntry<>("mixtral-8x7b-32768", new ModelBinder.ModelInfo(
                "mixtral-8x7b-32768", PROVIDER_ID, "Mixtral 8x7B", 
                "Mixture of experts", "text", 32768, true, 30.0
            )),
            new AbstractMap.SimpleEntry<>("gemma2-9b-it", new ModelBinder.ModelInfo(
                "gemma2-9b-it", PROVIDER_ID, "Gemma 2 9B Instruct", 
                "Google's model on Groq", "text", 8192, true, 20.0
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
                requestBody.put("stream", false);
                
                applyOptions(requestBody, options);
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseCompletionResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Groq request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Groq generation failed", e);
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
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseChatResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Groq chat failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Groq chat failed", e);
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
    }

    private ModelBinder.ModelResponse parseCompletionResponse(String json, String modelId, long start) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(json);
        
        String content = node.get("choices").get(0).get("text").asText();
        int tokens = node.get("usage").get("total_tokens").asInt();
        
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
        int tokens = node.get("usage").get("total_tokens").asInt();
        
        return new ModelBinder.ModelResponse(
            content,
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
                requestBody.put("stream", true);
                
                applyOptions(requestBody, options);
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/completions"))
                    .timeout(Duration.ofSeconds(60))
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
                                        
                                        sink.next(new ModelBinder.StreamingChunk(content, false, null));
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
                .uri(URI.create(baseUrl + "/models"))
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
                    "Connected - Ultra-fast inference",
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
                    "Response: " + response.statusCode(),
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
