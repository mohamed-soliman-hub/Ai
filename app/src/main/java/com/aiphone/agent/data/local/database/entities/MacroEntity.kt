package com.aiphone.agent.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val stepsJson: String,
    val createdAt: Long,
    val lastRunAt: Long?,
    val runCount: Int = 0
)
