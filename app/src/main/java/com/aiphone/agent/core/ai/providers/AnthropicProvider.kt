package com.aiphone.agent.core.ai.providers

import com.aiphone.agent.data.remote.models.AnthropicChatRequest
import com.aiphone.agent.data.remote.models.AnthropicChatResponse
import com.aiphone.agent.data.remote.models.AnthropicMessage
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.MessageRole
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AnthropicProvider(
    private val apiKey: String,
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) : BaseAIProvider() {
    override val providerName = "Anthropic"
    private val reqAdapter = moshi.adapter(AnthropicChatRequest::class.java)
    private val respAdapter = moshi.adapter(AnthropicChatResponse::class.java)

    override suspend fun chat(messages: List<Message>, model: String, systemPrompt: String): Result<AIResponse> = runCatching {
        val anthropicMsgs = messages.filter { it.role != MessageRole.SYSTEM }.map { AnthropicMessage(it.role.name.lowercase(), it.content) }
        val body = AnthropicChatRequest(model = model, messages = anthropicMsgs, system = systemPrompt.takeIf { it.isNotBlank() })
        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .post(reqAdapter.toJson(body).toRequestBody("application/json".toMediaType()))
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .build()
        val resp = httpClient.newCall(req).execute()
        val respBody = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("Anthropic API error ${resp.code}: $respBody")
        val parsed = respAdapter.fromJson(respBody) ?: throw Exception("Parse error")
        AIResponse(parsed.content.firstOrNull { it.type == "text" }?.text ?: "", (parsed.usage?.inputTokens ?: 0) + (parsed.usage?.outputTokens ?: 0), parsed.model)
    }
}
