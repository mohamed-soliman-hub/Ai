package com.aiphone.agent.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Macro(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<MacroStep>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastRunAt: Long? = null,
    val runCount: Int = 0
) : Parcelable

@Parcelize
data class MacroStep(
    val order: Int,
    val tool: String,
    val params: Map<String, String>
) : Parcelable
