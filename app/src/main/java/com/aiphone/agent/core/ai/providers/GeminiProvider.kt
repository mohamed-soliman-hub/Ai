package com.aiphone.agent.core.ai.providers

import com.aiphone.agent.data.remote.models.*
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.MessageRole
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiProvider(
    private val apiKey: String,
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) : BaseAIProvider() {
    override val providerName = "Google Gemini"
    private val reqAdapter = moshi.adapter(GeminiRequest::class.java)
    private val respAdapter = moshi.adapter(GeminiResponse::class.java)

    override suspend fun chat(messages: List<Message>, model: String, systemPrompt: String): Result<AIResponse> = runCatching {
        val contents = messages.filter { it.role != MessageRole.SYSTEM }.map { msg ->
            GeminiContent(role = if (msg.role == MessageRole.USER) "user" else "model", parts = listOf(GeminiPart(msg.content)))
        }
        val body = GeminiRequest(contents = contents, systemInstruction = if (systemPrompt.isNotBlank()) GeminiContent(parts = listOf(GeminiPart(systemPrompt))) else null)
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
            .post(reqAdapter.toJson(body).toRequestBody("application/json".toMediaType()))
            .build()
        val resp = httpClient.newCall(req).execute()
        val respBody = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("Gemini API error ${resp.code}: $respBody")
        val parsed = respAdapter.fromJson(respBody) ?: throw Exception("Parse error")
        val text = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        AIResponse(text, parsed.usageMetadata?.totalTokenCount ?: 0, model)
    }
}
