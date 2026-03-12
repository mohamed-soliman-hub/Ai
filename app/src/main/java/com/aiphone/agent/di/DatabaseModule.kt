package com.aiphone.agent.di
import android.content.Context
import androidx.room.Room
import com.aiphone.agent.data.local.database.AppDatabase
import com.aiphone.agent.data.local.database.dao.*
import dagger.Module; import dagger.Provides; import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent; import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class) object DatabaseModule {
    @Provides @Singleton fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.DATABASE_NAME).fallbackToDestructiveMigration().build()
    @Provides fun provideConversationDao(db: AppDatabase) = db.conversationDao()
    @Provides fun provideMessageDao(db: AppDatabase) = db.messageDao()
    @Provides fun provideMacroDao(db: AppDatabase) = db.macroDao()
}