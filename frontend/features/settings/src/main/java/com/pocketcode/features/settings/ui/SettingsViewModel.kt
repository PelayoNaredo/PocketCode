package com.pocketcode.features.settings.ui

import androidx.lifecycle.ViewModel
import com.google.jetpackcamera.core.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    fun setTheme(theme: ThemeMode) {
        _uiState.value = _uiState.value.copy(theme = theme)
    }

    fun setFontSize(fontSize: Int) {
        _uiState.value = _uiState.value.copy(fontSize = fontSize)
    }
}

data class SettingsState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val fontSize: Int = 14
)
