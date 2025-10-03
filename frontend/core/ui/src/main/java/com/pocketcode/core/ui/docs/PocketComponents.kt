package com.pocketcode.core.ui.docs

/**
 * # Guía de componentes Pocket
 *
 * Este archivo documenta los patrones de uso de los componentes del sistema de diseño Pocket,
 * con ejemplos prácticos y mejores prácticas. Todos los componentes están disponibles en
 * el módulo `:core:ui`.
 *
 * ## Índice
 * 1. [Feedback efímero](#feedback-efimero)
 *    - [PocketToast](#pockettoast)
 *    - [PocketSnackbar](#pocketsnackbar)
 * 2. [Diálogos](#dialogos)
 * 3. [Formularios](#formularios)
 * 4. [Navegación](#navegacion)
 * 5. [Layout](#layout)
 *
 * ---
 *
 * ## 1. Feedback efímero {#feedback-efimero}
 *
 * El sistema de feedback unificado permite emitir mensajes transitorios desde cualquier
 * módulo sin recrear hosts. La shell (`MainAppScreen`) hospeda `PocketToastHost` y
 * `PocketSnackbarHost`, exponiendo dispatchers vía CompositionLocal.
 *
 * ### PocketToast {#pockettoast}
 *
 * **Cuándo usar:**
 * - Confirmaciones rápidas (guardado exitoso, elemento copiado)
 * - Notificaciones no críticas (sincronización completada)
 * - Feedback de acciones sin requerir interacción
 *
 * **Cuándo NO usar:**
 * - Errores que requieren acción del usuario → usar `PocketSnackbar` con botón
 * - Información permanente → usar `ErrorDisplay` o `EmptyState`
 * - Confirmaciones críticas → usar `PocketDialog`
 *
 * **Ejemplo básico:**
 * ```kotlin
 * @Composable
 * fun MyFeatureScreen() {
 *     val toastDispatcher = LocalGlobalToastDispatcher.current
 *
 *     PocketButton(
 *         text = "Guardar",
 *         onClick = {
 *             // ... lógica de guardado ...
 *             toastDispatcher.showMessage(
 *                 message = "Proyecto guardado",
 *                 style = PocketToastStyle.Success,
 *                 origin = GlobalSnackbarOrigin.PROJECTS,
 *                 duration = PocketToastDuration.Short
 *             )
 *         }
 *     )
 * }
 * ```
 *
 * **Ejemplo con LaunchedEffect (reaccionar a estado):**
 * ```kotlin
 * @Composable
 * fun EditorContainer(
 *     uiState: EditorUiState,
 *     viewModel: EditorViewModel
 * ) {
 *     val toastDispatcher = LocalGlobalToastDispatcher.current
 *
 *     // Emitir toast cuando cambia el resultado de guardado
 *     LaunchedEffect(uiState.saveResult) {
 *         when (uiState.saveResult) {
 *             is SaveResult.Success -> {
 *                 toastDispatcher.showMessage(
 *                     message = "Cambios guardados",
 *                     style = PocketToastStyle.Success,
 *                     origin = GlobalSnackbarOrigin.EDITOR
 *                 )
 *             }
 *             is SaveResult.Error -> {
 *                 toastDispatcher.showMessage(
 *                     message = "Error al guardar: ${uiState.saveResult.message}",
 *                     style = PocketToastStyle.Error,
 *                     origin = GlobalSnackbarOrigin.EDITOR,
 *                     duration = PocketToastDuration.Extended
 *                 )
 *             }
 *             null -> { /* sin acción */ }
 *         }
 *     }
 * }
 * ```
 *
 * **Estilos disponibles:**
 * - `PocketToastStyle.Info` → Azul (información general)
 * - `PocketToastStyle.Success` → Verde (operación exitosa)
 * - `PocketToastStyle.Warning` → Amarillo (advertencia, no crítico)
 * - `PocketToastStyle.Error` → Rojo (error, puede requerir acción)
 *
 * **Duraciones:**
 * - `PocketToastDuration.Short` → 2.5s (confirmaciones rápidas)
 * - `PocketToastDuration.Extended` → 5s (errores, mensajes más largos)
 *
 * **Orígenes para telemetría:**
 * Los orígenes permiten agregar métricas por módulo en Firebase Analytics:
 * - `SHELL` → Acciones globales de navegación
 * - `PROJECTS` → Creación/apertura/importación de proyectos
 * - `EDITOR` → Edición de código, guardado, compilación
 * - `AI_ASSISTANT` → Chat, generación de código
 * - `MARKETPLACE` → Búsqueda, descarga, sincronización
 * - `SETTINGS` → Cambios de configuración
 * - `DESIGNER` → Edición visual, canvas
 * - `AUTH` → Login, registro, recuperación de contraseña
 *
 * **Ejemplo en ViewModel (inyectando el dispatcher):**
 * ```kotlin
 * @HiltViewModel
 * class MarketplaceHomeViewModel @Inject constructor(
 *     private val getAssetsUseCase: GetMarketplaceAssetsUseCase,
 *     private val toastDispatcher: GlobalToastDispatcher
 * ) : ViewModel() {
 *
 *     fun loadAssets() {
 *         viewModelScope.launch {
 *             getAssetsUseCase()
 *                 .onSuccess { assets ->
 *                     toastDispatcher.showMessage(
 *                         message = "${assets.size} recursos cargados",
 *                         style = PocketToastStyle.Success,
 *                         origin = GlobalSnackbarOrigin.MARKETPLACE
 *                     )
 *                 }
 *                 .onFailure { error ->
 *                     toastDispatcher.showMessage(
 *                         message = "Error al cargar recursos",
 *                         style = PocketToastStyle.Error,
 *                         origin = GlobalSnackbarOrigin.MARKETPLACE,
 *                         duration = PocketToastDuration.Extended
 *                     )
 *                 }
 *         }
 *     }
 * }
 * ```
 *
 * ---
 *
 * ### PocketSnackbar {#pocketsnackbar}
 *
 * **Cuándo usar:**
 * - Errores que requieren acción del usuario (reintentar, deshacer)
 * - Confirmaciones con opción de cancelar
 * - Mensajes que necesitan más contexto (texto de apoyo)
 *
 * **Cuándo NO usar:**
 * - Confirmaciones simples sin acción → usar `PocketToast`
 * - Errores críticos → usar `PocketDialog`
 * - Información permanente → usar `ErrorDisplay`
 *
 * **Ejemplo básico:**
 * ```kotlin
 * @Composable
 * fun FileExplorerScreen() {
 *     val snackbarDispatcher = LocalGlobalSnackbarDispatcher.current
 *
 *     PocketButton(
 *         text = "Eliminar archivo",
 *         onClick = {
 *             snackbarDispatcher.dispatch(
 *                 GlobalSnackbarEvent(
 *                     message = "Archivo eliminado",
 *                     actionLabel = "Deshacer",
 *                     onAction = { /* restaurar archivo */ },
 *                     severity = GlobalSnackbarSeverity.WARNING,
 *                     origin = GlobalSnackbarOrigin.PROJECTS,
 *                     analyticsId = "file_delete_undo"
 *                 )
 *             )
 *         }
 *     )
 * }
 * ```
 *
 * **Ejemplo con texto de apoyo y callback de descarte:**
 * ```kotlin
 * snackbarDispatcher.dispatch(
 *     GlobalSnackbarEvent(
 *         message = "Sin conexión",
 *         supportingText = "Verifica tu red e intenta de nuevo",
 *         actionLabel = "Reintentar",
 *         onAction = { viewModel.retryConnection() },
 *         onDismiss = { logDismissal("network_error") },
 *         severity = GlobalSnackbarSeverity.ERROR,
 *         origin = GlobalSnackbarOrigin.MARKETPLACE,
 *         duration = GlobalSnackbarDuration.LONG,
 *         analyticsId = "marketplace_offline_retry"
 *     )
 * )
 * ```
 *
 * **Diferencias clave Toast vs Snackbar:**
 *
 * | Aspecto              | PocketToast                    | PocketSnackbar                  |
 * |----------------------|--------------------------------|---------------------------------|
 * | Posición             | Bottom-center (flotante)       | Bottom (anclado al scaffold)    |
 * | Acción               | Opcional (raro)                | Recomendada (Retry, Undo, etc.) |
 * | Duración             | Corta (2.5-5s)                 | Configurable (4s-∞)             |
 * | Uso típico           | Confirmaciones rápidas         | Errores recuperables            |
 * | Interrupción         | Baja (no bloquea interacción)  | Media (requiere atención)       |
 *
 * ---
 *
 * ## 2. Diálogos {#dialogos}
 *
 * ### PocketDialog
 *
 * **Variantes:**
 * - **Confirmación:** Acción destructiva (eliminar, salir sin guardar)
 * - **Formulario:** Entrada de datos (nombre de proyecto, clave API)
 * - **Información:** Ayuda, about, términos de servicio
 *
 * **Ejemplo de confirmación (danger):**
 * ```kotlin
 * var showDeleteDialog by remember { mutableStateOf(false) }
 *
 * if (showDeleteDialog) {
 *     PocketDialog(
 *         title = "¿Eliminar proyecto?",
 *         description = "Esta acción no se puede deshacer. Todos los archivos se perderán.",
 *         confirmText = "Eliminar",
 *         dismissText = "Cancelar",
 *         onConfirm = {
 *             viewModel.deleteProject()
 *             showDeleteDialog = false
 *         },
 *         onDismiss = { showDeleteDialog = false },
 *         isDanger = true
 *     )
 * }
 * ```
 *
 * **Ejemplo de formulario:**
 * ```kotlin
 * var showRenameDialog by remember { mutableStateOf(false) }
 * var newName by remember { mutableStateOf("") }
 *
 * if (showRenameDialog) {
 *     PocketDialog(
 *         title = "Renombrar proyecto",
 *         confirmText = "Guardar",
 *         dismissText = "Cancelar",
 *         onConfirm = {
 *             viewModel.renameProject(newName)
 *             showRenameDialog = false
 *         },
 *         onDismiss = { showRenameDialog = false }
 *     ) {
 *         PocketTextField(
 *             value = newName,
 *             onValueChange = { newName = it },
 *             label = "Nuevo nombre",
 *             modifier = Modifier.fillMaxWidth()
 *         )
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## 3. Formularios {#formularios}
 *
 * ### FormContainer
 *
 * **Características:**
 * - Layout responsivo (columna en móvil, grid en tablet/desktop)
 * - Manejo automático de scroll
 * - Estados de carga unificados
 * - Botón de submit integrado
 *
 * **Ejemplo básico (login):**
 * ```kotlin
 * @Composable
 * fun LoginScreen() {
 *     val emailState = rememberFieldState()
 *     val passwordState = rememberFieldState()
 *     val isLoading by viewModel.isLoading.collectAsState()
 *
 *     FormContainer(
 *         title = "Iniciar sesión",
 *         description = "Accede a tus proyectos",
 *         isLoading = isLoading,
 *         submitText = "Entrar",
 *         onSubmit = {
 *             viewModel.login(emailState.value, passwordState.value)
 *         }
 *     ) {
 *         PocketTextField(
 *             value = emailState.value,
 *             onValueChange = { emailState.value = it },
 *             label = "Email",
 *             modifier = Modifier.fillMaxWidth()
 *         )
 *
 *         PocketPasswordField(
 *             value = passwordState.value,
 *             onValueChange = { passwordState.value = it },
 *             label = "Contraseña",
 *             modifier = Modifier.fillMaxWidth()
 *         )
 *     }
 * }
 * ```
 *
 * **Ejemplo con validación:**
 * ```kotlin
 * val emailState = rememberFieldState(
 *     validator = { email ->
 *         when {
 *             email.isBlank() -> "El email es obligatorio"
 *             !email.contains("@") -> "Email inválido"
 *             else -> null
 *         }
 *     }
 * )
 *
 * PocketTextField(
 *     value = emailState.value,
 *     onValueChange = { emailState.value = it },
 *     label = "Email",
 *     error = emailState.error,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * ---
 *
 * ## 4. Navegación {#navegacion}
 *
 * ### PocketTopBar
 *
 * **Variantes:**
 * - **Simple:** Título + navegación
 * - **Con subtítulo:** Título + descripción contextual
 * - **Con acciones:** Hasta 3 botones de acción (más overflows)
 * - **Con tabs:** Navegación horizontal integrada
 *
 * **Ejemplo con acciones:**
 * ```kotlin
 * PocketTopBar(
 *     title = "Marketplace",
 *     subtitle = "Descubre plantillas y recursos",
 *     navigationIcon = PocketIcons.ChevronLeft,
 *     onNavigationClick = { navController.popBackStack() },
 *     actions = listOf(
 *         TopBarAction(
 *             icon = PocketIcons.Search,
 *             contentDescription = "Buscar",
 *             onClick = { showSearchDialog = true }
 *         ),
 *         TopBarAction(
 *             icon = PocketIcons.Add,
 *             contentDescription = "Publicar recurso",
 *             onClick = { navController.navigate("marketplace/upload") }
 *         )
 *     )
 * )
 * ```
 *
 * ### NavigationContainer
 *
 * **Reemplaza:** `HorizontalPager` manual + indicadores custom
 * **Ventajas:** Sincronización automática con tabs, animaciones tokenizadas, estado persistente
 *
 * **Ejemplo (settings con tabs):**
 * ```kotlin
 * val tabs = listOf(
 *     TabItem(id = "general", title = "General", icon = Icons.Default.Settings),
 *     TabItem(id = "editor", title = "Editor", icon = Icons.Default.Edit),
 *     TabItem(id = "ai", title = "IA", icon = Icons.AutoMirrored.Filled.Chat)
 * )
 *
 * PocketScaffold(
 *     config = PocketScaffoldConfig(hasTabs = true),
 *     topBar = { PocketTopBar(title = "Configuración") },
 *     tabs = tabs,
 *     selectedTabIndex = selectedTab,
 *     onTabSelected = { index -> selectedTab = index }
 * ) { paddingValues ->
 *     NavigationContainer(
 *         selectedIndex = selectedTab,
 *         onPageChange = { selectedTab = it }
 *     ) {
 *         GeneralSettingsTab()
 *         EditorSettingsTab()
 *         AISettingsTab()
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## 5. Layout {#layout}
 *
 * ### PocketScaffold
 *
 * **Ventajas sobre `Scaffold` de Material3:**
 * - Padding unificado con tokens semánticos
 * - Safe areas automáticos (iOS, notch, barras de navegación)
 * - Configuración declarativa (scroll, background, padding)
 * - Integración con NavigationContainer
 *
 * **Ejemplo básico:**
 * ```kotlin
 * PocketScaffold(
 *     config = PocketScaffoldConfig(
 *         hasTopBar = true,
 *         isScrollable = true,
 *         paddingValues = PaddingValues(
 *             horizontal = SpacingTokens.Semantic.screenPaddingHorizontal,
 *             vertical = SpacingTokens.Semantic.screenPaddingVertical
 *         )
 *     ),
 *     topBar = {
 *         PocketTopBar(
 *             title = "Mi pantalla",
 *             navigationIcon = PocketIcons.ChevronLeft,
 *             onNavigationClick = onBack
 *         )
 *     }
 * ) { paddingValues ->
 *     LazyColumn(
 *         modifier = Modifier.padding(paddingValues)
 *     ) {
 *         // contenido...
 *     }
 * }
 * ```
 *
 * ### SectionLayout
 *
 * **Uso:** Agrupar campos relacionados con título y separador visual
 *
 * **Ejemplo:**
 * ```kotlin
 * SectionLayout(title = "Apariencia") {
 *     Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)) {
 *         ModernSettingsSwitch(
 *             title = "Tema oscuro",
 *             subtitle = "Activar modo nocturno",
 *             checked = isDarkTheme,
 *             onCheckedChange = { viewModel.updateTheme(it) }
 *         )
 *         ModernSettingsSwitch(
 *             title = "Animaciones",
 *             subtitle = "Habilitar transiciones",
 *             checked = animationsEnabled,
 *             onCheckedChange = { viewModel.updateAnimations(it) }
 *         )
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## Mejores prácticas generales
 *
 * 1. **Tokens sobre valores hardcoded:**
 *    ```kotlin
 *    // ❌ Evitar
 *    Spacer(modifier = Modifier.height(16.dp))
 *
 *    // ✅ Preferir
 *    Spacer(modifier = Modifier.height(SpacingTokens.medium))
 *    ```
 *
 * 2. **Origenes de telemetría consistentes:**
 *    - Cada módulo debe usar su propio `GlobalSnackbarOrigin`
 *    - Permite agregar métricas por feature en Analytics
 *
 * 3. **Estados de UI centralizados:**
 *    - `LoadingIndicator` para cargas
 *    - `EmptyState` para listas vacías
 *    - `ErrorDisplay` para errores persistentes
 *
 * 4. **Feedback apropiado según contexto:**
 *    - Operación exitosa sin consecuencias → `PocketToast.Success`
 *    - Error recuperable con acción → `PocketSnackbar` + botón
 *    - Acción destructiva → `PocketDialog` (confirmación)
 *
 * 5. **Accesibilidad:**
 *    - Todos los `Icon` deben tener `contentDescription`
 *    - Botones con estado `enabled` según validaciones
 *    - Contraste mínimo WCAG AA (automático con tokens)
 *
 * ---
 *
 * ## Referencias
 *
 * - **Tokens:** `com.pocketcode.core.ui.tokens.*`
 * - **Componentes:** `com.pocketcode.core.ui.components.*`
 * - **Iconos:** `com.pocketcode.core.ui.icons.PocketIcons`
 * - **Arquitectura:** `ARCHITECTURE.md` (sección "Feedback efímero unificado")
 * - **Migración:** `MIGRACION_0928.md` (inventario de equivalencias Material3 → Pocket)
 *
 * ---
 *
 * **Última actualización:** 1 de octubre de 2025
 * **Versión:** 1.0.0
 * **Mantenedores:** Equipo PocketCode UI
 */
