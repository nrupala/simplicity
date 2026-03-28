package com.simplicity.desktop;

import com.simplicity.model.binder.ModelBinder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

public class Main extends Application {

    private ModelBinder modelBinder;
    private ComboBox<String> providerSelector;
    private ComboBox<String> modelSelector;
    private TextArea chatArea;
    private TextField inputField;
    private Label statusLabel;
    private Label latencyLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simplicity AI - Local Model Binder");
        
        // Initialize model binder
        modelBinder = new ModelBinder();
        
        // Build UI
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        Label header = new Label("Simplicity AI - Local Model Binder");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #6366f1;");
        
        // Provider section
        HBox providerBox = new HBox(10);
        providerBox.getChildren().addAll(
            new Label("Provider:"),
            providerSelector = new ComboBox<>()
        );
        
        // Model section  
        HBox modelBox = new HBox(10);
        modelBox.getChildren().addAll(
            new Label("Model:"),
            modelSelector = new ComboBox<>()
        );

        // Status section
        HBox statusBox = new HBox(20);
        statusLabel = new Label("Checking providers...");
        latencyLabel = new Label("");
        statusBox.getChildren().addAll(statusLabel, latencyLabel);

        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefRowCount(10);
        chatArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");

        // Input section
        HBox inputBox = new HBox(10);
        inputField = new TextField();
        inputField.setPromptText("Enter your question...");
        inputField.setPrefWidth(400);
        
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-padding: 8 20;");
        sendButton.setOnAction(e -> sendMessage());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> chatArea.clear());
        
        inputBox.getChildren().addAll(inputField, sendButton, clearButton);

        // Assemble layout
        root.getChildren().addAll(
            header,
            new Separator(),
            providerBox,
            modelBox,
            statusBox,
            new Separator(),
            chatArea,
            inputBox
        );

        // Load providers
        loadProviders();

        // Setup event handlers
        providerSelector.setOnAction(e -> loadModels());
        modelSelector.setOnAction(e -> updateStatus());

        // Create scene
        Scene scene = new Scene(root, 700, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadProviders() {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                // Register local providers
                modelBinder.ollama("http://localhost:11434");
                modelBinder.lmStudio("http://localhost:1234");
                
                // Get available providers
                return List.of("ollama", "lmstudio");
            }
        };

        task.setOnSucceeded(e -> {
            providerSelector.getItems().addAll(task.getValue());
            if (!task.getValue().isEmpty()) {
                providerSelector.getSelectionModel().selectFirst();
                loadModels();
            }
        });

        new Thread(task).start();
    }

    private void loadModels() {
        String provider = providerSelector.getSelectionModel().getSelectedItem();
        if (provider == null) return;

        modelSelector.getItems().clear();
        
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                // Discover models from provider
                Map<String, ModelBinder.ProviderHealth> health = modelBinder.getProviderHealth();
                ModelBinder.ProviderHealth providerHealth = health.get(provider);
                
                if (providerHealth != null && providerHealth.isHealthy()) {
                    return List.of("llama3", "mistral", "phi3", "deepseek-r1:8b");
                }
                return List.of();
            }
        };

        task.setOnSucceeded(e -> {
            modelSelector.getItems().addAll(task.getValue());
            if (!task.getValue().isEmpty()) {
                modelSelector.getSelectionModel().selectFirst();
            }
            updateStatus();
        });

        new Thread(task).start();
    }

    private void updateStatus() {
        String provider = providerSelector.getSelectionModel().getSelectedItem();
        String model = modelSelector.getSelectionModel().getSelectedItem();
        
        Map<String, ModelBinder.ProviderHealth> health = modelBinder.getProviderHealth();
        ModelBinder.ProviderHealth providerHealth = health.get(provider);
        
        if (providerHealth != null && providerHealth.isHealthy()) {
            statusLabel.setText("Status: Connected");
            statusLabel.setStyle("-fx-text-fill: #10b981;");
            latencyLabel.setText("Latency: " + providerHealth.avgLatencyMs() + "ms");
        } else {
            statusLabel.setText("Status: Not Connected");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            latencyLabel.setText("Start " + provider + " to continue");
        }
    }

    private void sendMessage() {
        String prompt = inputField.getText().trim();
        if (prompt.isEmpty()) return;

        String provider = providerSelector.getSelectionModel().getSelectedItem();
        String model = modelSelector.getSelectionModel().getSelectedItem();

        chatArea.appendText("\n[You] " + prompt + "\n");
        inputField.clear();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                ModelBinder.GenerationOptions options = ModelBinder.GenerationOptions.forChat();
                ModelBinder.ModelResponse response = modelBinder.generate(model, prompt, options).join();
                return response.content();
            }
        };

        task.setOnSucceeded(e -> {
            chatArea.appendText("[AI] " + task.getValue() + "\n");
        });

        task.setOnFailed(e -> {
            chatArea.appendText("[Error] " + task.getException().getMessage() + "\n");
        });

        new Thread(task).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
