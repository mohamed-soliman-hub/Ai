package com.aiphone.agent.core.orchestration

import com.aiphone.agent.core.ai.AIProviderManager
import com.aiphone.agent.core.tools.ToolRegistry
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.MessageRole
import com.aiphone.agent.domain.model.TaskPlan
import com.squareup.moshi.Moshi
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Planner @Inject constructor(
    private val aiProviderManager: AIProviderManager,
    private val toolRegistry: ToolRegistry,
    private val moshi: Moshi
) {
    private val planAdapter = moshi.adapter(TaskPlan::class.java)

    suspend fun createPlan(
        userInput: String,
        conversationHistory: List<Message>
    ): Result<TaskPlan> = runCatching {
        val systemPrompt = buildSystemPrompt()
        val messages = buildPlanningMessages(userInput, conversationHistory)
        Timber.d("Requesting plan from AI provider...")
        val response = aiProviderManager.chat(messages, systemPrompt).getOrThrow()
        parsePlan(response.content)
    }

    private fun buildSystemPrompt(): String {
        return "You are an AI task planner for an Android phone assistant. " +
            "Analyze requests and create structured JSON execution plans.\n\n" +
            toolRegistry.getToolDescriptionsForPrompt() +
            "\n\nRULES:\n" +
            "1. Respond ONLY with valid JSON - no markdown, no explanation outside JSON\n" +
            "2. Use exact tool names from the list above\n" +
            "3. Use absolute file paths (e.g. /storage/emulated/0/Download/)\n" +
            "4. For conversational questions (no tool needed), return: {\"steps\": []}\n\n" +
            "RESPONSE FORMAT:\n" +
            "{\n" +
            "  \"reasoning\": \"brief plan explanation\",\n" +
            "  \"steps\": [\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"tool\": \"tool_name\",\n" +
            "      \"params\": {\"param1\": \"value1\"},\n" +
            "      \"description\": \"what this step does\",\n" +
            "      \"dependsOn\": []\n" +
            "    }\n" +
            "  ]\n" +
            "}"
    }

    private fun buildPlanningMessages(userInput: String, history: List<Message>): List<Message> {
        val contextMessages = history.takeLast(6)
        val planningMessage = Message(
            id = "planning_request",
            conversationId = "planning",
            role = MessageRole.USER,
            content = "Create a JSON execution plan for: $userInput"
        )
        return contextMessages + planningMessage
    }

    private fun parsePlan(json: String): TaskPlan {
        val cleaned = json
            .trimStart()
            .removePrefix("```json")
            .removePrefix("```")
            .trimEnd()
            .removeSuffix("```")
            .trim()
        return planAdapter.fromJson(cleaned)
            ?: throw IllegalArgumentException("Failed to parse plan JSON")
    }
}
