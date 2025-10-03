package com.pocketcode.features.settings.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.pocketcode.core.ui.components.layout.PocketDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.feedback.PocketDialog
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.layout.SectionLayout
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.TabItem
import com.pocketcode.core.ui.components.selection.PocketFilterChip
import com.pocketcode.core.ui.components.selection.PocketSwitch
import com.pocketcode.core.ui.snackbar.GlobalSnackbarEvent
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.core.ui.snackbar.LocalGlobalSnackbarDispatcher
import com.pocketcode.core.ui.snackbar.LocalGlobalToastDispatcher
import com.pocketcode.core.ui.components.feedback.PocketToastStyle
import com.pocketcode.core.ui.components.feedback.PocketToastDuration
import com.pocketcode.core.ui.theme.ThemeViewModel
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant
import com.pocketcode.features.settings.model.AIProvider
import com.pocketcode.features.settings.model.FontSize
import com.pocketcode.core.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModernSettingsScreen(
    onDismiss: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    val toastDispatcher = LocalGlobalToastDispatcher.current
    val onSettingChanged: (String, GlobalSnackbarSeverity) -> Unit = { message, severity ->
        val toastStyle = when (severity) {
            GlobalSnackbarSeverity.SUCCESS -> PocketToastStyle.Success
            GlobalSnackbarSeverity.ERROR -> PocketToastStyle.Error
            GlobalSnackbarSeverity.WARNING -> PocketToastStyle.Warning
            GlobalSnackbarSeverity.INFO -> PocketToastStyle.Info
        }
        toastDispatcher.showMessage(
            message = message,
            style = toastStyle,
            origin = GlobalSnackbarOrigin.SETTINGS,
            duration = PocketToastDuration.Short
        )
    }

    val tabs = listOf(
        TabItem(id = "general", title = "General", icon = Icons.Default.Settings),
        TabItem(id = "editor", title = "Editor", icon = Icons.Default.Edit),
        TabItem(id = "ai", title = "IA", icon = Icons.AutoMirrored.Filled.Chat),
        TabItem(id = "project", title = "Proyecto", icon = Icons.Default.Folder),
        TabItem(id = "about", title = "Acerca de", icon = Icons.Default.Info)
    )

    PocketScaffold(
        config = PocketScaffoldConfig(
            hasTopBar = true,
            hasTabs = true,
            isScrollable = false,
            paddingValues = PaddingValues(0.dp)
        ),
        topBar = {
            PocketTopBar(
                title = "Configuración",
                navigationIcon = Icons.Default.Close,
                onNavigationClick = onDismiss
            )
        },
        tabs = tabs,
        selectedTabIndex = pagerState.currentPage,
        onTabSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState, 
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> GeneralSettingsTab(settingsViewModel, themeViewModel, onSettingChanged, paddingValues)
                1 -> EditorSettingsTab(settingsViewModel, onSettingChanged, paddingValues)
                2 -> AISettingsTab(settingsViewModel, onSettingChanged, paddingValues)
                3 -> ProjectSettingsTab(settingsViewModel, onSettingChanged, paddingValues)
                4 -> AboutTab(paddingValues)
            }
        }
    }
}

@Composable
private fun GeneralSettingsTab(
    settingsViewModel: SettingsViewModel,
    themeViewModel: ThemeViewModel,
    onSettingChanged: (String, GlobalSnackbarSeverity) -> Unit,
    paddingValues: PaddingValues
) {
    val settingsState by settingsViewModel.uiState.collectAsState()
    val appSettings = settingsState.userSettings.appSettings
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 96.dp
        )
    ) {
        item {
            SectionLayout(title = "Apariencia") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tema",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeFilterChip(
                            selected = appSettings.themeMode == ThemeMode.SYSTEM,
                            icon = Icons.Default.Settings,
                            label = "Sistema"
                        ) {
                            settingsViewModel.updateThemeMode(ThemeMode.SYSTEM)
                            themeViewModel.setThemeMode(
                                mode = ThemeMode.SYSTEM,
                                persist = false,
                                isDarkOverride = systemDark
                            )
                            themeViewModel.updateSystemDarkTheme(systemDark)
                            onSettingChanged("Tema del sistema activado", GlobalSnackbarSeverity.SUCCESS)
                        }
                        ThemeFilterChip(
                            selected = appSettings.themeMode == ThemeMode.LIGHT,
                            icon = Icons.Default.LightMode,
                            label = "Claro"
                        ) {
                            settingsViewModel.updateThemeMode(ThemeMode.LIGHT)
                            themeViewModel.setDarkTheme(isDark = false, persist = false)
                            onSettingChanged("Tema claro activado", GlobalSnackbarSeverity.SUCCESS)
                        }
                        ThemeFilterChip(
                            selected = appSettings.themeMode == ThemeMode.DARK,
                            icon = Icons.Default.DarkMode,
                            label = "Oscuro"
                        ) {
                            settingsViewModel.updateThemeMode(ThemeMode.DARK)
                            themeViewModel.setDarkTheme(isDark = true, persist = false)
                            onSettingChanged("Tema oscuro activado", GlobalSnackbarSeverity.SUCCESS)
                        }
                    }
                }
            }
        }

        item {
            SectionLayout(title = "Comportamiento") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernSettingsSwitch(
                        title = "Guardado automático",
                        subtitle = "Guardar archivos automáticamente al editar",
                        checked = appSettings.autoSave,
                        onCheckedChange = {
                            settingsViewModel.updateAutoSave(it)
                            onSettingChanged(
                                "Guardado automático ${if (it) "activado" else "desactivado"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Animaciones",
                        subtitle = "Habilitar animaciones en la interfaz",
                        checked = appSettings.animationsEnabled,
                        onCheckedChange = {
                            settingsViewModel.updateAnimationsEnabled(it)
                            onSettingChanged(
                                "Animaciones ${if (it) "activadas" else "desactivadas"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Vibración",
                        subtitle = "Feedback háptico en interacciones",
                        checked = appSettings.hapticFeedback,
                        onCheckedChange = {
                            settingsViewModel.updateHapticFeedback(it)
                            onSettingChanged(
                                "Vibración ${if (it) "activada" else "desactivada"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                }
            }
        }

        item {
            SectionLayout(title = "Privacidad") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernSettingsSwitch(
                        title = "Permitir analíticas",
                        subtitle = "Ayuda a mejorar PocketCode con métricas anónimas",
                        checked = appSettings.analyticsEnabled,
                        onCheckedChange = { enabled ->
                            settingsViewModel.updateAnalyticsEnabled(enabled)
                            onSettingChanged(
                                "Analíticas ${if (enabled) "activadas" else "desactivadas"}",
                                if (enabled) GlobalSnackbarSeverity.SUCCESS else GlobalSnackbarSeverity.WARNING
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Reportes de fallos",
                        subtitle = "Comparte informes de errores con el equipo",
                        checked = appSettings.crashReportingEnabled,
                        onCheckedChange = { enabled ->
                            settingsViewModel.updateCrashReportingEnabled(enabled)
                            onSettingChanged(
                                "Reportes de fallos ${if (enabled) "activados" else "desactivados"}",
                                if (enabled) GlobalSnackbarSeverity.SUCCESS else GlobalSnackbarSeverity.WARNING
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeFilterChip(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onSelected: () -> Unit
) {
    PocketFilterChip(
        selected = selected,
        onClick = onSelected,
        label = label,
        leadingIcon = icon
    )
}

@Composable
private fun EditorSettingsTab(
    settingsViewModel: SettingsViewModel,
    onSettingChanged: (String, GlobalSnackbarSeverity) -> Unit,
    paddingValues: PaddingValues
) {
    val settingsState by settingsViewModel.uiState.collectAsState()
    val editorSettings = settingsState.userSettings.editorSettings

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 96.dp
        )
    ) {
        item {
            SectionLayout(title = "Visualización") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernSettingsSwitch(
                        title = "Números de línea",
                        subtitle = "Mostrar numeración en el editor",
                        checked = editorSettings.lineNumbers,
                        onCheckedChange = {
                            settingsViewModel.updateLineNumbers(it)
                            onSettingChanged(
                                "Números de línea ${if (it) "activados" else "desactivados"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Resaltado de sintaxis",
                        subtitle = "Colorear código según lenguaje",
                        checked = editorSettings.syntaxHighlighting,
                        onCheckedChange = {
                            settingsViewModel.updateSyntaxHighlighting(it)
                            onSettingChanged(
                                "Resaltado de sintaxis ${if (it) "activado" else "desactivado"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Ajuste de línea",
                        subtitle = "Ajustar líneas largas automáticamente",
                        checked = editorSettings.wordWrap,
                        onCheckedChange = {
                            settingsViewModel.updateWordWrap(it)
                            onSettingChanged(
                                "Ajuste de línea ${if (it) "activado" else "desactivado"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                }
            }
        }

        item {
            SectionLayout(title = "Formato") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = "Tamaño de fuente: ${editorSettings.fontSize.toDisplayName()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FontSize.values().forEach { size ->
                                PocketFilterChip(
                                    label = size.toDisplayName(),
                                    selected = editorSettings.fontSize == size,
                                    onClick = {
                                        settingsViewModel.updateFontSize(size)
                                        onSettingChanged(
                                            "Tamaño de fuente: ${size.toDisplayName()}",
                                            GlobalSnackbarSeverity.SUCCESS
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "Tamaño de tabulación: ${editorSettings.tabSize} espacios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 4, 8).forEach { size ->
                                PocketFilterChip(
                                    label = "$size",
                                    selected = editorSettings.tabSize == size,
                                    onClick = {
                                        settingsViewModel.updateTabSize(size)
                                        onSettingChanged(
                                            "Tabulación: $size espacios",
                                            GlobalSnackbarSeverity.SUCCESS
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AISettingsTab(
    settingsViewModel: SettingsViewModel,
    onSettingChanged: (String, GlobalSnackbarSeverity) -> Unit,
    paddingValues: PaddingValues
) {
    val settingsState by settingsViewModel.uiState.collectAsState()
    val aiSettings = settingsState.userSettings.aiSettings

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 96.dp
        )
    ) {
        item {
            SectionLayout(title = "Asistente IA") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernSettingsSwitch(
                        title = "Sugerencias automáticas",
                        subtitle = "Mostrar sugerencias mientras escribes",
                        checked = aiSettings.autoSuggestions,
                        onCheckedChange = {
                            settingsViewModel.updateAutoSuggestions(it)
                            onSettingChanged(
                                "Sugerencias automáticas ${if (it) "activadas" else "desactivadas"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Completado de código",
                        subtitle = "Autocompletar código con IA",
                        checked = aiSettings.codeCompletion,
                        onCheckedChange = {
                            settingsViewModel.updateCodeCompletion(it)
                            onSettingChanged(
                                "Completado de código ${if (it) "activado" else "desactivado"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Explicaciones contextuales",
                        subtitle = "Explicar código seleccionado",
                        checked = aiSettings.contextualExplanations,
                        onCheckedChange = {
                            settingsViewModel.updateContextualExplanations(it)
                            onSettingChanged(
                                "Explicaciones contextuales ${if (it) "activadas" else "desactivadas"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                }
            }
        }

        item {
            SectionLayout(title = "Modelo de IA") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Proveedor de IA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AIProvider.values().forEach { provider ->
                            PocketFilterChip(
                                label = provider.toDisplayName(),
                                selected = aiSettings.provider == provider,
                                onClick = {
                                    settingsViewModel.updateAIProvider(provider)
                                    onSettingChanged(
                                        "Proveedor de IA: ${provider.toDisplayName()}",
                                        GlobalSnackbarSeverity.SUCCESS
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSettingsTab(
    settingsViewModel: SettingsViewModel,
    onSettingChanged: (String, GlobalSnackbarSeverity) -> Unit,
    paddingValues: PaddingValues
) {
    val settingsState by settingsViewModel.uiState.collectAsState()
    val projectSettings = settingsState.userSettings.projectSettings

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 96.dp
        )
    ) {
        item {
            SectionLayout(title = "Gestión de proyectos") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernSettingsSwitch(
                        title = "Crear backup automático",
                        subtitle = "Crear copias de seguridad periódicas",
                        checked = projectSettings.autoBackup,
                        onCheckedChange = {
                            settingsViewModel.updateAutoBackup(it)
                            onSettingChanged(
                                "Backup automático ${if (it) "activado" else "desactivado"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Sincronización en la nube",
                        subtitle = "Sincronizar proyectos con la nube",
                        checked = projectSettings.cloudSync,
                        onCheckedChange = {
                            settingsViewModel.updateCloudSync(it)
                            onSettingChanged(
                                "Sincronización en la nube ${if (it) "activada" else "desactivada"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                    ModernSettingsSwitch(
                        title = "Compresión de archivos",
                        subtitle = "Comprimir proyectos para ahorrar espacio",
                        checked = projectSettings.compression,
                        onCheckedChange = {
                            settingsViewModel.updateCompression(it)
                            onSettingChanged(
                                "Compresión de archivos ${if (it) "activada" else "desactivada"}",
                                GlobalSnackbarSeverity.SUCCESS
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutTab(paddingValues: PaddingValues) {
    var showLicensesDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 24.dp,
                bottom = paddingValues.calculateBottomPadding() + 96.dp
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "PocketCode IDE",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Versión 1.0.0",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PocketCard(variant = CardVariant.Elevated) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Créditos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                InfoRow(label = "Equipo", value = "PocketCode Team")
                InfoRow(label = "Contacto", value = "hello@pocketcode.dev")
                PocketDivider()
                PocketButton(
                    text = "Licencias de código abierto",
                    onClick = { showLicensesDialog = true },
                    variant = ButtonVariant.Text,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showLicensesDialog) {
        LicensesDialog(onDismiss = { showLicensesDialog = false })
    }
}

@Composable
private fun LicensesDialog(onDismiss: () -> Unit) {
    PocketDialog(
        title = "Licencias de código abierto",
        message = "Consulta la lista de proyectos que hacen posible PocketCode.",
        onDismissRequest = onDismiss,
        confirmText = "Cerrar",
        onConfirm = onDismiss,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                openSourceLicenses.forEach { license ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = license.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${license.author} · ${license.license}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = license.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ModernSettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    PocketSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = title,
        description = subtitle,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class LicenseInfo(
    val name: String,
    val author: String,
    val url: String,
    val license: String
)

private val openSourceLicenses = listOf(
    LicenseInfo(
        name = "Jetpack Compose",
        author = "Google",
        url = "https://developer.android.com/jetpack/compose",
        license = "Apache License 2.0"
    ),
    LicenseInfo(
        name = "Kotlin",
        author = "JetBrains",
        url = "https://kotlinlang.org/",
        license = "Apache License 2.0"
    ),
    LicenseInfo(
        name = "Dagger Hilt",
        author = "Google",
        url = "https://dagger.dev/hilt/",
        license = "Apache License 2.0"
    )
)

private fun FontSize.toDisplayName(): String = when (this) {
    FontSize.SMALL -> "Pequeña"
    FontSize.MEDIUM -> "Mediana"
    FontSize.LARGE -> "Grande"
    FontSize.EXTRA_LARGE -> "Extra grande"
}

private fun AIProvider.toDisplayName(): String = when (this) {
    AIProvider.OPENAI_GPT4 -> "OpenAI GPT-4"
    AIProvider.GOOGLE_GEMINI -> "Google Gemini"
    AIProvider.ANTHROPIC_CLAUDE -> "Anthropic Claude"
}