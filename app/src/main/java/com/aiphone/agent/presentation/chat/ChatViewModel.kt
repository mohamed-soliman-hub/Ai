package com.aiphone.agent.presentation.chat
import androidx.lifecycle.*; import com.aiphone.agent.domain.model.*
import com.aiphone.agent.domain.repository.IChatRepository
import com.aiphone.agent.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch
import java.util.UUID; import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isProcessing: Boolean = false,
    val processingStep: String = "",
    val currentTools: List<String> = emptyList(),
    val error: String? = null,
    val conversationId: String = UUID.randomUUID().toString()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val executeCommandUseCase: ExecuteCommandUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val chatRepository: IChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private val convId = savedStateHandle.get<String>("conversationId")?.takeIf { it != "new" } ?: UUID.randomUUID().toString()

    init {
        _uiState.update { it.copy(conversationId = convId) }
        viewModelScope.launch {
            getConversationUseCase.getConversation(convId).collect { conv ->
                conv?.let { _uiState.update { s -> s.copy(messages = conv.messages) } }
            }
        }
    }

    fun sendMessage(input: String) {
        if (input.isBlank() || _uiState.value.isProcessing) return
        viewModelScope.launch {
            val userMsg = Message(UUID.randomUUID().toString(), convId, MessageRole.USER, input.trim(), MessageStatus.SENT)
            _uiState.update { it.copy(messages = it.messages + userMsg, isProcessing = true, processingStep = "Thinking...", error = null) }
            chatRepository.saveConversation(Conversation(convId, input.take(50)))
            chatRepository.saveMessage(userMsg)
            val toolResults = mutableListOf<ToolResult>()
            executeCommandUseCase(input.trim(), _uiState.value.messages.dropLast(1), convId).collect { result ->
                when (result) {
                    is CommandResult.Thinking     -> _uiState.update { it.copy(processingStep = result.step) }
                    is CommandResult.ToolExecution -> _uiState.update { it.copy(processingStep = "Running: ${result.toolName}", currentTools = it.currentTools + result.toolName) }
                    is CommandResult.ToolCompleted -> toolResults.add(result.result)
                    is CommandResult.Complete -> {
                        val finalMsg = result.message.copy(id = UUID.randomUUID().toString(), conversationId = convId, toolResults = toolResults.toList())
                        chatRepository.saveMessage(finalMsg)
                        _uiState.update { it.copy(messages = it.messages + finalMsg, isProcessing = false, processingStep = "", currentTools = emptyList()) }
                    }
                    is CommandResult.Error -> _uiState.update { it.copy(isProcessing = false, processingStep = "", error = result.message, currentTools = emptyList()) }
                }
            }
        }
    }
    fun clearError() = _uiState.update { it.copy(error = null) }
}