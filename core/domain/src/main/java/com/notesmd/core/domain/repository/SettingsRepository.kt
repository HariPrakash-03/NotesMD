package com.notesmd.core.domain.repository

import com.notesmd.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    fun observeDefaultTemplateId(): Flow<Long?>
    suspend fun setDefaultTemplateId(id: Long)
    fun observeTargetDirectoryUri(): Flow<String?>
    suspend fun saveTargetDirectoryUri(uri: String)
}
