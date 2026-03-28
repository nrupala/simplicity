package com.simplicity.android.data.model

data class Provider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val isConnected: Boolean = false,
    val latencyMs: Long? = null,
    val models: List<Model> = emptyList(),
    val error: String? = null
)

data class Model(
    val name: String,
    val size: String? = null,
    val contextLength: Int? = null
)

data class OllamaModelsResponse(
    val models: List<OllamaModel>
)

data class OllamaModel(
    val name: String,
    val size: Long,
    val modified_at: String
)

data class ChatRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val options: GenerationOptions? = null
)

data class GenerationOptions(
    val temperature: Double = 0.7,
    val maxTokens: Int = 500,
    val topP: Double = 0.9
)

data class ChatResponse(
    val model: String,
    val response: String,
    val done: Boolean = true,
    val context: List<Int>? = null,
    val totalDuration: Long? = null,
    val loadDuration: Long? = null,
    val promptEvalCount: Int? = null,
    val evalCount: Int? = null
)
