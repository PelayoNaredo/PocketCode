package com.pocketcode.designer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Represents a component on the canvas
data class CanvasComponent(
    val id: Int,
    val type: ComponentType,
    val text: String = ""
)

// Enum for the different types of components a user can add
enum class ComponentType {
    TEXT,
    BUTTON
}

@Composable
fun DesignerScreen() {
    var components by remember { mutableStateOf(listOf<CanvasComponent>()) }
    var selectedComponentId by remember { mutableStateOf<Int?>(null) }
    var nextId by remember { mutableStateOf(0) }

    val selectedComponent = components.find { it.id == selectedComponentId }

    Row(modifier = Modifier.fillMaxSize()) {
        // Palette of available components
        Palette(
            modifier = Modifier.weight(1f),
            onAddComponent = { type ->
                val newComponent = CanvasComponent(id = nextId++, type = type, text = "New ${type.name.toLowerCase().capitalize()}")
                components = components + newComponent
            }
        )

        // The canvas where components are rendered
        Canvas(
            modifier = Modifier.weight(2f),
            components = components,
            selectedComponentId = selectedComponentId,
            onSelectComponent = { id -> selectedComponentId = id }
        )

        // The properties panel
        PropertiesPanel(
            modifier = Modifier.weight(1.5f),
            selectedComponent = selectedComponent,
            onUpdateComponent = { updatedComponent ->
                components = components.map { if (it.id == updatedComponent.id) updatedComponent else it }
            }
        )
    }
}

@Composable
fun Palette(
    modifier: Modifier = Modifier,
    onAddComponent: (ComponentType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Text("Components", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onAddComponent(ComponentType.TEXT) }) {
            Text("Add Text")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onAddComponent(ComponentType.BUTTON) }) {
            Text("Add Button")
        }
    }
}

@Composable
fun Canvas(
    modifier: Modifier = Modifier,
    components: List<CanvasComponent>,
    selectedComponentId: Int?,
    onSelectComponent: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
            .border(1.dp, Color.Gray)
            .padding(8.dp)
    ) {
        if (components.isEmpty()) {
            Text("Canvas is empty. Add components from the palette.")
        } else {
            components.forEach { component ->
                RenderComponent(
                    component = component,
                    isSelected = component.id == selectedComponentId,
                    onClick = { onSelectComponent(component.id) }
                )
            }
        }
    }
}

@Composable
fun RenderComponent(
    component: CanvasComponent,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
    ) {
        when (component.type) {
            ComponentType.TEXT -> Text(component.text)
            ComponentType.BUTTON -> Button(onClick = {}) { Text(component.text) }
        }
    }
}

@Composable
fun PropertiesPanel(
    modifier: Modifier = Modifier,
    selectedComponent: CanvasComponent?,
    onUpdateComponent: (CanvasComponent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Text("Properties", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedComponent != null) {
            OutlinedTextField(
                value = selectedComponent.text,
                onValueChange = { newText ->
                    onUpdateComponent(selectedComponent.copy(text = newText))
                },
                label = { Text("Text") }
            )
        } else {
            Text("No component selected.")
        }
    }
}
