package com.aiphone.agent.domain.model

enum class ProviderType(
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String,
    val availableModels: List<String>
) {
    OPENAI(
        displayName = "OpenAI",
        baseUrl = "https://api.openai.com/v1/",
        defaultModel = "gpt-4o",
        availableModels = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo")
    ),
    ANTHROPIC(
        displayName = "Anthropic",
        baseUrl = "https://api.anthropic.com/v1/",
        defaultModel = "claude-sonnet-4-5",
        availableModels = listOf("claude-opus-4-5", "claude-sonnet-4-5", "claude-haiku-4-5-20251001")
    ),
    GEMINI(
        displayName = "Google Gemini",
        baseUrl = "https://generativelanguage.googleapis.com/v1beta/",
        defaultModel = "gemini-1.5-pro",
        availableModels = listOf("gemini-1.5-pro", "gemini-1.5-flash", "gemini-2.0-flash")
    ),
    OPENROUTER(
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1/",
        defaultModel = "openai/gpt-4o",
        availableModels = listOf(
            "openai/gpt-4o",
            "anthropic/claude-sonnet-4-5",
            "google/gemini-1.5-pro",
            "meta-llama/llama-3.1-70b-instruct",
            "mistralai/mistral-large"
        )
    )
}

data class AIProviderConfig(
    val type: ProviderType,
    val apiKey: String,
    val selectedModel: String = type.defaultModel
)
