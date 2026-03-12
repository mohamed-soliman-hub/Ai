package com.aiphone.agent.data.repository

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import com.aiphone.agent.data.local.preferences.SecurePreferences
import com.aiphone.agent.domain.repository.FileInfo
import com.aiphone.agent.domain.repository.IFileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences
) : IFileRepository {

    private fun resolveAndValidatePath(path: String): Result<File> {
        val file = File(path)
        // Sandbox enforcement
        if (securePreferences.isSandboxEnabled()) {
            val sandboxFolder = securePreferences.getSandboxFolder()
            if (sandboxFolder.isNotBlank() && !file.canonicalPath.startsWith(File(sandboxFolder).canonicalPath)) {
                return Result.failure(SecurityException("Access denied: path is outside sandbox folder"))
            }
        }
        return Result.success(file)
    }

    override suspend fun listFiles(directory: String, pattern: String): Result<List<FileInfo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = resolveAndValidatePath(directory).getOrThrow()
                require(dir.exists() && dir.isDirectory) { "Directory does not exist: $directory" }
                val regex = pattern.replace("*", ".*").replace("?", ".").toRegex(RegexOption.IGNORE_CASE)
                dir.listFiles()
                    ?.filter { if (pattern == "*") true else regex.matches(it.name) }
                    ?.map { it.toFileInfo() }
                    ?: emptyList()
            }
        }

    override suspend fun readFile(path: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = resolveAndValidatePath(path).getOrThrow()
                require(file.exists() && file.isFile) { "File does not exist: $path" }
                file.readText(Charsets.UTF_8)
            }
        }

    override suspend fun writeFile(path: String, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = resolveAndValidatePath(path).getOrThrow()
                file.parentFile?.mkdirs()
                file.writeText(content, Charsets.UTF_8)
            }
        }

    override suspend fun deleteFile(path: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = resolveAndValidatePath(path).getOrThrow()
                require(file.exists()) { "File does not exist: $path" }
                if (file.isDirectory) file.deleteRecursively() else file.delete()
                Unit
            }
        }

    override suspend fun moveFile(source: String, destination: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val src = resolveAndValidatePath(source).getOrThrow()
                val dst = resolveAndValidatePath(destination).getOrThrow()
                require(src.exists()) { "Source does not exist: $source" }
                dst.parentFile?.mkdirs()
                src.copyRecursively(dst, overwrite = true)
                src.deleteRecursively()
                Unit
            }
        }

    override suspend fun copyFile(source: String, destination: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val src = resolveAndValidatePath(source).getOrThrow()
                val dst = resolveAndValidatePath(destination).getOrThrow()
                require(src.exists()) { "Source does not exist: $source" }
                dst.parentFile?.mkdirs()
                src.copyRecursively(dst, overwrite = true)
            }
        }

    override suspend fun createFolder(path: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = resolveAndValidatePath(path).getOrThrow()
                require(dir.mkdirs() || dir.exists()) { "Failed to create folder: $path" }
            }
        }

    override suspend fun renameFile(path: String, newName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = resolveAndValidatePath(path).getOrThrow()
                require(file.exists()) { "File does not exist: $path" }
                val newFile = File(file.parent, newName)
                require(file.renameTo(newFile)) { "Failed to rename file" }
            }
        }

    override suspend fun searchFiles(directory: String, query: String): Result<List<FileInfo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = resolveAndValidatePath(directory).getOrThrow()
                require(dir.exists() && dir.isDirectory) { "Directory does not exist: $directory" }
                val results = mutableListOf<FileInfo>()
                dir.walkTopDown().forEach { file ->
                    if (file.name.contains(query, ignoreCase = true)) {
                        results.add(file.toFileInfo())
                    }
                }
                results
            }
        }

    override suspend fun getFileInfo(path: String): Result<FileInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = resolveAndValidatePath(path).getOrThrow()
                require(file.exists()) { "File does not exist: $path" }
                file.toFileInfo()
            }
        }

    private fun File.toFileInfo() = FileInfo(
        name = name,
        path = absolutePath,
        size = if (isDirectory) 0L else length(),
        isDirectory = isDirectory,
        lastModified = lastModified(),
        mimeType = if (!isDirectory) getMimeType(extension) else null
    )

    private fun getMimeType(extension: String): String? =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
}
