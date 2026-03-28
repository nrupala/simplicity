package com.simplicity.model.binder;

import java.util.*;
import java.util.concurrent.*;

public class ModelBinderDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  SIMPLICITY MODEL BINDER - LIVE DEMO");
        System.out.println("=".repeat(60));
        System.out.println();

        ModelBinder binder = new ModelBinder();

        System.out.println("[1] Checking available providers...\n");
        
        System.out.println("  Local Providers:");
        System.out.println("    - Ollama (http://localhost:11434)");
        System.out.println("    - LM Studio (http://localhost:1234)");
        System.out.println();

        System.out.println("[2] Testing Ollama Provider...");
        System.out.println("-".repeat(40));
        testOllama(binder);
        
        System.out.println();
        System.out.println("[3] Testing LM Studio Provider...");
        System.out.println("-".repeat(40));
        testLMStudio(binder);
        
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("  DEMO COMPLETE");
        System.out.println("=".repeat(60));
    }

    private static void testOllama(ModelBinder binder) {
        OllamaProvider ollama = new OllamaProvider("http://localhost:11434");
        ModelBinder.ProviderHealth health = ollama.healthCheck();
        
        System.out.println("  Health Check: " + (health.isHealthy() ? "✓ CONNECTED" : "✗ FAILED"));
        if (health.avgLatencyMs() != null) {
            System.out.println("  Latency: " + health.avgLatencyMs() + "ms");
        }
        if (health.errorMessage() != null) {
            System.out.println("  Error: " + health.errorMessage());
            System.out.println("  → Is Ollama running? Run: ollama serve");
        }
        
        if (health.isHealthy()) {
            List<ModelBinder.ModelInfo> models = ollama.discoverModels();
            System.out.println("  Discovered " + models.size() + " local models:");
            for (int i = 0; i < Math.min(5, models.size()); i++) {
                ModelBinder.ModelInfo m = models.get(i);
                System.out.println("    • " + m.modelId() + " (" + m.displayName() + ")");
            }
            if (models.size() > 5) {
                System.out.println("    ... and " + (models.size() - 5) + " more");
            }
            
            System.out.println();
            System.out.println("  Testing chat with first model...");
            try {
                List<ModelBinder.ChatMessage> messages = List.of(
                    new ModelBinder.ChatMessage("user", "Say 'Hello from Ollama!' in exactly those words.")
                );
                
                String modelId = models.get(0).modelId();
                System.out.println("  Using model: " + modelId);
                
                CompletableFuture<ModelBinder.ModelResponse> future = 
                    ollama.chat(modelId, messages, ModelBinder.GenerationOptions.forChat());
                
                ModelBinder.ModelResponse response = future.get(60, TimeUnit.SECONDS);
                
                System.out.println("  Response: " + response.content());
                System.out.println("  Tokens: " + response.tokensUsed() + " | Latency: " + response.latencyMs() + "ms");
                
            } catch (Exception e) {
                System.out.println("  Chat test failed: " + e.getMessage());
            }
        }
    }

    private static void testLMStudio(ModelBinder binder) {
        LMStudioProvider lmstudio = new LMStudioProvider("http://localhost:1234");
        ModelBinder.ProviderHealth health = lmstudio.healthCheck();
        
        System.out.println("  Health Check: " + (health.isHealthy() ? "✓ CONNECTED" : "✗ FAILED"));
        if (health.avgLatencyMs() != null) {
            System.out.println("  Latency: " + health.avgLatencyMs() + "ms");
        }
        if (health.errorMessage() != null) {
            System.out.println("  Error: " + health.errorMessage());
            System.out.println("  → Is LM Studio running? Open LM Studio and load a model.");
        }
        
        if (health.isHealthy()) {
            List<ModelBinder.ModelInfo> models = lmstudio.discoverModels();
            System.out.println("  Discovered " + models.size() + " local models:");
            for (int i = 0; i < Math.min(5, models.size()); i++) {
                ModelBinder.ModelInfo m = models.get(i);
                System.out.println("    • " + m.modelId() + " (" + m.displayName() + ")");
            }
            
            System.out.println();
            System.out.println("  Testing chat with first model...");
            try {
                List<ModelBinder.ChatMessage> messages = List.of(
                    new ModelBinder.ChatMessage("user", "Say 'Hello from LM Studio!' in exactly those words.")
                );
                
                String modelId = models.get(0).modelId();
                System.out.println("  Using model: " + modelId);
                
                CompletableFuture<ModelBinder.ModelResponse> future = 
                    lmstudio.chat(modelId, messages, ModelBinder.GenerationOptions.forChat());
                
                ModelBinder.ModelResponse response = future.get(60, TimeUnit.SECONDS);
                
                System.out.println("  Response: " + response.content());
                System.out.println("  Tokens: " + response.tokensUsed() + " | Latency: " + response.latencyMs() + "ms");
                
            } catch (Exception e) {
                System.out.println("  Chat test failed: " + e.getMessage());
            }
        }
    }
}
