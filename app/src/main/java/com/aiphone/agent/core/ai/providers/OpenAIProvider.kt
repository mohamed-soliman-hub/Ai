package com.aiphone.agent.core.ai.providers

import com.aiphone.agent.data.remote.models.OpenAIChatRequest
import com.aiphone.agent.data.remote.models.OpenAIChatResponse
import com.aiphone.agent.data.remote.models.OpenAIMessage
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.MessageRole
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAIProvider(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1/",
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) : BaseAIProvider() {
    override val providerName = "OpenAI"
    private val reqAdapter = moshi.adapter(OpenAIChatRequest::class.java)
    private val respAdapter = moshi.adapter(OpenAIChatResponse::class.java)

    override suspend fun chat(messages: List<Message>, model: String, systemPrompt: String): Result<AIResponse> = runCatching {
        val all = mutableListOf<OpenAIMessage>()
        if (systemPrompt.isNotBlank()) all.add(OpenAIMessage("system", systemPrompt))
        all.addAll(messages.filter { it.role != MessageRole.SYSTEM }.map { OpenAIMessage(it.role.name.lowercase(), it.content) })
        val body = OpenAIChatRequest(model = model, messages = all)
        val req = Request.Builder()
            .url("${baseUrl}chat/completions")
            .post(reqAdapter.toJson(body).toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer $apiKey")
            .build()
        val resp = httpClient.newCall(req).execute()
        val respBody = resp.body?.string() ?: throw Exception("Empty response")
        if (!resp.isSuccessful) throw Exception("OpenAI API error ${resp.code}: $respBody")
        val parsed = respAdapter.fromJson(respBody) ?: throw Exception("Parse error")
        AIResponse(parsed.choices.firstOrNull()?.message?.content ?: "", parsed.usage?.totalTokens ?: 0, parsed.model)
    }
}
