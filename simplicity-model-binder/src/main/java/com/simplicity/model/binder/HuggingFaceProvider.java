package com.simplicity.model.binder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Flux;

public class HuggingFaceProvider implements ModelBinder.ModelProvider {

    private final String apiKey;
    private final String baseUrl = "https://api-inference.huggingface.co";
    private final HttpClient httpClient;
    private final Map<String, ModelBinder.ModelInfo> availableModels = new HashMap<>();

    private static final String PROVIDER_ID = "huggingface";
    private static final String PROVIDER_NAME = "HuggingFace";

    public HuggingFaceProvider(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        initializeModels();
    }

    private void initializeModels() {
        availableModels.putAll(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("meta-llama/Llama-3.1-70B-Instruct", new ModelBinder.ModelInfo(
                "meta-llama/Llama-3.1-70B-Instruct", PROVIDER_ID, "Llama 3.1 70B Instruct", 
                "Meta's large open model", "text", 128000, true, 400.0
            )),
            new AbstractMap.SimpleEntry<>("meta-llama/Llama-3.1-8B-Instruct", new ModelBinder.ModelInfo(
                "meta-llama/Llama-3.1-8B-Instruct", PROVIDER_ID, "Llama 3.1 8B Instruct", 
                "Meta's medium open model", "text", 128000, true, 80.0
            )),
            new AbstractMap.SimpleEntry<>("meta-llama/Llama-3.2-3B-Instruct", new ModelBinder.ModelInfo(
                "meta-llama/Llama-3.2-3B-Instruct", PROVIDER_ID, "Llama 3.2 3B Instruct", 
                "Meta's efficient instruct model", "text", 128000, true, 40.0
            )),
            new AbstractMap.SimpleEntry<>("mistralai/Mistral-7B-Instruct-v0.3", new ModelBinder.ModelInfo(
                "mistralai/Mistral-7B-Instruct-v0.3", PROVIDER_ID, "Mistral 7B Instruct v0.3", 
                "Efficient instruction model", "text", 32768, true, 60.0
            )),
            new AbstractMap.SimpleEntry<>("mistralai/Mixtral-8x7B-Instruct-v0.1", new ModelBinder.ModelInfo(
                "mistralai/Mixtral-8x7B-Instruct-v0.1", PROVIDER_ID, "Mixtral 8x7B Instruct", 
                "Mixture of experts model", "text", 32768, true, 150.0
            )),
            new AbstractMap.SimpleEntry<>("microsoft/Phi-3-mini-128k-instruct", new ModelBinder.ModelInfo(
                "microsoft/Phi-3-mini-128k-instruct", PROVIDER_ID, "Phi-3 Mini 128K", 
                "Microsoft's efficient model", "text", 128000, true, 50.0
            )),
            new AbstractMap.SimpleEntry<>("Qwen/Qwen2.5-72B-Instruct", new ModelBinder.ModelInfo(
                "Qwen/Qwen2.5-72B-Instruct", PROVIDER_ID, "Qwen 2.5 72B Instruct", 
                "Alibaba's large model", "text", 32768, true, 350.0
            )),
            new AbstractMap.SimpleEntry<>("Qwen/Qwen2.5-7B-Instruct", new ModelBinder.ModelInfo(
                "Qwen/Qwen2.5-7B-Instruct", PROVIDER_ID, "Qwen 2.5 7B Instruct", 
                "Alibaba's medium model", "text", 32768, true, 60.0
            )),
            new AbstractMap.SimpleEntry<>("google/gemma-2-27b-it", new ModelBinder.ModelInfo(
                "google/gemma-2-27b-it", PROVIDER_ID, "Gemma 2 27B Instruct", 
                "Google's open model", "text", 8192, true, 200.0
            )),
            new AbstractMap.SimpleEntry<>("google/gemma-2-9b-it", new ModelBinder.ModelInfo(
                "google/gemma-2-9b-it", PROVIDER_ID, "Gemma 2 9B Instruct", 
                "Google's efficient model", "text", 8192, true, 80.0
            )),
            new AbstractMap.SimpleEntry<>("bigcode/starcoder2-15b", new ModelBinder.ModelInfo(
                "bigcode/starcoder2-15b", PROVIDER_ID, "StarCoder2 15B", 
                "Code generation model", "text", 16384, true, 100.0
            )),
            new AbstractMap.SimpleEntry<>("deepseek-ai/DeepSeek-V2.5", new ModelBinder.ModelInfo(
                "deepseek-ai/DeepSeek-V2.5", PROVIDER_ID, "DeepSeek V2.5", 
                "Advanced open model", "text", 128000, true, 200.0
            )),
            new AbstractMap.SimpleEntry<>("CohereForAI/aya-expanse-32b", new ModelBinder.ModelInfo(
                "CohereForAI/aya-expanse-32b", PROVIDER_ID, "Aya Expanse 32B", 
                "Multilingual model", "text", 8192, true, 180.0
            )),
            new AbstractMap.SimpleEntry<>("sentence-transformers/all-MiniLM-L6-v2", new ModelBinder.ModelInfo(
                "sentence-transformers/all-MiniLM-L6-v2", PROVIDER_ID, "MiniLM L6 Embeddings", 
                "Fast embeddings model", "embedding", 512, true, 15.0
            )),
            new AbstractMap.SimpleEntry<>("BAAI/bge-large-en-v1.5", new ModelBinder.ModelInfo(
                "BAAI/bge-large-en-v1.5", PROVIDER_ID, "BGE Large Embeddings", 
                "High-quality embeddings", "embedding", 512, true, 30.0
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
                .uri(URI.create("https://huggingface.co/api/models?sort=downloads&direction=-1&limit=50"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseModelsResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("HuggingFace model discovery failed: " + e.getMessage());
        }
        return getAvailableModels();
    }

    private List<ModelBinder.ModelInfo> parseModelsResponse(String json) {
        List<ModelBinder.ModelInfo> models = new ArrayList<>();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var array = mapper.readTree(json);
            
            if (array.isArray()) {
                for (var modelNode : array) {
                    String id = modelNode.get("id").asText();
                    String modelType = modelNode.has("model_type") ? modelNode.get("model_type").asText() : "text";
                    int ctxLen = modelNode.has("max_model_length") ? modelNode.get("max_model_length").asInt() : 4096;
                    int downloads = modelNode.has("downloads") ? modelNode.get("downloads").asInt() : 0;
                    
                    models.add(new ModelBinder.ModelInfo(
                        id,
                        PROVIDER_ID,
                        id.split("/")[1],
                        downloads + " downloads",
                        modelType,
                        ctxLen,
                        true,
                        100.0
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse HuggingFace models: " + e.getMessage());
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
                requestBody.put("inputs", prompt);
                requestBody.put("parameters", buildParameters(options));
                requestBody.put("options", Map.of("use_cache", false, "wait_for_model", true));
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/models/" + modelId))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseResponse(response.body(), modelId, start);
                } else if (response.statusCode() == 503) {
                    throw new RuntimeException("Model is loading, please wait and retry");
                } else {
                    throw new RuntimeException("HuggingFace request failed: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("HuggingFace generation failed", e);
            }
        });
    }

    @Override
    public CompletableFuture<ModelBinder.ModelResponse> chat(
            String modelId, List<ModelBinder.ChatMessage> messages, ModelBinder.GenerationOptions options) {
        
        String prompt = buildChatPrompt(messages);
        return generate(modelId, prompt, options);
    }

    private String buildChatPrompt(List<ModelBinder.ChatMessage> messages) {
        StringBuilder prompt = new StringBuilder();
        for (ModelBinder.ChatMessage msg : messages) {
            String role = msg.role();
            if ("user".equals(role)) {
                prompt.append("<|user|>\n").append(msg.content()).append("\n");
            } else if ("assistant".equals(role)) {
                prompt.append("<|assistant|>\n").append(msg.content()).append("\n");
            } else if ("system".equals(role)) {
                prompt.append("<|system|>\n").append(msg.content()).append("\n");
            }
        }
        prompt.append("<|assistant|>\n");
        return prompt.toString();
    }

    private Map<String, Object> buildParameters(ModelBinder.GenerationOptions options) {
        Map<String, Object> params = new HashMap<>();
        params.put("return_full_text", false);
        
        if (options != null) {
            if (options.temperature() != null) {
                params.put("temperature", options.temperature());
            }
            if (options.maxTokens() != null) {
                params.put("max_new_tokens", options.maxTokens());
            }
            if (options.topP() != null) {
                params.put("top_p", options.topP());
            }
            if (options.repetitionPenalty() != null) {
                params.put("repetition_penalty", options.repetitionPenalty());
            }
        }
        
        return params;
    }

    private ModelBinder.ModelResponse parseResponse(String json, String modelId, long start) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        
        String content;
        if (json.startsWith("[")) {
            var array = mapper.readTree(json);
            if (array.isArray() && array.size() > 0) {
                content = array.get(0).has("generated_text") 
                    ? array.get(0).get("generated_text").asText() 
                    : array.get(0).asText();
            } else {
                content = json;
            }
        } else {
            var node = mapper.readTree(json);
            content = node.has("generated_text") ? node.get("generated_text").asText() : json;
        }
        
        int tokens = (int) (content.split("\\s+").length * 1.3);
        
        return new ModelBinder.ModelResponse(
            content,
            modelId,
            (int) tokens,
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
                requestBody.put("inputs", prompt);
                
                Map<String, Object> params = buildParameters(options);
                params.put("stream", true);
                requestBody.put("parameters", params);
                requestBody.put("options", Map.of("use_cache", false, "wait_for_model", true));
                
                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/models/" + modelId))
                    .timeout(Duration.ofSeconds(300))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
                
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        try {
                            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            response.body().forEach(line -> {
                                if (!line.isBlank()) {
                                    try {
                                        var node = mapper.readTree(line);
                                        String token = node.has("token") && node.get("token").has("text")
                                            ? node.get("token").get("text").asText()
                                            : node.has("generated_text")
                                                ? node.get("generated_text").asText()
                                                : "";
                                        boolean done = node.has("done") && node.get("done").asBoolean();
                                        
                                        sink.next(new ModelBinder.StreamingChunk(token, done, null));
                                        
                                        if (done) {
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
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/status/" + availableModels.keySet().iterator().next()))
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
