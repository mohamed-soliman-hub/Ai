package com.aiphone.agent.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── OpenAI / OpenRouter Request ────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    @Json(name = "max_tokens") val maxTokens: Int = 4096,
    val temperature: Double = 0.7,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class OpenAIMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAIChatResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int,
    val message: OpenAIMessage,
    @Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens") val promptTokens: Int,
    @Json(name = "completion_tokens") val completionTokens: Int,
    @Json(name = "total_tokens") val totalTokens: Int
)

// ── Anthropic Request ──────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class AnthropicChatRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    @Json(name = "max_tokens") val maxTokens: Int = 4096,
    val system: String? = null
)

@JsonClass(generateAdapter = true)
data class AnthropicMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class AnthropicChatResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<AnthropicContent>,
    val model: String,
    @Json(name = "stop_reason") val stopReason: String?,
    val usage: AnthropicUsage?
)

@JsonClass(generateAdapter = true)
data class AnthropicContent(
    val type: String,
    val text: String?
)

@JsonClass(generateAdapter = true)
data class AnthropicUsage(
    @Json(name = "input_tokens") val inputTokens: Int,
    @Json(name = "output_tokens") val outputTokens: Int
)

// ── Gemini Request ────────────────────────────────────────────────────
@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiConfig(
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int = 4096,
    val temperature: Double = 0.7
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    @Json(name = "usageMetadata") val usageMetadata: GeminiUsage?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent,
    @Json(name = "finishReason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class GeminiUsage(
    @Json(name = "promptTokenCount") val promptTokenCount: Int,
    @Json(name = "candidatesTokenCount") val candidatesTokenCount: Int,
    @Json(name = "totalTokenCount") val totalTokenCount: Int
)
