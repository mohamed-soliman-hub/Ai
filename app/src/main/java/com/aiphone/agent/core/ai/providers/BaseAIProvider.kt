package com.aiphone.agent.core.ai.providers

import com.aiphone.agent.domain.model.Message

data class AIResponse(val content: String, val tokensUsed: Int, val model: String)

abstract class BaseAIProvider {
    abstract val providerName: String
    abstract suspend fun chat(messages: List<Message>, model: String, systemPrompt: String): Result<AIResponse>
}
