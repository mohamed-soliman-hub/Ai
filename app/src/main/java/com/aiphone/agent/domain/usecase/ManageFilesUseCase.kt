package com.aiphone.agent.domain.usecase

import com.aiphone.agent.domain.repository.FileInfo
import com.aiphone.agent.domain.repository.IFileRepository
import javax.inject.Inject

class ManageFilesUseCase @Inject constructor(
    private val fileRepository: IFileRepository
) {
    suspend fun listFiles(directory: String, pattern: String = "*") =
        fileRepository.listFiles(directory, pattern)

    suspend fun readFile(path: String) = fileRepository.readFile(path)

    suspend fun writeFile(path: String, content: String) =
        fileRepository.writeFile(path, content)

    suspend fun deleteFile(path: String) = fileRepository.deleteFile(path)

    suspend fun moveFile(source: String, destination: String) =
        fileRepository.moveFile(source, destination)

    suspend fun copyFile(source: String, destination: String) =
        fileRepository.copyFile(source, destination)

    suspend fun createFolder(path: String) = fileRepository.createFolder(path)

    suspend fun searchFiles(directory: String, query: String) =
        fileRepository.searchFiles(directory, query)
}
