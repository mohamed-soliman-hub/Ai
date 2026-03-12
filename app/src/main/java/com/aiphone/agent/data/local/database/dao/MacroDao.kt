package com.aiphone.agent.data.local.database.dao

import androidx.room.*
import com.aiphone.agent.data.local.database.entities.MacroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY createdAt DESC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getMacro(id: String): MacroEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity)

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacro(id: String)

    @Query("UPDATE macros SET lastRunAt = :lastRunAt, runCount = runCount + 1 WHERE id = :id")
    suspend fun updateRunStats(id: String, lastRunAt: Long)
}
