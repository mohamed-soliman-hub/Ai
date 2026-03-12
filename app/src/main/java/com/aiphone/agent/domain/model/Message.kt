package com.aiphone.agent.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class MessageRole { USER, ASSISTANT, SYSTEM, TOOL }
enum class MessageStatus { SENDING, SENT, ERROR }

@Parcelize
data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val status: MessageStatus = MessageStatus.SENT,
    val toolResults: List<ToolResult> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val tokensUsed: Int = 0,
    val model: String = ""
) : Parcelable
