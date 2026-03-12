package com.aiphone.agent.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class ToolResultStatus { SUCCESS, FAILURE, SKIPPED }

@Parcelize
data class ToolResult(
    val stepId: Int,
    val toolName: String,
    val status: ToolResultStatus,
    val output: String,
    val errorMessage: String? = null,
    val durationMs: Long = 0
) : Parcelable
