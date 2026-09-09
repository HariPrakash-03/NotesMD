package com.notesmd.core.data.di

import com.notesmd.core.data.repository.NoteRepositoryImpl
import com.notesmd.core.data.repository.SettingsRepositoryImpl
import com.notesmd.core.data.repository.TagRepositoryImpl
import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.domain.repository.SettingsRepository
import com.notesmd.core.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
