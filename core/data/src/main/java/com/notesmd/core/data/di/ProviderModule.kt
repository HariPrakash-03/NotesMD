package com.notesmd.core.data.di

import com.notesmd.core.data.provider.NoOpProStatusProvider
import com.notesmd.core.data.provider.NoOpSyncEngine
import com.notesmd.core.data.provider.OfflineAuthSession
import com.notesmd.core.data.provider.SafDocumentStorageProvider
import com.notesmd.core.domain.provider.AuthSessionProvider
import com.notesmd.core.domain.provider.DocumentStorageProvider
import com.notesmd.core.domain.provider.ProStatusProvider
import com.notesmd.core.domain.provider.SyncEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @Singleton
    abstract fun bindAuthSessionProvider(impl: OfflineAuthSession): AuthSessionProvider

    @Binds
    @Singleton
    abstract fun bindDocumentStorageProvider(impl: SafDocumentStorageProvider): DocumentStorageProvider

    @Binds
    @Singleton
    abstract fun bindProStatusProvider(impl: NoOpProStatusProvider): ProStatusProvider

    @Binds
    @Singleton
    abstract fun bindSyncEngine(impl: NoOpSyncEngine): SyncEngine
}
