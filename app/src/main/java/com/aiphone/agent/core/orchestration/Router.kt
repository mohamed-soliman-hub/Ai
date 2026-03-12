package com.aiphone.agent.core.orchestration

import com.aiphone.agent.core.ai.AIProviderManager
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.MessageRole
import com.aiphone.agent.domain.model.ToolResultStatus
import com.aiphone.agent.domain.usecase.CommandResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Router @Inject constructor(
    private val planner: Planner,
    private val executor: Executor,
    private val aiProviderManager: AIProviderManager
) {
    fun route(input: String, history: List<Message>): Flow<CommandResult> = flow {
        if (!aiProviderManager.hasValidApiKey()) {
            emit(CommandResult.Error("No API key configured. Please go to Settings and add your API key."))
            return@flow
        }

        emit(CommandResult.Thinking("Creating execution plan..."))

        val planResult = planner.createPlan(input, history)
        if (planResult.isFailure) {
            Timber.e(planResult.exceptionOrNull(), "Planning failed")
            emit(CommandResult.Thinking("Falling back to conversational response..."))
            val fallbackResult = getConversationalResponse(input, history)
            emit(fallbackResult)
            return@flow
        }

        val plan = planResult.getOrThrow()
        Timber.d("Plan created with ${plan.steps.size} steps: ${plan.reasoning}")

        if (plan.steps.isEmpty()) {
            val response = getConversationalResponse(input, history)
            emit(response)
            return@flow
        }

        emit(CommandResult.Thinking("Executing ${plan.steps.size} step(s)..."))
        val toolResults = executor.execute(plan, this)
        val summary = buildSummary(input, toolResults, plan.reasoning)
        val finalResponse = getAISummary(input, summary, history)
        emit(finalResponse)
    }

    private fun buildSummary(input: String, results: List<com.aiphone.agent.domain.model.ToolResult>, reasoning: String): String {
        val sb = StringBuilder()
        sb.appendLine("Task: $input")
        sb.appendLine("Plan reasoning: $reasoning")
        sb.appendLine("Results:")
        results.forEach { r ->
            val status = if (r.status == ToolResultStatus.SUCCESS) "SUCCESS" else "FAILED"
            sb.appendLine("[$status] ${r.toolName}: ${r.output.take(200).ifBlank { r.errorMessage ?: "completed" }}")
        }
        return sb.toString()
    }

    private suspend fun getConversationalResponse(input: String, history: List<Message>): CommandResult {
        val systemPrompt = "You are an AI assistant for Android device control. " +
            "Answer helpfully. If the user needs file management, app control, or image analysis, " +
            "explain that you can help and ask them to be more specific."

        return try {
            val messages = history.takeLast(10) + Message(
                id = "conv_${System.currentTimeMillis()}",
                conversationId = "temp",
                role = MessageRole.USER,
                content = input
            )
            val response = aiProviderManager.chat(messages, systemPrompt).getOrThrow()
            val msg = Message(
                id = "resp_${System.currentTimeMillis()}",
                conversationId = "temp",
                role = MessageRole.ASSISTANT,
                content = response.content,
                tokensUsed = response.tokensUsed,
                model = response.model
            )
            CommandResult.Complete(msg)
        } catch (e: Exception) {
            CommandResult.Error("AI response failed: ${e.message}", e)
        }
    }

    private suspend fun getAISummary(
        originalInput: String,
        summary: String,
        history: List<Message>
    ): CommandResult {
        val systemPrompt = "You are an AI assistant summarizing executed phone tasks. Be brief and friendly."
        return try {
            val messages = listOf(
                Message(
                    id = "summary_req",
                    conversationId = "summary",
                    role = MessageRole.USER,
                    content = "Summarize this task result in 2 sentences:\n$summary"
                )
            )
            val response = aiProviderManager.chat(messages, systemPrompt).getOrThrow()
            val msg = Message(
                id = "resp_${System.currentTimeMillis()}",
                conversationId = "temp",
                role = MessageRole.ASSISTANT,
                content = response.content,
                tokensUsed = response.tokensUsed,
                model = response.model
            )
            CommandResult.Complete(msg)
        } catch (e: Exception) {
            val fallbackMsg = Message(
                id = "resp_${System.currentTimeMillis()}",
                conversationId = "temp",
                role = MessageRole.ASSISTANT,
                content = "Task completed.\n\n$summary"
            )
            CommandResult.Complete(fallbackMsg)
        }
    }
}
