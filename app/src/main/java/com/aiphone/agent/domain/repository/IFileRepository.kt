package com.aiphone.agent.domain.repository

import java.io.File

interface IFileRepository {
    suspend fun listFiles(directory: String, pattern: String = "*"): Result<List<FileInfo>>
    suspend fun readFile(path: String): Result<String>
    suspend fun writeFile(path: String, content: String): Result<Unit>
    suspend fun deleteFile(path: String): Result<Unit>
    suspend fun moveFile(source: String, destination: String): Result<Unit>
    suspend fun copyFile(source: String, destination: String): Result<Unit>
    suspend fun createFolder(path: String): Result<Unit>
    suspend fun renameFile(path: String, newName: String): Result<Unit>
    suspend fun searchFiles(directory: String, query: String): Result<List<FileInfo>>
    suspend fun getFileInfo(path: String): Result<FileInfo>
}

data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long,
    val mimeType: String?
)
