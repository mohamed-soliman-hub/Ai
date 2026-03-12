package com.aiphone.agent.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiphone.agent.data.local.database.dao.ConversationDao
import com.aiphone.agent.data.local.database.dao.MacroDao
import com.aiphone.agent.data.local.database.dao.MessageDao
import com.aiphone.agent.data.local.database.entities.ConversationEntity
import com.aiphone.agent.data.local.database.entities.MacroEntity
import com.aiphone.agent.data.local.database.entities.MessageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MacroEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun macroDao(): MacroDao

    companion object {
        const val DATABASE_NAME = "aiphone_agent.db"
    }
}
