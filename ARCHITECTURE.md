# PocketCode – Guía Unificada de Arquitectura y Roadmap

> Documento maestro que consolida la visión de arquitectura, el sistema de
> diseño, el plan de componentización y la hoja de ruta evolutiva de PocketCode.
> Última actualización: 2025-09-28.

---

## 1. Visión general

PocketCode es un IDE nativo de Android concebido para ofrecer una experiencia de
desarrollo profesional desde dispositivos móviles. La estrategia combina un
cliente rápido y modular con servicios en la nube que ejecutan las tareas
computacionalmente costosas.

### Principios rectores

- **Cloud-Native Augmented Client:** El dispositivo actúa como interfaz rica; la
  compilación, la generación asistida por IA y otros procesos intensivos se
  delegan a infraestructura serverless.
- **Arquitectura Limpia y Multi-Módulo:** Dependencias unidireccionales, módulos
  pequeños y altamente cohesionados para mejorar testabilidad y tiempos de
  build.
- **Seguridad por Diseño:** Ninguna credencial sensible vive en el cliente; un
  proxy seguro maneja claves y operaciones críticas.
- **Infrastructure as Code:** Terraform define servicios de GCP, garantizando
  reproducibilidad y control de versiones.
- **Iteración Guiada por Valor:** El roadmap prioriza entregar flujo de trabajo
  completo por fases, validando continuamente con usuarios reales.

---

## 2. Frontend – IDE nativo en Android

### 2.1 Stack tecnológico y patrones

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose con patrón **MVI** (Model–View–Intent)
- **Asincronía:** Kotlin Coroutines & Flows
- **DI:** Hilt
- **Navegación:** Navigation Compose
- **Testing:** JUnit5 + Compose UI Testing + Turbine para flujos

### 2.2 Estructura multi-módulo

| Capa         | Módulos clave                                                                                                                                        | Responsabilidad                                                                      | Dependencias                                  |
| ------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ | --------------------------------------------- |
| **App**      | `:app`                                                                                                                                               | Punto de entrada, orquesta navegación y DI global.                                   | `:features:*`                                 |
| **Features** | `:features:editor`, `:features:project`, `:features:settings`, `:features:ai`, `:features:designer`, `:features:marketplace`, `:features:management` | UI y lógica de presentación por caso de uso.                                         | `:domain:*`, `:core:ui`, `:core:utils`        |
| **Domain**   | `:domain:ide`, `:domain:project`, `:domain:ai`, `:domain:marketplace`                                                                                | Casos de uso, entidades, reglas de negocio puras.                                    | `:core:api`                                   |
| **Data**     | `:data:ide`, `:data:project`, `:data:ai`, `:data:marketplace`                                                                                        | Implementaciones de repositorios, persistencia local y remota.                       | `:domain:*`, `:core:network`, `:core:storage` |
| **Core**     | `:core:ui`, `:core:tokens`, `:core:network`, `:core:storage`, `:core:utils`, `:core:p2p`, `:core:api`                                                | Componentes compartidos, tokens de diseño, utilidades, conectividad y abstracciones. | –                                             |

### 2.3 Experiencia del IDE

- **Project Dashboard:** Lista proyectos, crea nuevos espacios y ofrece acceso a
  Marketplace y ajustes.
- **Main Workspace:** Combina `EditorView` tabulado, `FileExplorerPanel`,
  `AIAssistantPanel`, toolbar de acciones (Run, Git, Device Mesh) y panel
  inferior con Terminal/Build/Debugger.
- **Marketplace:** Catálogo y detalle de activos comunitarios con búsqueda y
  filtros.
- **Settings:** Preferencias de editor, cuenta, BYOK y personalización de temas.
- **Git Operations:** Pantalla dedicada con visor de diffs, mensaje de commit y
  acciones rápidas.

### 2.4 Estrategia de componentización

La refactorización se ejecuta en ocho fases iterativas:

1. **Componentes base:** PocketButton, PocketCard, PocketTextField,
   LoadingIndicator, ErrorDisplay, EmptyState.
2. **Navegación desacoplada:** PocketTopBar, NavigationContainer, TabIndicator,
   NavigationDrawer.
3. **Editor modular:** EditorContainer, EditorTopBar, EditorContent,
   SyntaxHighlighter, LineNumbers, EditorControls.
4. **Design system avanzado:** Tokens completos de color, tipografía, spacing,
   elevation, motion y variantes temáticas.
5. **Formularios y estado:** FormContainer, FieldGroup, ValidationDisplay y
   proveedores de estado compartido.
6. **Productividad del editor:** Autocompletado, búsqueda avanzada, code
   folding, minimap, multi-cursor.
7. **Temas expertos:** Soporte multi-tema, A/B testing, theme builder y vista
   previa en vivo.
8. **Optimización continua:** Tests específicos por componente, linting de
   tokens y monitorización de performance.

Impacto esperado: reducción del 40 % en duplicación de UI, pantallas reducidas

> 50 % en líneas de código y mayor velocidad de entrega.

### 2.5 Patrones UI transversales

- **Settings telemetradas:** `ModernSettingsScreen` combina `PocketScaffold`,
  `SectionLayout` y componentes `PocketSwitch`/`PocketFilterChip` con el
  `GlobalSnackbarDispatcher`. Cada ajuste dispara `settings_change` vía
  `SettingsTelemetry`, sincronizando los toggles de privacidad con Firebase. El
  patrón propone recoger metadatos (`surface`, `setting_key`, `change_type`) y
  respetar las preferencias guardadas en DataStore.
- **Diseñador visual:** `DesignerScreen` estructura paleta, lienzo y panel de
  propiedades sobre `PocketScaffold` + `SectionLayout`, reutiliza `PocketCard`
  para los componentes del canvas y emite snackbars globales
  (`GlobalSnackbarOrigin.DESIGNER`). El siguiente hito es habilitar drag & drop,
  snap-to-grid y la vista previa en vivo reutilizando el renderizador remoto.
- **Feedback efímero unificado:** `MainAppScreen` hospeda `PocketSnackbarHost` +
  `PocketToastHost` y expone `LocalGlobalSnackbarDispatcher` /
  `LocalGlobalToastDispatcher` para que las features emitan mensajes sin recrear
  hosts. `FileExplorer`, `ProjectSelection` y `EditorContainer` ya despachan
  toasts de creación/guardado con `PocketToastStyle` y origenes analíticos
  (`GlobalSnackbarOrigin.PROJECTS/EDITOR`).

### 2.6 Sistema de diseño

- **Principios:** Minimalismo, claridad, eficiencia y gestos nativos.
- **Paletas:**
  - Tema claro – `primary #007AFF`, `secondary #8E8E93`, `background #F2F2F7`,
    `surface #FFFFFF`, `success #34C759`, `error #FF3B30`.
  - Tema oscuro – `primary #0A84FF`, `secondary #8E8E93`, `background #000000`,
    `surface #1C1C1E`, `success #30D158`, `error #FF453A`.
- **Tipografía:** Inter/Roboto con jerarquía `h1 34px`, `h2 28px`, `h3 22px`,
  `body 17px`, `subheadline 15px`, `caption 13px`, line-height 1.5.
- **Spacing:** Escala 4-48 px (múltiplos de 8 px por defecto).
- **Iconografía:** Feather/Heroicons 24 px, estilo outline consistente.
- **Buenas prácticas:** Prohibido hardcodear estilos; usar tokens `PocketTheme`.
- **Ubicación del código fuente:**
  - Tokens: `frontend/core/ui/tokens/`
  - Componentes reutilizables: `frontend/core/ui/components/`
  - Thème principal: `frontend/core/ui/theme/`

---

## 3. Backend – Servicios serverless en GCP

### 3.1 Tecnologías y servicios

- **Compute:** Google Cloud Run (BFF, API Proxy, Marketplace Service)
- **BaaS:** Firebase Authentication, Firestore, Cloud Storage
- **Alternativas OSS evaluadas:** Supabase y Appwrite para control de costos a
  largo plazo.
- **Orquestación CI/CD:** GitHub Actions
- **Pasarela IA:** OpenRouter (proxy multi-modelo) con opción BYOK

### 3.2 Microservicios principales

1. **Backend for Frontend (BFF):** Punto de entrada único; autentica tokens de
   Firebase, agrega datos y expone REST/GraphQL optimizado para mobile.
2. **Secure API Proxy:** Gestiona claves (plataforma y BYOK), enruta peticiones
   a proveedores IA, aplica caché y observabilidad.
3. **Marketplace Service:** CRUD de activos, perfiles de creadores, ratings y
   comentarios.

Cada servicio se contenedoriza (Dockerfile) y se despliega en Cloud Run; la
infraestructura se describe en `backend/terraform/`.

### 3.3 Modelo de datos

- **Firestore:**
  - `users` (perfiles, preferencias, claves cifradas BYOK)
  - `projects` (metadatos, vínculos a Cloud Storage)
  - `builds` (estado y logs de compilaciones remotas)
  - `marketplace_assets` y `asset_reviews`
- **Cloud Storage:**
  - `project-source-files/`
  - `build-artifacts/`
  - `marketplace-assets/`
- **Policies:** Ciclo de vida para artefactos antiguos, roles mínimos por
  bucket.

---

## 4. Pipeline "Code-to-Cloud-to-Store"

1. **Trigger:** Acción en el IDE (build/deploy) genera `git push` a repo
   privado.
2. **Workflow (GitHub Actions):**
   - Configura JDK y permisos de Gradle
   - Ejecuta `./gradlew bundleRelease`
   - Firma artefactos con claves almacenadas en GitHub Secrets
   - Sube APK/AAB a Cloud Storage
3. **Publicación (fase futura):** Integración con Google Play Developer API para
   subir artefactos, gestionar tracks y notas de versión.
4. **Retroalimentación:** Estado y logs sincronizados en Firestore para UI en
   tiempo real.

---

## 5. Seguridad y cumplimiento

- Todas las llamadas pasan por el BFF que valida tokens de Firebase.
- El proxy seguro encapsula las claves de terceros (Gemini, OpenAI, etc.) y
  soporta BYOK cifrado por usuario.
- El cliente aplica ofuscación, checksums y detección de manipulación.
- Principle of Least Privilege en Firestore/Storage; auditoría con Cloud
  Logging.

---

## 6. Roadmap evolutivo

### Fase 1 – MVP local (Compleción alta)

- Editor de código con tabs y resaltado (Kotlin/Java/XML)
- Gestión de proyectos y explorador de archivos local
- Git básico offline (init/add/commit)
- Settings para tema y tipografía
- Tests: capa de datos con buena cobertura; pendiente reforzar UI/ViewModel

### Fase 2 – Integración en la nube

- **Backend:** Implementar y desplegar BFF, API Proxy, Marketplace skeleton.
- **Frontend:** Autenticación con Firebase, módulo `:core:network`, build remoto
  con monitoreo en tiempo real, panel inicial del Asistente IA, BYOK.
- **CI/CD:** Workflow `android-publish.yml` operativo.

### Fase 3 – Monetización y Visual Designer MVP

- Integrar servicio de suscripciones (RevenueCat u otro) en cliente y servidor.
- Gateo de funcionalidades premium (minutos de build, modelos IA avanzados).
- Pantalla "Go Pro" y gestión in-app.
- Canvas WYSIWYG (`:features:designer`) con edición básica de propiedades.

### Fase 4 – Ecosistema y liderazgo

- Marketplace completo (uploads, ratings, reviews, distribución de activos).
- Pipeline extendido con publicación automática en Google Play.
- Visual Designer bidireccional (code↔design en tiempo real).
- Device Mesh sobre `:core:p2p` con sincronización multi-dispositivo.
- AI Assistant avanzado (generación de módulos, code review automática).

---

## 7. Referencias rápidas para el equipo

- **Código frontend:** `frontend/`
- **Código backend:** `backend/services/`
- **Infraestructura:** `backend/terraform/`
- **Tokens y temas:** `frontend/core/ui/tokens/`, `frontend/core/ui/theme/`
- **Componentes reutilizables:** `frontend/core/ui/components/`
- **Guía de implementación modular:** ver componentes `features/editor/ui/`
- **Pipeline de usuario:** `backend/user-project-template/.github/workflows/`

Este documento sustituye a `AI_DEVELOPMENT_ROADMAP.md`,
`Frontend_architecture.md`, `Backend_architecture.md`, `DESIGN_SYSTEM.md`,
`COMPONENTIZATION.md` y `PHASE1_Architecture.md`. Para historial detallado o
versiones previas, consulta el control de versiones del repositorio.
