package com.aiphone.agent.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskPlan(
    val steps: List<PlanStep>,
    val reasoning: String = ""
)

@JsonClass(generateAdapter = true)
data class PlanStep(
    val id: Int,
    val tool: String,
    val params: Map<String, String>,
    val description: String = "",
    val dependsOn: List<Int> = emptyList()
)
