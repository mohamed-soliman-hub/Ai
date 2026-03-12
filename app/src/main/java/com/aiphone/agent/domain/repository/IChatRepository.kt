package com.aiphone.agent.domain.repository

import com.aiphone.agent.domain.model.Conversation
import com.aiphone.agent.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface IChatRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    fun getConversation(id: String): Flow<Conversation?>
    suspend fun saveConversation(conversation: Conversation)
    suspend fun saveMessage(message: Message)
    suspend fun deleteConversation(id: String)
    suspend fun clearAll()
}
