package com.aiphone.agent.core.macro

import com.aiphone.agent.core.tools.ToolRegistry
import com.aiphone.agent.domain.model.Macro
import com.aiphone.agent.domain.model.MacroStep
import com.aiphone.agent.domain.model.ToolResult
import com.aiphone.agent.domain.model.ToolResultStatus
import com.aiphone.agent.domain.repository.IMacroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class MacroResult {
    data class StepStarted(val step: MacroStep) : MacroResult()
    data class StepCompleted(val step: MacroStep, val result: ToolResult) : MacroResult()
    data class Completed(val macro: Macro, val results: List<ToolResult>) : MacroResult()
    data class Failed(val message: String) : MacroResult()
}

@Singleton
class MacroEngine @Inject constructor(
    private val toolRegistry: ToolRegistry,
    private val macroRepository: IMacroRepository
) {
    private val recordingSteps = mutableListOf<MacroStep>()
    private var isRecording = false

    fun startRecording() { recordingSteps.clear(); isRecording = true }
    fun recordStep(tool: String, params: Map<String, String>) {
        if (isRecording) recordingSteps.add(MacroStep(recordingSteps.size, tool, params))
    }
    fun stopRecording(): List<MacroStep> { isRecording = false; return recordingSteps.toList() }
    fun isCurrentlyRecording() = isRecording

    fun playMacro(macro: Macro): Flow<MacroResult> = flow {
        val results = mutableListOf<ToolResult>()
        for (step in macro.steps.sortedBy { it.order }) {
            emit(MacroResult.StepStarted(step))
            val tool = toolRegistry.getTool(step.tool)
            if (tool == null) { emit(MacroResult.Failed("Unknown tool: ${step.tool}")); return@flow }
            val execResult = tool.execute(step.params)
            val toolResult = ToolResult(step.order, step.tool,
                if (execResult.success) ToolResultStatus.SUCCESS else ToolResultStatus.FAILURE,
                execResult.output, execResult.error, execResult.durationMs)
            results.add(toolResult)
            emit(MacroResult.StepCompleted(step, toolResult))
        }
        macroRepository.updateRunStats(macro.id)
        emit(MacroResult.Completed(macro, results))
    }
}
