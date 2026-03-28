package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class LMStudioProvider implements ModelBinder.ModelProvider {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "lmstudio";
    private static final String PROVIDER_NAME = "LM Studio (Local)";

    public LMStudioProvider(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeKnownModels();
    }

    private void initializeKnownModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("LFM2.5-1.2B-Instruct-Q8_0", new ModelBinder.ModelInfo(
                "LFM2.5-1.2B-Instruct-Q8_0", PROVIDER_ID, "LFM 2.5 1.2B (Q8)", 
                "Local model from LM Studio", "text", 4096, true, 25.0
            )),
            new AbstractMap.SimpleEntry<>("llama-3.2-1b-instruct", new ModelBinder.ModelInfo(
                "llama-3.2-1b-instruct", PROVIDER_ID, "Llama 3.2 1B Instruct", 
                "Meta's smallest instruct model", "text", 128000, true, 20.0
            )),
            new AbstractMap.SimpleEntry<>("llama-3.2-3b-instruct", new ModelBinder.ModelInfo(
                "llama-3.2-3b-instruct", PROVIDER_ID, "Llama 3.2 3B Instruct", 
                "Meta's medium instruct model", "text", 128000, true, 30.0
            )),
            new AbstractMap.SimpleEntry<>("llama-3.1-8b-instruct", new ModelBinder.ModelInfo(
                "llama-3.1-8b-instruct", PROVIDER_ID, "Llama 3.1 8B Instruct", 
                "Meta's 8B instruct model", "text", 128000, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("mistral-7b-instruct", new ModelBinder.ModelInfo(
                "mistral-7b-instruct", PROVIDER_ID, "Mistral 7B Instruct", 
                "Efficient instruction model", "text", 8192, true, 35.0
            )),
            new AbstractMap.SimpleEntry<>("phi-3.5-mini-instruct", new ModelBinder.ModelInfo(
                "phi-3.5-mini-instruct", PROVIDER_ID, "Phi-3.5 Mini Instruct", 
                "Microsoft's efficient model", "text", 4096, true, 25.0
            )),
            new AbstractMap.SimpleEntry<>("qwen-2.5-7b-instruct", new ModelBinder.ModelInfo(
                "qwen-2.5-7b-instruct", PROVIDER_ID, "Qwen 2.5 7B Instruct", 
                "Alibaba's multilingual model", "text", 32768, true, 45.0
            )),
            new AbstractMap.SimpleEntry<>("gemma-2-2b-it", new ModelBinder.ModelInfo(
                "gemma-2-2b-it", PROVIDER_ID, "Gemma 2 2B Instruct", 
                "Google's small instruct model", "text", 8192, true, 25.0
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
                .uri(URI.create(baseUrl + "/v1/models"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                List<ModelBinder.ModelInfo> discovered = parseLMStudioModels(response.body());
                discovered.forEach(model -> availableModels.put(model.modelId(), model));
                return discovered;
            }
        } catch (Exception e) {
            System.err.println("LM Studio discovery failed: " + e.getMessage());
        }
        return getAvailableModels();
    }

    private List<ModelBinder.ModelInfo> parseLMStudioModels(String json) {
        List<ModelBinder.ModelInfo> models = new ArrayList<>();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(json);
            var dataNode = node.get("data");
            
            if (dataNode != null && dataNode.isArray()) {
                for (var modelNode : dataNode) {
                    String id = modelNode.get("id").asText();
                    String object = modelNode.has("object") ? modelNode.get("object").asText() : "model";
                    int ctxLen = modelNode.has("context_window") ? modelNode.get("context_window").asInt() : 4096;
                    
                    models.add(new ModelBinder.ModelInfo(
                        id,
                        PROVIDER_ID,
                        id + " (Local)",
                        "Local LM Studio model",
                        "text",
                        ctxLen,
                        true,
                        30.0
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse LM Studio models: " + e.getMessage());
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
                
                if (options != null) {
                    if (options.temperature() != null) {
                        requestBody.put("temperature", options.temperature());
                    }
                    if (options.maxTokens() != null) {
                        requestBody.put("max_tokens", options.maxTokens());
                    }
                    if (options.topP() != null) {
                        requestBody.put("top_p", options.topP());
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/completions"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var node = mapper.readTree(response.body());
                    var choices = node.get("choices").get(0);
                    String content = choices.get("text").asText();
                    var usage = node.get("usage");
                    int tokens = usage != null ? usage.get("completion_tokens").asInt() : content.split("\\s+").length;
                    
                    return new ModelBinder.ModelResponse(
                        content,
                        modelId,
                        tokens,
                        System.currentTimeMillis() - start,
                        Map.of("provider", PROVIDER_ID, "model", modelId)
                    );
                } else {
                    throw new RuntimeException("LM Studio request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("LM Studio generation failed", e);
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
                
                if (options != null) {
                    if (options.temperature() != null) {
                        requestBody.put("temperature", options.temperature());
                    }
                    if (options.maxTokens() != null) {
                        requestBody.put("max_tokens", options.maxTokens());
                    }
                    if (options.topP() != null) {
                        requestBody.put("top_p", options.topP());
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var node = mapper.readTree(response.body());
                    var choices = node.get("choices").get(0);
                    var message = choices.get("message");
                    String content = message.get("content").asText();
                    var usage = node.get("usage");
                    int tokens = usage != null ? usage.get("completion_tokens").asInt() : content.split("\\s+").length;
                    
                    return new ModelBinder.ModelResponse(
                        content,
                        modelId,
                        tokens,
                        System.currentTimeMillis() - start,
                        Map.of("provider", PROVIDER_ID, "model", modelId)
                    );
                } else {
                    throw new RuntimeException("LM Studio chat request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("LM Studio chat failed", e);
            }
        });
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
                
                if (options != null) {
                    if (options.temperature() != null) {
                        requestBody.put("temperature", options.temperature());
                    }
                    if (options.maxTokens() != null) {
                        requestBody.put("max_tokens", options.maxTokens());
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/completions"))
                    .timeout(Duration.ofSeconds(300))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpClient streamingClient = HttpClient.newHttpClient();
                streamingClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        try {
                            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            response.body().forEach(line -> {
                                if (line.startsWith("data: ")) {
                                    try {
                                        String data = line.substring(6);
                                        if ("[DONE]".equals(data)) {
                                            sink.next(new ModelBinder.StreamingChunk("", true, null));
                                            sink.complete();
                                            return;
                                        }
                                        
                                        var node = mapper.readTree(data);
                                        var choices = node.get("choices");
                                        if (choices != null && choices.size() > 0) {
                                            String content = choices.get(0).has("text") 
                                                ? choices.get(0).get("text").asText()
                                                : choices.get(0).get("delta").get("content").asText();
                                            boolean done = choices.get(0).has("finish_reason");
                                            
                                            sink.next(new ModelBinder.StreamingChunk(content, done, null));
                                            
                                            if (done) {
                                                sink.complete();
                                            }
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
                .uri(URI.create(baseUrl + "/v1/models"))
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
