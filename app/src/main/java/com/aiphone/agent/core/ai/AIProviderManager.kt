package com.aiphone.agent.core.ai

import com.aiphone.agent.core.ai.providers.AIResponse
import com.aiphone.agent.core.ai.providers.AnthropicProvider
import com.aiphone.agent.core.ai.providers.BaseAIProvider
import com.aiphone.agent.core.ai.providers.GeminiProvider
import com.aiphone.agent.core.ai.providers.OpenAIProvider
import com.aiphone.agent.data.local.preferences.SecurePreferences
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.ProviderType
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIProviderManager @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) {
    private fun buildProvider(type: ProviderType): BaseAIProvider {
        val apiKey = securePreferences.getApiKey(type)
        return when (type) {
            ProviderType.OPENAI -> OpenAIProvider(apiKey, httpClient = httpClient, moshi = moshi)
            ProviderType.ANTHROPIC -> AnthropicProvider(apiKey, httpClient = httpClient, moshi = moshi)
            ProviderType.GEMINI -> GeminiProvider(apiKey, httpClient = httpClient, moshi = moshi)
            ProviderType.OPENROUTER -> OpenAIProvider(apiKey, "https://openrouter.ai/api/v1/", httpClient, moshi)
        }
    }

    suspend fun chat(messages: List<Message>, systemPrompt: String): Result<AIResponse> {
        val providerType = securePreferences.getSelectedProvider()
        val model = securePreferences.getSelectedModel(providerType)
        return buildProvider(providerType).chat(messages, model, systemPrompt)
    }

    fun getCurrentProviderType() = securePreferences.getSelectedProvider()
    fun getCurrentModel(): String { val t = securePreferences.getSelectedProvider(); return securePreferences.getSelectedModel(t) }
    fun hasValidApiKey() = securePreferences.hasApiKey(securePreferences.getSelectedProvider())
}
