package com.notesmd.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.notesmd.core.domain.repository.SettingsRepository
import com.notesmd.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_TEMPLATE_ID = longPreferencesKey("default_template_id")
        val TARGET_DIRECTORY_URI = stringPreferencesKey("target_directory_uri")
    }

    override fun observeThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            prefs[THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }

    override fun observeDefaultTemplateId(): Flow<Long?> =
        dataStore.data.map { prefs -> prefs[DEFAULT_TEMPLATE_ID] }

    override suspend fun setDefaultTemplateId(id: Long) {
        dataStore.edit { prefs -> prefs[DEFAULT_TEMPLATE_ID] = id }
    }

    override fun observeTargetDirectoryUri(): Flow<String?> =
        dataStore.data.map { prefs -> prefs[TARGET_DIRECTORY_URI] }

    override suspend fun saveTargetDirectoryUri(uri: String) {
        dataStore.edit { prefs -> prefs[TARGET_DIRECTORY_URI] = uri }
    }
}
