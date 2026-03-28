package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class TogetherAIProvider implements ModelBinder.ModelProvider {

    private final String apiKey;
    private final String baseUrl = "https://api.together.xyz/v1";
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "togetherai";
    private static final String PROVIDER_NAME = "Together AI";

    public TogetherAIProvider(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeModels();
    }

    private void initializeModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("meta-llama/Llama-3.3-70B-Instruct-Turbo", new ModelBinder.ModelInfo(
                "meta-llama/Llama-3.3-70B-Instruct-Turbo", PROVIDER_ID, "Llama 3.3 70B Turbo", 
                "Fast large model", "text", 128000, true, 200.0
            )),
            new AbstractMap.SimpleEntry<>("meta-llama/Llama-3.1-405B-Instruct-Turbo", new ModelBinder.ModelInfo(
                "meta-llama/Llama-3.1-405B-Instruct-Turbo", PROVIDER_ID, "Llama 3.1 405B Turbo", 
                "Extremely large model", "text", 128000, true, 800.0
            )),
            new AbstractMap.SimpleEntry<>("meta-llama/Llama-3.1-70B-Instruct-Turbo", new ModelBinder.ModelInfo(
                "meta-llama/Llama-3.1-70B-Instruct-Turbo", PROVIDER_ID, "Llama 3.1 70B Turbo", 
                "Balanced large model", "text", 128000, true, 150.0
            )),
            new AbstractMap.SimpleEntry<>("meta-llama/Llama-3.1-8B-Instruct-Turbo", new ModelBinder.ModelInfo(
                "meta-llama/Llama-3.1-8B-Instruct-Turbo", PROVIDER_ID, "Llama 3.1 8B Turbo", 
                "Fast small model", "text", 128000, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("mistralai/Mixtral-8x22B-Instruct-v0.1", new ModelBinder.ModelInfo(
                "mistralai/Mixtral-8x22B-Instruct-v0.1", PROVIDER_ID, "Mixtral 8x22B", 
                "Large MoE model", "text", 65536, true, 250.0
            )),
            new AbstractMap.SimpleEntry<>("Qwen/Qwen2-72B-Instruct", new ModelBinder.ModelInfo(
                "Qwen/Qwen2-72B-Instruct", PROVIDER_ID, "Qwen 2 72B Instruct", 
                "Alibaba's large model", "text", 32768, true, 180.0
            )),
            new AbstractMap.SimpleEntry<>("deepseek-ai/DeepSeek-V3", new ModelBinder.ModelInfo(
                "deepseek-ai/DeepSeek-V3", PROVIDER_ID, "DeepSeek V3", 
                "Advanced open model", "text", 128000, true, 300.0
            )),
            new AbstractMap.SimpleEntry<>("mistralai/Mistral-7B-Instruct-v0.3", new ModelBinder.ModelInfo(
                "mistralai/Mistral-7B-Instruct-v0.3", PROVIDER_ID, "Mistral 7B v0.3", 
                "Efficient model", "text", 32768, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("google/gemma-2-27b-it", new ModelBinder.ModelInfo(
                "google/gemma-2-27b-it", PROVIDER_ID, "Gemma 2 27B", 
                "Google's model", "text", 8192, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("sentence-transformers/clip-vit-large-patch14-336", new ModelBinder.ModelInfo(
                "sentence-transformers/clip-vit-large-patch14-336", PROVIDER_ID, "CLIP Vision", 
                "Vision embeddings", "multimodal", 512, true, 30.0
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
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseModelsResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("Together AI model discovery failed: " + e.getMessage());
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
                    String displayName = modelNode.has("display_name") ? modelNode.get("display_name").asText() : id;
                    int ctxLen = modelNode.has("context_length") ? modelNode.get("context_length").asInt() : 4096;
                    
                    models.add(new ModelBinder.ModelInfo(
                        id,
                        PROVIDER_ID,
                        displayName,
                        "Together AI model",
                        "text",
                        ctxLen,
                        true,
                        100.0
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Together AI models: " + e.getMessage());
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
                    throw new RuntimeException("Together AI request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Together AI generation failed", e);
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
                    throw new RuntimeException("Together AI chat failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Together AI chat failed", e);
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
