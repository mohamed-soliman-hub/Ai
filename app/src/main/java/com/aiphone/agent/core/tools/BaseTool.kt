package com.aiphone.agent.core.tools

abstract class BaseTool {
    abstract val name: String
    abstract val description: String
    abstract val parameters: Map<String, ToolParameter>
    abstract suspend fun execute(params: Map<String, String>): ToolExecutionResult

    data class ToolParameter(val description: String, val required: Boolean = true, val type: String = "string")
    data class ToolExecutionResult(val success: Boolean, val output: String, val error: String? = null, val durationMs: Long = 0)

    protected fun param(params: Map<String, String>, key: String): String =
        params[key] ?: throw IllegalArgumentException("Missing required parameter: $key")
    protected fun paramOrDefault(params: Map<String, String>, key: String, default: String = ""): String =
        params[key] ?: default
}
