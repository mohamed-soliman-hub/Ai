package com.aiphone.agent.data.repository

import com.aiphone.agent.data.local.database.dao.ConversationDao
import com.aiphone.agent.data.local.database.dao.MessageDao
import com.aiphone.agent.data.local.database.entities.ConversationEntity
import com.aiphone.agent.data.local.database.entities.MessageEntity
import com.aiphone.agent.domain.model.Conversation
import com.aiphone.agent.domain.model.Message
import com.aiphone.agent.domain.model.MessageRole
import com.aiphone.agent.domain.model.MessageStatus
import com.aiphone.agent.domain.model.ToolResult
import com.aiphone.agent.domain.repository.IChatRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val moshi: Moshi
) : IChatRepository {

    private val toolResultListAdapter = moshi.adapter<List<ToolResult>>(
        Types.newParameterizedType(List::class.java, ToolResult::class.java)
    )

    override fun getAllConversations(): Flow<List<Conversation>> =
        conversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getConversation(id: String): Flow<Conversation?> =
        conversationDao.getConversation(id).flatMapLatest { convEntity ->
            messageDao.getMessagesForConversation(id).map { msgEntities ->
                convEntity?.toDomain()?.copy(
                    messages = msgEntities.map { it.toDomain() }
                )
            }
        }

    override suspend fun saveConversation(conversation: Conversation) {
        conversationDao.insertConversation(conversation.toEntity())
    }

    override suspend fun saveMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
        conversationDao.updateConversation(
            id = message.conversationId,
            updatedAt = System.currentTimeMillis(),
            tokens = message.tokensUsed,
            title = "Conversation"
        )
    }

    override suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversation(id)
    }

    override suspend fun clearAll() {
        conversationDao.deleteAll()
    }

    // ── Mappers ────────────────────────────────────────────────────────
    private fun ConversationEntity.toDomain() = Conversation(
        id = id, title = title, createdAt = createdAt,
        updatedAt = updatedAt, totalTokens = totalTokens
    )

    private fun Conversation.toEntity() = ConversationEntity(
        id = id, title = title, createdAt = createdAt,
        updatedAt = updatedAt, totalTokens = totalTokens
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        conversationId = conversationId,
        role = MessageRole.valueOf(role),
        content = content,
        status = MessageStatus.valueOf(status),
        toolResults = runCatching {
            toolResultListAdapter.fromJson(toolResultsJson) ?: emptyList()
        }.getOrDefault(emptyList()),
        timestamp = timestamp,
        tokensUsed = tokensUsed,
        model = model
    )

    private fun Message.toEntity() = MessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.name,
        content = content,
        status = status.name,
        toolResultsJson = runCatching {
            toolResultListAdapter.toJson(toolResults)
        }.getOrDefault("[]"),
        timestamp = timestamp,
        tokensUsed = tokensUsed,
        model = model
    )
}
