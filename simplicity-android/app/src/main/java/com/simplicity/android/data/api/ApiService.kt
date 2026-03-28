package com.simplicity.android.data.api

import com.simplicity.android.data.model.*
import retrofit2.http.*

interface OllamaApi {
    @GET("api/tags")
    suspend fun getModels(): OllamaModelsResponse
    
    @POST("api/generate")
    suspend fun generate(@Body request: ChatRequest): ChatResponse
    
    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}

interface LMStudioApi {
    @GET("v1/models")
    suspend fun getModels(): ModelsResponse
    
    @POST("v1/completions")
    suspend fun complete(@Body request: OpenAIRequest): OpenAIResponse
    
    @POST("v1/chat/completions")
    suspend fun chat(@Body request: OpenAIChatRequest): OpenAIChatResponse
}

data class ModelsResponse(
    val data: List<ModelInfo>
)

data class ModelInfo(
    val id: String,
    val context_window: Int?
)

data class OpenAIRequest(
    val model: String,
    val prompt: String,
    val max_tokens: Int = 500,
    val temperature: Double = 0.7
)

data class OpenAIChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int = 500,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val id: String?,
    val choices: List<Choice>
)

data class OpenAIChatResponse(
    val id: String?,
    val choices: List<ChatChoice>
)

data class Choice(
    val text: String,
    val index: Int,
    val finish_reason: String?
)

data class ChatChoice(
    val message: Message,
    val index: Int,
    val finish_reason: String?
)
