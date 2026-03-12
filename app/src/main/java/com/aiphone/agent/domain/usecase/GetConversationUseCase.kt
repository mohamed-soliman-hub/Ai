package com.aiphone.agent.domain.usecase

import com.aiphone.agent.domain.model.Conversation
import com.aiphone.agent.domain.repository.IChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationUseCase @Inject constructor(
    private val repository: IChatRepository
) {
    fun getAllConversations(): Flow<List<Conversation>> =
        repository.getAllConversations()

    fun getConversation(id: String): Flow<Conversation?> =
        repository.getConversation(id)
}
