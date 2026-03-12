package com.aiphone.agent.domain.usecase

import com.aiphone.agent.core.orchestration.Router
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.MessageRole
import com.aiphone.agent.domain.model.MessageStatus
import com.aiphone.agent.domain.model.ToolResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

sealed class CommandResult {
    data class Thinking(val step: String) : CommandResult()
    data class ToolExecution(val toolName: String, val params: Map<String, String>) : CommandResult()
    data class ToolCompleted(val result: ToolResult) : CommandResult()
    data class Complete(val message: Message) : CommandResult()
    data class Error(val message: String, val cause: Throwable? = null) : CommandResult()
}

class ExecuteCommandUseCase @Inject constructor(
    private val router: Router
) {
    operator fun invoke(
        userInput: String,
        conversationHistory: List<Message>,
        conversationId: String
    ): Flow<CommandResult> = flow {
        try {
            emit(CommandResult.Thinking("Analyzing your request…"))

            router.route(
                input = userInput,
                history = conversationHistory
            ).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(CommandResult.Error("Failed to execute command: ${e.message}", e))
        }
    }
}
