package com.pocketcode.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Placeholder for other settings
            Text(text = "Theme Settings")
            Text(text = "Editor Settings")

            // Button to navigate to the paywall
            Button(onClick = onNavigateToPaywall) {
                Text("Go Pro")
            }
        }
    }
}
