package com.pocketcode.core.storage.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized preferences manager using DataStore
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
        private val AUTO_SAVE_KEY = booleanPreferencesKey("auto_save")
        private val SHOW_LINE_NUMBERS_KEY = booleanPreferencesKey("show_line_numbers")
        private val WORD_WRAP_KEY = booleanPreferencesKey("word_wrap")
        private val LAST_OPENED_PROJECT_KEY = stringPreferencesKey("last_opened_project")
    }
    
    // Theme preferences
    val theme: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }
    
    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
    
    // Font size preferences
    val fontSize: Flow<Int> = dataStore.data.map { preferences ->
        preferences[FONT_SIZE_KEY] ?: 14
    }
    
    suspend fun setFontSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = size
        }
    }
    
    // Auto-save preferences
    val autoSave: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_SAVE_KEY] ?: true
    }
    
    suspend fun setAutoSave(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SAVE_KEY] = enabled
        }
    }
    
    // Show line numbers
    val showLineNumbers: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_LINE_NUMBERS_KEY] ?: true
    }
    
    suspend fun setShowLineNumbers(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_LINE_NUMBERS_KEY] = enabled
        }
    }
    
    // Word wrap
    val wordWrap: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[WORD_WRAP_KEY] ?: false
    }
    
    suspend fun setWordWrap(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WORD_WRAP_KEY] = enabled
        }
    }
    
    // Last opened project
    val lastOpenedProject: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LAST_OPENED_PROJECT_KEY]
    }
    
    suspend fun setLastOpenedProject(projectId: String?) {
        dataStore.edit { preferences ->
            if (projectId != null) {
                preferences[LAST_OPENED_PROJECT_KEY] = projectId
            } else {
                preferences.remove(LAST_OPENED_PROJECT_KEY)
            }
        }
    }
}