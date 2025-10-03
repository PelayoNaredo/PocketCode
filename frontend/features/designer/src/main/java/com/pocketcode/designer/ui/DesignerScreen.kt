package com.pocketcode.designer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.layout.ResponsiveLayout
import com.pocketcode.core.ui.components.layout.SectionLayout
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.TopBarAction
import com.pocketcode.core.ui.components.input.PocketTextField
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.snackbar.GlobalSnackbarEvent
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.core.ui.snackbar.LocalGlobalSnackbarDispatcher
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonSize
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

data class CanvasComponent(
    val id: Int,
    val type: ComponentType,
    val text: String
)

enum class ComponentType {
    TEXT,
    BUTTON
}

private val CanvasComponentListSaver = listSaver<List<CanvasComponent>, Any>(
    save = { list ->
        list.flatMap { component ->
            listOf(component.id, component.type.name, component.text)
        }
    },
    restore = { saved ->
        saved.chunked(3).map { chunk ->
            CanvasComponent(
                id = chunk[0] as Int,
                type = ComponentType.valueOf(chunk[1] as String),
                text = chunk[2] as String
            )
        }
    }
)

@Composable
fun DesignerScreen(
    onNavigateBack: () -> Unit = {},
    onOpenPreview: () -> Unit = {}
) {
    val snackbarDispatcher = LocalGlobalSnackbarDispatcher.current

    var components by rememberSaveable(stateSaver = CanvasComponentListSaver) {
        mutableStateOf(emptyList())
    }
    var selectedComponentId by rememberSaveable { mutableStateOf<Int?>(null) }
    var nextId by rememberSaveable { mutableStateOf(0) }

    val selectedComponent = components.firstOrNull { it.id == selectedComponentId }

    val addComponent: (ComponentType) -> Unit = { type ->
        val newComponent = CanvasComponent(
            id = nextId,
            type = type,
            text = type.defaultLabel(nextId)
        )
        nextId += 1
        components = components + newComponent
        selectedComponentId = newComponent.id
        snackbarDispatcher.dispatch(
            GlobalSnackbarEvent(
                message = "${type.displayName} añadido al lienzo",
                origin = GlobalSnackbarOrigin.DESIGNER,
                severity = GlobalSnackbarSeverity.SUCCESS,
                analyticsId = "designer_component_added"
            )
        )
    }

    val updateComponent: (CanvasComponent) -> Unit = { updated ->
        components = components.map { if (it.id == updated.id) updated else it }
    }

    val removeSelected: () -> Unit = {
        val target = selectedComponent
        if (target != null) {
            val updatedList = components.filterNot { it.id == target.id }
            components = updatedList
            selectedComponentId = updatedList.lastOrNull()?.id
            snackbarDispatcher.dispatch(
                GlobalSnackbarEvent(
                    message = "${target.type.displayName} eliminado del lienzo",
                    origin = GlobalSnackbarOrigin.DESIGNER,
                    severity = GlobalSnackbarSeverity.WARNING,
                    analyticsId = "designer_component_removed"
                )
            )
        }
    }

    val clearCanvas: () -> Unit = {
        if (components.isEmpty()) {
            snackbarDispatcher.dispatch(
                GlobalSnackbarEvent(
                    message = "No hay componentes que limpiar",
                    origin = GlobalSnackbarOrigin.DESIGNER,
                    severity = GlobalSnackbarSeverity.INFO,
                    analyticsId = "designer_canvas_clean_noop"
                )
            )
        } else {
            components = emptyList()
            selectedComponentId = null
            snackbarDispatcher.dispatch(
                GlobalSnackbarEvent(
                    message = "Lienzo limpio",
                    origin = GlobalSnackbarOrigin.DESIGNER,
                    severity = GlobalSnackbarSeverity.INFO,
                    analyticsId = "designer_canvas_cleared"
                )
            )
        }
    }

    val handlePreview: () -> Unit = {
        if (components.isEmpty()) {
            snackbarDispatcher.dispatch(
                GlobalSnackbarEvent(
                    message = "Agrega un componente antes de abrir la vista previa",
                    origin = GlobalSnackbarOrigin.DESIGNER,
                    severity = GlobalSnackbarSeverity.WARNING,
                    analyticsId = "designer_preview_empty"
                )
            )
        } else {
            onOpenPreview()
            snackbarDispatcher.dispatch(
                GlobalSnackbarEvent(
                    message = "Vista previa generada (beta)",
                    origin = GlobalSnackbarOrigin.DESIGNER,
                    severity = GlobalSnackbarSeverity.INFO,
                    analyticsId = "designer_preview_opened"
                )
            )
        }
    }

    PocketScaffold(
        config = PocketScaffoldConfig(
            paddingValues = PaddingValues(
                horizontal = SpacingTokens.Semantic.screenPaddingHorizontal,
                vertical = SpacingTokens.Semantic.screenPaddingVertical
            ),
            isScrollable = false
        ),
        topBar = {
            PocketTopBar(
                title = "Diseñador visual",
                subtitle = "Construye pantallas con componentes Pocket",
                navigationIcon = PocketIcons.ChevronLeft,
                onNavigationClick = onNavigateBack,
                actions = listOf(
                    TopBarAction(
                        icon = PocketIcons.Preview,
                        contentDescription = "Abrir vista previa",
                        onClick = handlePreview
                    )
                )
            )
        }
    ) { paddingValues ->
        DesignerContent(
            paddingValues = paddingValues,
            components = components,
            selectedComponent = selectedComponent,
            onAddComponent = addComponent,
            onSelectComponent = { selectedComponentId = it.id },
            onUpdateComponent = updateComponent,
            onRemoveSelected = removeSelected,
            onClearCanvas = clearCanvas
        )
    }
}

@Composable
private fun DesignerContent(
    paddingValues: PaddingValues,
    components: List<CanvasComponent>,
    selectedComponent: CanvasComponent?,
    onAddComponent: (ComponentType) -> Unit,
    onSelectComponent: (CanvasComponent) -> Unit,
    onUpdateComponent: (CanvasComponent) -> Unit,
    onRemoveSelected: () -> Unit,
    onClearCanvas: () -> Unit
) {
    ResponsiveLayout(
        compactContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingRelaxed)
            ) {
                DesignerPaletteSection(onAddComponent = onAddComponent)
                DesignerCanvasSection(
                    components = components,
                    selectedComponent = selectedComponent,
                    onSelectComponent = onSelectComponent,
                    onClearCanvas = onClearCanvas,
                    enableScroll = false
                )
                DesignerPropertiesSection(
                    selectedComponent = selectedComponent,
                    onUpdateComponent = onUpdateComponent,
                    onRemoveSelected = onRemoveSelected
                )
            }
        },
        mediumContent = {
            DesignerRowLayout(
                paddingValues = paddingValues,
                components = components,
                selectedComponent = selectedComponent,
                onAddComponent = onAddComponent,
                onSelectComponent = onSelectComponent,
                onUpdateComponent = onUpdateComponent,
                onRemoveSelected = onRemoveSelected,
                onClearCanvas = onClearCanvas,
                paletteWeight = 1f,
                canvasWeight = 1.8f,
                propertiesWeight = 1.2f
            )
        },
        expandedContent = {
            DesignerRowLayout(
                paddingValues = paddingValues,
                components = components,
                selectedComponent = selectedComponent,
                onAddComponent = onAddComponent,
                onSelectComponent = onSelectComponent,
                onUpdateComponent = onUpdateComponent,
                onRemoveSelected = onRemoveSelected,
                onClearCanvas = onClearCanvas,
                paletteWeight = 0.9f,
                canvasWeight = 2.2f,
                propertiesWeight = 1.3f
            )
        }
    )
}

@Composable
private fun DesignerRowLayout(
    paddingValues: PaddingValues,
    components: List<CanvasComponent>,
    selectedComponent: CanvasComponent?,
    onAddComponent: (ComponentType) -> Unit,
    onSelectComponent: (CanvasComponent) -> Unit,
    onUpdateComponent: (CanvasComponent) -> Unit,
    onRemoveSelected: () -> Unit,
    onClearCanvas: () -> Unit,
    paletteWeight: Float,
    canvasWeight: Float,
    propertiesWeight: Float
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingRelaxed)
    ) {
        DesignerPaletteSection(
            modifier = Modifier.weight(paletteWeight),
            onAddComponent = onAddComponent
        )
        DesignerCanvasSection(
            modifier = Modifier.weight(canvasWeight),
            components = components,
            selectedComponent = selectedComponent,
            onSelectComponent = onSelectComponent,
            onClearCanvas = onClearCanvas,
            enableScroll = true
        )
        DesignerPropertiesSection(
            modifier = Modifier.weight(propertiesWeight),
            selectedComponent = selectedComponent,
            onUpdateComponent = onUpdateComponent,
            onRemoveSelected = onRemoveSelected
        )
    }
}

@Composable
private fun DesignerPaletteSection(
    modifier: Modifier = Modifier,
    onAddComponent: (ComponentType) -> Unit
) {
    SectionLayout(
        title = "Paleta",
        subtitle = "Agrega componentes Pocket al lienzo",
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            ComponentType.values().forEach { type ->
                PocketButton(
                    text = type.actionLabel,
                    onClick = { onAddComponent(type) },
                    variant = ButtonVariant.Secondary,
                    size = ButtonSize.Medium,
                    leadingIcon = {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DesignerCanvasSection(
    modifier: Modifier = Modifier,
    components: List<CanvasComponent>,
    selectedComponent: CanvasComponent?,
    onSelectComponent: (CanvasComponent) -> Unit,
    onClearCanvas: () -> Unit,
    enableScroll: Boolean
) {
    val scrollState = rememberScrollState()
    SectionLayout(
        title = "Lienzo",
        subtitle = "Toca un componente para editarlo",
        actions = {
            PocketButton(
                text = "Limpiar",
                onClick = onClearCanvas,
                variant = ButtonVariant.Outline,
                size = ButtonSize.Small,
                enabled = components.isNotEmpty(),
                leadingIcon = {
                    Icon(
                        imageVector = PocketIcons.Delete,
                        contentDescription = null
                    )
                }
            )
        },
        modifier = modifier
    ) {
        if (components.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
            ) {
                Icon(
                    imageVector = PocketIcons.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "El lienzo está vacío. Agrega elementos desde la paleta.",
                    style = TypographyTokens.Body.medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val contentModifier = if (enableScroll) {
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            } else {
                Modifier.fillMaxWidth()
            }
            Column(
                modifier = contentModifier,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
            ) {
                components.forEach { component ->
                    DesignerCanvasComponent(
                        component = component,
                        isSelected = selectedComponent?.id == component.id,
                        onClick = { onSelectComponent(component) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DesignerCanvasComponent(
    component: CanvasComponent,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val border = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }
    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (isSelected) CardVariant.Outlined else CardVariant.Elevated,
        border = border,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
            ) {
                Icon(
                    imageVector = component.type.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
                ) {
                    Text(
                        text = component.type.displayName,
                        style = TypographyTokens.Title.small,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = component.text,
                        style = TypographyTokens.Body.medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isSelected) {
                Text(
                    text = "Seleccionado",
                    style = TypographyTokens.Label.small,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DesignerPropertiesSection(
    modifier: Modifier = Modifier,
    selectedComponent: CanvasComponent?,
    onUpdateComponent: (CanvasComponent) -> Unit,
    onRemoveSelected: () -> Unit
) {
    SectionLayout(
        title = "Propiedades",
        subtitle = selectedComponent?.type?.displayName ?: "Selecciona un componente para editarlo",
        actions = {
            PocketButton(
                text = "Eliminar",
                onClick = onRemoveSelected,
                variant = ButtonVariant.Danger,
                size = ButtonSize.Small,
                enabled = selectedComponent != null,
                leadingIcon = {
                    Icon(
                        imageVector = PocketIcons.Delete,
                        contentDescription = null
                    )
                }
            )
        },
        modifier = modifier
    ) {
        if (selectedComponent == null) {
            Text(
                text = "Selecciona un componente en el lienzo para ver sus propiedades.",
                style = TypographyTokens.Body.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
            ) {
                PocketTextField(
                    value = selectedComponent.text,
                    onValueChange = { newValue ->
                        onUpdateComponent(selectedComponent.copy(text = newValue))
                    },
                    label = "Texto principal",
                    helperText = "Se actualizará en la vista previa",
                    maxLines = 3,
                    singleLine = false
                )
                Text(
                    text = "Tipo: ${selectedComponent.type.displayName}",
                    style = TypographyTokens.Body.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val ComponentType.displayName: String
    get() = when (this) {
        ComponentType.TEXT -> "Texto"
        ComponentType.BUTTON -> "Botón"
    }

private val ComponentType.icon: ImageVector
    get() = when (this) {
        ComponentType.TEXT -> PocketIcons.Title
        ComponentType.BUTTON -> PocketIcons.TouchApp
    }

private val ComponentType.actionLabel: String
    get() = when (this) {
        ComponentType.TEXT -> "Añadir texto"
        ComponentType.BUTTON -> "Añadir botón"
    }

private fun ComponentType.defaultLabel(index: Int): String = when (this) {
    ComponentType.TEXT -> "Texto ${index + 1}"
    ComponentType.BUTTON -> "Botón ${index + 1}"
}
