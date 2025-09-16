package com.pocketcode.features.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BYOKScreen() {
    var apiKey by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bring Your Own Key", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your third-party AI provider API key below. Your key will be stored securely and used for all AI-powered features.", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // TODO: Wire this up to the ViewModel/UseCase
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Key")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BYOKScreenPreview() {
    BYOKScreen()
}
