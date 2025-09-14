package com.pocketcode.features.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.jetpackcamera.core.ui.theme.PocketCodeTheme
import com.google.jetpackcamera.core.ui.theme.ThemeMode
import com.google.jetpackcamera.core.ui.theme.medium_spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    PocketCodeTheme(theme = uiState.theme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(medium_spacing)
            ) {
                ThemeSetting(
                    selectedTheme = uiState.theme,
                    onThemeChange = { viewModel.setTheme(it) }
                )
                FontSizeSetting(
                    fontSize = uiState.fontSize,
                    onFontSizeChange = { viewModel.setFontSize(it) }
                )
            }
        }
    }
}

@Composable
fun ThemeSetting(
    selectedTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    Column {
        Text("Theme", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedTheme == ThemeMode.LIGHT,
                onClick = { onThemeChange(ThemeMode.LIGHT) }
            )
            Text("Light")
            Spacer(modifier = Modifier.width(medium_spacing))
            RadioButton(
                selected = selectedTheme == ThemeMode.DARK,
                onClick = { onThemeChange(ThemeMode.DARK) }
            )
            Text("Dark")
            Spacer(modifier = Modifier.width(medium_spacing))
            RadioButton(
                selected = selectedTheme == ThemeMode.SYSTEM,
                onClick = { onThemeChange(ThemeMode.SYSTEM) }
            )
            Text("System")
        }
    }
}

@Composable
fun FontSizeSetting(
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = medium_spacing)) {
        Text("Font Size", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(fontSize.toString(), modifier = Modifier.width(40.dp))
            Slider(
                value = fontSize.toFloat(),
                onValueChange = { onFontSizeChange(it.toInt()) },
                valueRange = 10f..24f,
                steps = 13
            )
        }
    }
}
