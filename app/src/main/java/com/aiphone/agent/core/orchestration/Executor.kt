package com.aiphone.agent.core.orchestration

import com.aiphone.agent.core.cache.CacheManager
import com.aiphone.agent.core.tools.ToolRegistry
import com.aiphone.agent.domain.model.TaskPlan
import com.aiphone.agent.domain.model.ToolResult
import com.aiphone.agent.domain.model.ToolResultStatus
import com.aiphone.agent.domain.usecase.CommandResult
import kotlinx.coroutines.flow.FlowCollector
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Executor @Inject constructor(
    private val toolRegistry: ToolRegistry,
    private val cacheManager: CacheManager
) {
    suspend fun execute(
        plan: TaskPlan,
        collector: FlowCollector<CommandResult>
    ): List<ToolResult> {
        val results = mutableListOf<ToolResult>()
        val stepOutputs = mutableMapOf<Int, String>()

        for (step in plan.steps) {
            collector.emit(CommandResult.ToolExecution(step.tool, step.params))
            val tool = toolRegistry.getTool(step.tool)
            if (tool == null) {
                val failResult = ToolResult(step.id, step.tool, ToolResultStatus.FAILURE, "", "Unknown tool: ${step.tool}")
                results.add(failResult)
                collector.emit(CommandResult.ToolCompleted(failResult))
                continue
            }
            val resolvedParams = step.params.mapValues { (_, v) -> resolveTemplates(v, stepOutputs) }
            val cacheKey = "${step.tool}:${resolvedParams.entries.sortedBy { it.key }.joinToString()}"
            val cached = cacheManager.get(cacheKey)
            val toolResult = if (cached != null) {
                Timber.d("Cache hit for $cacheKey")
                ToolResult(step.id, step.tool, ToolResultStatus.SUCCESS, cached)
            } else {
                val execResult = tool.execute(resolvedParams)
                val tr = ToolResult(
                    stepId = step.id, toolName = step.tool,
                    status = if (execResult.success) ToolResultStatus.SUCCESS else ToolResultStatus.FAILURE,
                    output = execResult.output, errorMessage = execResult.error, durationMs = execResult.durationMs
                )
                if (execResult.success && isCacheable(step.tool)) {
                    cacheManager.put(cacheKey, execResult.output, ttlMs = 30_000L)
                }
                tr
            }
            stepOutputs[step.id] = toolResult.output
            results.add(toolResult)
            collector.emit(CommandResult.ToolCompleted(toolResult))
        }
        return results
    }

    private fun resolveTemplates(value: String, stepOutputs: Map<Int, String>): String {
        var resolved = value
        val regex = Regex("\\{\\{step(\\d+)\\.result\\}\\}")
        regex.findAll(value).forEach { match ->
            val stepId = match.groupValues[1].toIntOrNull() ?: return@forEach
            resolved = resolved.replace(match.value, stepOutputs[stepId] ?: "")
        }
        return resolved
    }

    private fun isCacheable(toolName: String) = toolName in setOf("list_files", "get_file_info", "extract_text_from_image", "list_apps")
}
