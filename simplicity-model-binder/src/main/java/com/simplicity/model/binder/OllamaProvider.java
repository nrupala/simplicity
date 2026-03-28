package com.simplicity.model.binder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OllamaProvider implements ModelBinder.ModelProvider {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "ollama";
    private static final String PROVIDER_NAME = "Ollama (Local)";

    public OllamaProvider(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeKnownModels();
    }

    private void initializeKnownModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("llama3", new ModelBinder.ModelInfo(
                "llama3", PROVIDER_ID, "Llama 3", "Meta's latest open model", "text", 8192, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("llama3:70b", new ModelBinder.ModelInfo(
                "llama3:70b", PROVIDER_ID, "Llama 3 70B", "Meta's large open model", "text", 8192, true, 200.0
            )),
            new AbstractMap.SimpleEntry<>("llama3.1", new ModelBinder.ModelInfo(
                "llama3.1", PROVIDER_ID, "Llama 3.1", "Meta's updated open model", "text", 128000, true, 60.0
            )),
            new AbstractMap.SimpleEntry<>("llama3.2", new ModelBinder.ModelInfo(
                "llama3.2", PROVIDER_ID, "Llama 3.2", "Meta's latest with vision", "multimodal", 128000, true, 70.0
            )),
            new AbstractMap.SimpleEntry<>("mistral", new ModelBinder.ModelInfo(
                "mistral", PROVIDER_ID, "Mistral 7B", "Efficient open model", "text", 8192, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("mixtral", new ModelBinder.ModelInfo(
                "mixtral", PROVIDER_ID, "Mixtral 8x7B", "Mixture of experts", "text", 32768, true, 80.0
            )),
            new AbstractMap.SimpleEntry<>("codellama", new ModelBinder.ModelInfo(
                "codellama", PROVIDER_ID, "Code Llama", "Code-specialized model", "text", 16384, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("phi3", new ModelBinder.ModelInfo(
                "phi3", PROVIDER_ID, "Phi-3 Mini", "Microsoft's efficient model", "text", 4096, true, 30.0
            )),
            new AbstractMap.SimpleEntry<>("phi3:medium", new ModelBinder.ModelInfo(
                "phi3:medium", PROVIDER_ID, "Phi-3 Medium", "Microsoft's medium model", "text", 4096, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("qwen2", new ModelBinder.ModelInfo(
                "qwen2", PROVIDER_ID, "Qwen 2", "Alibaba's latest model", "text", 32768, true, 45.0
            )),
            new AbstractMap.SimpleEntry<>("qwen2.5-coder", new ModelBinder.ModelInfo(
                "qwen2.5-coder", PROVIDER_ID, "Qwen 2.5 Coder", "Alibaba's code model", "text", 32768, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("gemma2", new ModelBinder.ModelInfo(
                "gemma2", PROVIDER_ID, "Gemma 2", "Google's open model", "text", 8192, true, 45.0
            )),
            new AbstractMap.SimpleEntry<>("gemma2:27b", new ModelBinder.ModelInfo(
                "gemma2:27b", PROVIDER_ID, "Gemma 2 27B", "Google's large open model", "text", 8192, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("nomic-embed-text", new ModelBinder.ModelInfo(
                "nomic-embed-text", PROVIDER_ID, "Nomic Embed Text", "High-quality embeddings", "embedding", 8192, true, 20.0
            )),
            new AbstractMap.SimpleEntry<>("mxbai-embed-large", new ModelBinder.ModelInfo(
                "mxbai-embed-large", PROVIDER_ID, "Mixed Bread Embeddings", "High-performance embeddings", "embedding", 512, true, 15.0
            )),
            new AbstractMap.SimpleEntry<>("deepseek-coder-v2", new ModelBinder.ModelInfo(
                "deepseek-coder-v2", PROVIDER_ID, "DeepSeek Coder V2", "Advanced code generation", "text", 163840, true, 90.0
            )),
            new AbstractMap.SimpleEntry<>("command-r-plus", new ModelBinder.ModelInfo(
                "command-r-plus", PROVIDER_ID, "Command R+", "Cohere's research model", "text", 128000, true, 70.0
            )),
            new AbstractMap.SimpleEntry<>("wizardlm2", new ModelBinder.ModelInfo(
                "wizardlm2", PROVIDER_ID, "WizardLM 2", "High-quality instruction model", "text", 32768, true, 55.0
            )),
            new AbstractMap.SimpleEntry<>("stablelm2", new ModelBinder.ModelInfo(
                "stablelm2", PROVIDER_ID, "Stable LM 2", "Stability AI's model", "text", 8192, true, 35.0
            )),
            new AbstractMap.SimpleEntry<>("aya", new ModelBinder.ModelInfo(
                "aya", PROVIDER_ID, "Aya", "Multilingual model", "text", 8192, true, 40.0
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
                .uri(URI.create(baseUrl + "/api/tags"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                List<ModelBinder.ModelInfo> discovered = parseLocalModels(response.body());
                discovered.forEach(model -> availableModels.put(model.modelId(), model));
                return discovered;
            }
        } catch (Exception e) {
            System.err.println("Ollama discovery failed: " + e.getMessage());
        }
        return getAvailableModels();
    }

    private List<ModelBinder.ModelInfo> parseLocalModels(String json) {
        List<ModelBinder.ModelInfo> models = new ArrayList<>();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(json);
            var modelsNode = node.get("models");
            
            if (modelsNode != null && modelsNode.isArray()) {
                for (var modelNode : modelsNode) {
                    String name = modelNode.get("name").asText();
                    String modelName = name.contains(":") ? name.split(":")[1] : name;
                    
                    long size = modelNode.has("size") ? modelNode.get("size").asLong() : 0;
                    double sizeGB = size / (1024.0 * 1024.0 * 1024.0);
                    
                    models.add(new ModelBinder.ModelInfo(
                        name,
                        PROVIDER_ID,
                        modelName + " (Local)",
                        "Local Ollama model (" + String.format("%.1f GB", sizeGB) + ")",
                        "text",
                        8192,
                        true,
                        Math.max(20, sizeGB * 10)
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Ollama models: " + e.getMessage());
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
                        requestBody.put("options", Map.of("num_predict", options.maxTokens()));
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var node = mapper.readTree(response.body());
                    String content = node.get("response").asText();
                    int tokens = node.has("eval_count") ? node.get("eval_count").asInt() : content.split("\\s+").length;
                    
                    return new ModelBinder.ModelResponse(
                        content,
                        modelId,
                        tokens,
                        System.currentTimeMillis() - start,
                        Map.of("provider", PROVIDER_ID, "model", modelId)
                    );
                } else {
                    throw new RuntimeException("Ollama request failed: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Ollama generation failed", e);
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
                
                List<Map<String, String>> ollamaMessages = new ArrayList<>();
                for (ModelBinder.ChatMessage msg : messages) {
                    ollamaMessages.add(Map.of("role", msg.role(), "content", msg.content()));
                }
                requestBody.put("messages", ollamaMessages);
                
                if (options != null) {
                    if (options.temperature() != null) {
                        requestBody.put("temperature", options.temperature());
                    }
                    if (options.maxTokens() != null) {
                        requestBody.put("options", Map.of("num_predict", options.maxTokens()));
                    }
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/chat"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var node = mapper.readTree(response.body());
                    var messageNode = node.get("message");
                    String content = messageNode.get("content").asText();
                    int tokens = node.has("eval_count") ? node.get("eval_count").asInt() : content.split("\\s+").length;
                    
                    return new ModelBinder.ModelResponse(
                        content,
                        modelId,
                        tokens,
                        System.currentTimeMillis() - start,
                        Map.of("provider", PROVIDER_ID, "model", modelId)
                    );
                } else {
                    throw new RuntimeException("Ollama chat request failed: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Ollama chat failed", e);
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
                
                if (options != null && options.temperature() != null) {
                    requestBody.put("temperature", options.temperature());
                }
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
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
                                if (!line.isBlank()) {
                                    try {
                                        var node = mapper.readTree(line);
                                        String content = node.get("response").asText();
                                        boolean done = node.has("done") && node.get("done").asBoolean();
                                        
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
                .uri(URI.create(baseUrl + "/api/tags"))
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
