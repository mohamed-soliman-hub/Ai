package com.aiphone.agent.core.tools

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    listFilesTool: ListFilesTool,
    readFileTool: ReadFileTool,
    writeFileTool: WriteFileTool,
    deleteFileTool: DeleteFileTool,
    moveFileTool: MoveFileTool,
    copyFileTool: CopyFileTool,
    createFolderTool: CreateFolderTool,
    searchFilesTool: SearchFilesTool,
    openAppTool: OpenAppTool,
    listAppsTool: ListAppsTool,
    clickOnTextTool: ClickOnTextTool,
    typeTextTool: TypeTextTool,
    getScreenTextTool: GetScreenTextTool,
    scrollTool: ScrollTool,
    pressBackTool: PressBackTool,
    extractTextFromImageTool: ExtractTextFromImageTool
) {
    private val tools: Map<String, BaseTool> = listOf(
        listFilesTool, readFileTool, writeFileTool, deleteFileTool,
        moveFileTool, copyFileTool, createFolderTool, searchFilesTool,
        openAppTool, listAppsTool, clickOnTextTool, typeTextTool,
        getScreenTextTool, scrollTool, pressBackTool, extractTextFromImageTool
    ).associateBy { it.name }

    fun getTool(name: String): BaseTool? = tools[name]
    fun getAllTools(): List<BaseTool> = tools.values.toList()

    fun getToolDescriptionsForPrompt(): String = buildString {
        appendLine("Available tools:")
        tools.values.forEach { tool ->
            appendLine("- ${tool.name}: ${tool.description}")
            tool.parameters.forEach { (key, param) ->
                appendLine("  * $key (${if (param.required) "required" else "optional"}): ${param.description}")
            }
        }
    }
}
