package com.aiphone.agent.domain.repository

import com.aiphone.agent.domain.model.Macro
import kotlinx.coroutines.flow.Flow

interface IMacroRepository {
    fun getAllMacros(): Flow<List<Macro>>
    suspend fun getMacro(id: String): Macro?
    suspend fun saveMacro(macro: Macro)
    suspend fun deleteMacro(id: String)
    suspend fun updateRunStats(id: String)
}
