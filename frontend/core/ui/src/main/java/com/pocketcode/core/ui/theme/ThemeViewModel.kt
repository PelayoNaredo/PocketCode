package com.pocketcode.core.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.core.ui.providers.ThemeConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsable de sincronizar el modo de tema de toda la app.
 * Mantiene un flujo observable para modo claro/oscuro y expone utilidades
 * para integrarse con SharedStateProvider.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            themePreferencesRepository.themeMode.collect { mode ->
                updateThemeState(mode)
            }
        }
    }

    fun setDarkTheme(isDark: Boolean, persist: Boolean = true) {
        val mode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
        setThemeMode(mode, persist, isDarkOverride = isDark)
    }

    fun setThemeMode(
        mode: ThemeMode,
        persist: Boolean = true,
        isDarkOverride: Boolean? = null
    ) {
        updateThemeState(mode, isDarkOverride)
        if (persist) {
            persistThemeMode(mode)
        }
    }

    fun updateSystemDarkTheme(isDark: Boolean) {
        if (_themeMode.value == ThemeMode.SYSTEM) {
            _isDarkTheme.value = isDark
        }
    }

    fun syncWithSharedState(onThemeModeChange: (ThemeMode) -> Unit) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            themeMode.collect { mode ->
                onThemeModeChange(mode)
            }
        }
    }

    fun updateFromSharedConfig(configuration: ThemeConfiguration) {
        updateThemeState(configuration.mode, configuration.isDarkTheme)
    }

    override fun onCleared() {
        super.onCleared()
        syncJob?.cancel()
    }

    private fun updateThemeState(mode: ThemeMode, isDarkOverride: Boolean? = null) {
        _themeMode.value = mode
        when (mode) {
            ThemeMode.LIGHT -> _isDarkTheme.value = false
            ThemeMode.DARK -> _isDarkTheme.value = true
            ThemeMode.SYSTEM -> {
                if (isDarkOverride != null) {
                    _isDarkTheme.value = isDarkOverride
                }
            }
        }
    }

    private fun persistThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferencesRepository.setThemeMode(mode)
        }
    }
}
