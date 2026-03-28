package com.simplicity.android.data.repository

import com.simplicity.android.data.api.*
import com.simplicity.android.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ModelRepository {
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    private val ollamaRetrofit = Retrofit.Builder()
        .baseUrl("http://localhost:11434/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val lmStudioRetrofit = Retrofit.Builder()
        .baseUrl("http://localhost:1234/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val ollamaApi = ollamaRetrofit.create(OllamaApi::class.java)
    private val lmStudioApi = lmStudioRetrofit.create(LMStudioApi::class.java)
    
    suspend fun discoverProviders(): List<Provider> = withContext(Dispatchers.IO) {
        val providers = mutableListOf<Provider>()
        
        // Check Ollama
        providers.add(checkOllama())
        
        // Check LM Studio
        providers.add(checkLMStudio())
        
        return@withContext providers
    }
    
    private suspend fun checkOllama(): Provider {
        return try {
            val startTime = System.currentTimeMillis()
            val response = ollamaApi.getModels()
            val latency = System.currentTimeMillis() - startTime
            
            Provider(
                id = "ollama",
                name = "Ollama",
                baseUrl = "http://localhost:11434",
                isConnected = true,
                latencyMs = latency,
                models = response.models.map { model ->
                    Model(
                        name = model.name,
                        size = formatSize(model.size)
                    )
                }
            )
        } catch (e: Exception) {
            Provider(
                id = "ollama",
                name = "Ollama",
                baseUrl = "http://localhost:11434",
                isConnected = false,
                error = e.message ?: "Connection failed"
            )
        }
    }
    
    private suspend fun checkLMStudio(): Provider {
        return try {
            val startTime = System.currentTimeMillis()
            val response = lmStudioApi.getModels()
            val latency = System.currentTimeMillis() - startTime
            
            Provider(
                id = "lmstudio",
                name = "LM Studio",
                baseUrl = "http://localhost:1234",
                isConnected = true,
                latencyMs = latency,
                models = response.data.map { model ->
                    Model(
                        name = model.id,
                        contextLength = model.context_window
                    )
                }
            )
        } catch (e: Exception) {
            Provider(
                id = "lmstudio",
                name = "LM Studio",
                baseUrl = "http://localhost:1234",
                isConnected = false,
                error = e.message ?: "Connection failed"
            )
        }
    }
    
    suspend fun generate(provider: Provider, model: String, prompt: String): Result<String> = 
        withContext(Dispatchers.IO) {
            try {
                val request = ChatRequest(
                    model = model,
                    prompt = prompt,
                    stream = false
                )
                
                val response = when (provider.id) {
                    "ollama" -> ollamaApi.generate(request)
                    "lmstudio" -> {
                        val openAIRequest = OpenAIRequest(
                            model = model,
                            prompt = prompt
                        )
                        val openAIResponse = lmStudioApi.complete(openAIRequest)
                        ChatResponse(
                            model = model,
                            response = openAIResponse.choices.firstOrNull()?.text ?: "",
                            done = true
                        )
                    }
                    else -> throw IllegalArgumentException("Unknown provider: ${provider.id}")
                }
                
                Result.success(response.response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    private fun formatSize(bytes: Long): String {
        val gb = bytes.toDouble() / (1024 * 1024 * 1024)
        return "%.1f GB".format(gb)
    }
}
