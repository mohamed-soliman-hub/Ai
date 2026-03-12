package com.aiphone.agent.core.tools

import com.aiphone.agent.domain.repository.IFileRepository
import javax.inject.Inject

class ListFilesTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "list_files"
    override val description = "List files/folders in a directory. Use pattern like *.pdf to filter."
    override val parameters = mapOf(
        "directory" to ToolParameter("Absolute path of directory to list"),
        "pattern" to ToolParameter("File pattern e.g. *.jpg or * for all", required = false)
    )
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.listFiles(param(params, "directory"), paramOrDefault(params, "pattern", "*")).fold(
            onSuccess = { files ->
                val output = if (files.isEmpty()) "No files found" else
                    files.joinToString("\n") { "${if (it.isDirectory) "D" else "F"} ${it.name} (${if (it.isDirectory) "dir" else "${it.size}B"})" }
                ToolExecutionResult(true, output, durationMs = System.currentTimeMillis() - start)
            },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}

class ReadFileTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "read_file"
    override val description = "Read the text content of a file"
    override val parameters = mapOf("path" to ToolParameter("Absolute path of the file to read"))
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.readFile(param(params, "path")).fold(
            onSuccess = { ToolExecutionResult(true, it.take(4000), durationMs = System.currentTimeMillis() - start) },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}

class WriteFileTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "write_file"
    override val description = "Write or overwrite text content to a file"
    override val parameters = mapOf(
        "path" to ToolParameter("Absolute path of the file"),
        "content" to ToolParameter("Text content to write")
    )
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.writeFile(param(params, "path"), param(params, "content")).fold(
            onSuccess = { ToolExecutionResult(true, "File written successfully", durationMs = System.currentTimeMillis() - start) },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}

class DeleteFileTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "delete_file"
    override val description = "Delete a file or folder"
    override val parameters = mapOf("path" to ToolParameter("Absolute path to delete"))
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.deleteFile(param(params, "path")).fold(
            onSuccess = { ToolExecutionResult(true, "Deleted successfully", durationMs = System.currentTimeMillis() - start) },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}

class MoveFileTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "move_file"
    override val description = "Move a file or folder to a new location"
    override val parameters = mapOf(
        "source" to ToolParameter("Absolute path of source"),
        "destination" to ToolParameter("Absolute path of destination")
    )
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.moveFile(param(params, "source"), param(params, "destination")).fold(
            onSuccess = { ToolExecutionResult(true, "Moved successfully", durationMs = System.currentTimeMillis() - start) },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}

class CopyFileTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "copy_file"
    override val description = "Copy a file or folder to a new location"
    override val parameters = mapOf(
        "source" to ToolParameter("Absolute path of source"),
        "destination" to ToolParameter("Absolute path of destination")
    )
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.copyFile(param(params, "source"), param(params, "destination")).fold(
            onSuccess = { ToolExecutionResult(true, "Copied successfully", durationMs = System.currentTimeMillis() - start) },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}

class CreateFolderTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "create_folder"
    override val description = "Create a new folder including all parent folders"
    override val parameters = mapOf("path" to ToolParameter("Absolute path of folder to create"))
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.createFolder(param(params, "path")).fold(
            onSuccess = { ToolExecutionResult(true, "Folder created", durationMs = System.currentTimeMillis() - start) },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}

class SearchFilesTool @Inject constructor(private val fileRepository: IFileRepository) : BaseTool() {
    override val name = "search_files"
    override val description = "Search for files by name within a directory tree"
    override val parameters = mapOf(
        "directory" to ToolParameter("Root directory to search in"),
        "query" to ToolParameter("Search term to match file names")
    )
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return fileRepository.searchFiles(param(params, "directory"), param(params, "query")).fold(
            onSuccess = { files ->
                val output = if (files.isEmpty()) "No files found" else
                    "Found ${files.size} file(s):\n" + files.take(50).joinToString("\n") { "  ${it.path}" }
                ToolExecutionResult(true, output, durationMs = System.currentTimeMillis() - start)
            },
            onFailure = { ToolExecutionResult(false, "", it.message, System.currentTimeMillis() - start) }
        )
    }
}
