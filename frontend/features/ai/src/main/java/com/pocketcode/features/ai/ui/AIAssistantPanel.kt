package com.pocketcode.features.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AIAssistantPanel() {
    var prompt by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("AI Response will appear here.") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("AI Assistant", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Enter your prompt") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                // TODO: Wire this up to the ViewModel/UseCase
                result = "Generating response for: '$prompt'..."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Code")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(result)
    }
}

@Preview(showBackground = true)
@Composable
fun AIAssistantPanelPreview() {
    AIAssistantPanel()
}
