package com.aiphone.agent.data.repository

import com.aiphone.agent.data.local.database.dao.MacroDao
import com.aiphone.agent.data.local.database.entities.MacroEntity
import com.aiphone.agent.domain.model.Macro
import com.aiphone.agent.domain.model.MacroStep
import com.aiphone.agent.domain.repository.IMacroRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MacroRepositoryImpl @Inject constructor(
    private val macroDao: MacroDao,
    private val moshi: Moshi
) : IMacroRepository {

    private val stepsAdapter = moshi.adapter<List<MacroStep>>(
        Types.newParameterizedType(List::class.java, MacroStep::class.java)
    )

    override fun getAllMacros(): Flow<List<Macro>> =
        macroDao.getAllMacros().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getMacro(id: String): Macro? =
        macroDao.getMacro(id)?.toDomain()

    override suspend fun saveMacro(macro: Macro) =
        macroDao.insertMacro(macro.toEntity())

    override suspend fun deleteMacro(id: String) =
        macroDao.deleteMacro(id)

    override suspend fun updateRunStats(id: String) =
        macroDao.updateRunStats(id, System.currentTimeMillis())

    private fun MacroEntity.toDomain() = Macro(
        id = id, name = name, description = description,
        steps = runCatching { stepsAdapter.fromJson(stepsJson) ?: emptyList() }.getOrDefault(emptyList()),
        createdAt = createdAt, lastRunAt = lastRunAt, runCount = runCount
    )

    private fun Macro.toEntity() = MacroEntity(
        id = id, name = name, description = description,
        stepsJson = runCatching { stepsAdapter.toJson(steps) }.getOrDefault("[]"),
        createdAt = createdAt, lastRunAt = lastRunAt, runCount = runCount
    )
}
