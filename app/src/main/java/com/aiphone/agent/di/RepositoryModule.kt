package com.aiphone.agent.di
import com.aiphone.agent.data.repository.*; import com.aiphone.agent.domain.repository.*
import dagger.Binds; import dagger.Module; import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent; import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class) abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindChatRepository(impl: ChatRepositoryImpl): IChatRepository
    @Binds @Singleton abstract fun bindFileRepository(impl: FileRepositoryImpl): IFileRepository
    @Binds @Singleton abstract fun bindMacroRepository(impl: MacroRepositoryImpl): IMacroRepository
}