package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class AzureOpenAIProvider implements ModelBinder.ModelProvider {

    private final String endpoint;
    private final String apiKey;
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "azure-openai";
    private static final String PROVIDER_NAME = "Azure OpenAI";

    public AzureOpenAIProvider(String endpoint, String apiKey) {
        this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeModels();
    }

    private void initializeModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("gpt-4o", new ModelBinder.ModelInfo(
                "gpt-4o", PROVIDER_ID, "GPT-4o (Azure)", 
                "Azure OpenAI GPT-4o", "multimodal", 128000, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-4o-mini", new ModelBinder.ModelInfo(
                "gpt-4o-mini", PROVIDER_ID, "GPT-4o Mini (Azure)", 
                "Fast, affordable Azure GPT-4o", "multimodal", 128000, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-4-turbo", new ModelBinder.ModelInfo(
                "gpt-4-turbo", PROVIDER_ID, "GPT-4 Turbo (Azure)", 
                "Fast Azure GPT-4", "multimodal", 128000, true, 80.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-4", new ModelBinder.ModelInfo(
                "gpt-4", PROVIDER_ID, "GPT-4 (Azure)", 
                "Azure OpenAI GPT-4", "text", 128000, true, 120.0
            )),
            new AbstractMap.SimpleEntry<>("gpt-35-turbo", new ModelBinder.ModelInfo(
                "gpt-35-turbo", PROVIDER_ID, "GPT-3.5 Turbo (Azure)", 
                "Fast, affordable Azure model", "text", 16385, true, 30.0
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
                String url = endpoint + "/openai/deployments/" + modelId + "/completions?api-version=2024-02-01";
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseCompletionResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Azure OpenAI request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Azure OpenAI generation failed", e);
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
                String url = endpoint + "/openai/deployments/" + modelId + "/chat/completions?api-version=2024-02-01";
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseChatResponse(response.body(), modelId, start);
                } else {
                    throw new RuntimeException("Azure OpenAI chat failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Azure OpenAI chat failed", e);
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
                String url = endpoint + "/openai/deployments/" + modelId + "/completions?api-version=2024-02-01";
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("api-key", apiKey)
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
            
            String url = endpoint + "/openai/deployments/" + availableModels.keySet().iterator().next() 
                + "/?api-version=2024-02-01";
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("api-key", apiKey)
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - start;
            
            if (response.statusCode() == 200 || response.statusCode() == 404) {
                return new ModelBinder.ProviderHealth(
                    PROVIDER_ID,
                    true,
                    "Connected - Azure OpenAI",
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
