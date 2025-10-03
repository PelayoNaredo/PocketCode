# Editor Modular – Documentación Unificada

Este documento consolida la información histórica de la migración del editor, la
arquitectura modular vigente y el backlog operativo pendiente.

## 📌 Resumen Ejecutivo

- El editor modular está en producción y sustituyó por completo a la
  implementación monolítica.
- `CodeEditor` es un wrapper fino que delega la experiencia en `EditorContainer`
  y sus componentes especializados.
- La capa de dominio y `CodeEditorViewModel` permanecen compatibles.

## 🚦 Estado de la Migración

- ✅ Arquitectura modular integrada como implementación por defecto.
- 🔄 Validación funcional y de rendimiento en curso.
- 📝 Documentación de soporte y playbooks pendiente de cierre.

### Cronograma de finalización (histórico)

- **Semana 1**: Arquitectura modular implementada, creación del nuevo editor y
  marcado del legacy.
- **Semana 2**: Actualización de referencias y navegación ➜ _Pendiente QA y
  performance_.
- **Semana 3**: Eliminación del código legacy, renombrados finales ➜ _Pendiente
  doc final_.

## 🔄 Proceso de Migración

1. **Nuevos componentes modulares creados**
   - `EditorContainer.kt`, `EditorTopBar.kt`, `EditorContent.kt`,
     `SyntaxHighlighter.kt`, `LineNumbers.kt`, `EditorComponents.kt`,
     `EditorActions.kt`.
2. **Nueva implementación integrada**
   - `CodeEditor.kt` delega en el contenedor modular.
   - Compatibilidad total con `CodeEditorViewModel` y capa de dominio.
3. **Migración aplicada al producto**
   - `NavigationHost` usa el editor modular y se eliminaron restos legacy.
   - Toda la UI del editor opera sobre componentes compartidos.
4. **Próximos pasos**
   - QA y cobertura de tests.
   - Validación de performance & UX.
   - Completar backlog funcional del ViewModel y playbooks de soporte.

## 🏗️ Arquitectura de Componentes (detalle)

```
CodeEditor (wrapper)
└── EditorContainer (orquestador principal)
    ├── EditorTopBar (acciones contextuales)
    ├── EditorContent (render + estado)
    ├── syntax/* (resaltado y temas >15 lenguajes)
    ├── line_numbers/* (gutter, métricas y breakpoints)
    ├── EditorComponents (status bar, minimap, chips)
    └── EditorActions (acciones y modelos auxiliares)
```

- **EditorContainer**: Estado global, auto-guardado, integración con
  find/replace, chips de acciones rápidas y minimapa.
- **EditorTopBar**: Acciones de guardar/format/undo/redo, metadatos del archivo
  y menús secundarios.
- **EditorContent**: Renderizado Compose, resaltado, selección y cursores
  (exponen callbacks para `navigateToLine`).
- **syntax/**: `SyntaxHighlighter.kt` y temas/tokenizers con cache LRU.
- **line_numbers/**: Gutter con indicadores y utilidades para métricas.
- **EditorComponents**: Barra de estado, minimapa, controles flotantes y tokens
  de diseño alineados con Material 3.
- **EditorActions**: `EditorQuickAction`, configuraciones, atajos y modelos de
  selección/cursor/viewport.

## ⚖️ Arquitectura Comparativa

```
Versión Legacy (pre-migración)
CodeEditor monolítico (800+ líneas)
├── UI y estado acoplados
├── Resaltado inline básico
├── Línea de comandos simplificada
└── Sin pruebas ni componentes reutilizables
```

```
Versión Modular Actual
CodeEditor (wrapper)
└── EditorContainer (orquestador principal)
    ├── EditorTopBar (acciones contextuales)
    ├── EditorContent (render + estado)
    ├── syntax/* (resaltado y temas)
    ├── line_numbers/* (gutter y métricas)
    ├── EditorComponents (status bar, minimap, chips)
    └── EditorActions (acciones y modelos auxiliares)
```

## 🎯 Beneficios Clave

- **Rendimiento**: Virtualización con Compose, cache LRU para highlighting y
  procesamiento asíncrono.
- **Funcionalidades**: Find/Replace avanzado, minimapa, code folding, soporte
  inicial multi-cursor, breakpoints y cuatro temas de sintaxis.
- **Mantenibilidad**: Separación de responsabilidades, componentes reutilizables
  y pruebas independientes por módulo.
- **UX/UI**: Material 3, diseño responsive, accesibilidad y animaciones
  consistentes.

## 🔧 Instrucciones para Desarrolladores

```kotlin
import com.pocketcode.features.editor.ui.CodeEditor

CodeEditor(
    file = file,
    selectedProjectId = projectId,
    selectedProjectName = projectName,
    selectedProjectPath = projectPath
)
```

- Interfaz pública idéntica a la versión legacy.
- Compatible con Hilt (`hiltViewModel()`) y la capa de dominio actual.

## 🧪 Recomendaciones de Testing

- **Unit tests**: `EditorContainer`, resaltado de sintaxis, find/replace,
  formatter.
- **Compose UI tests**: Abrir/editar/guardar archivos, manejo de errores IO.
- **Smoke tests**: Proyectos de ejemplo para validar rutas, permisos y encoding.

## ✅ Salud Actual

- Builds `:features:editor:compileDebugKotlin` y `:app:compileDebugKotlin`
  finalizan sin errores.
- `CodeEditor` delega completamente en `EditorContainer`; no quedan imports ni
  dependencias legacy.
- La UI reutiliza componentes compartidos (`EditorTopBar`, `EditorContent`,
  `FindReplace`, etc.).

## � Backlog Consolidado

| Área                  | Tarea                    | Detalle                                                                     |
| --------------------- | ------------------------ | --------------------------------------------------------------------------- |
| `CodeEditorViewModel` | Acciones de edición      | Implementar copy/cut/paste, select all, duplicate/delete line y go to line. |
| Multi cursor          | Integración real         | Conectar `MultiCursorState` con la UI cuando el dominio esté listo.         |
| Folding avanzado      | Persistencia de regiones | Aprovechar `folding/CodeFolding.kt` para mantener el estado entre sesiones. |

## 📝 TODO Detallado

1. **Acciones del ViewModel**
   - Implementar copy/cut/paste, select all, duplicate/delete line y go to line.
   - Integrar multi-cursor real y persistencia de folding cuando el dominio lo
     soporte.
2. **Automatización de Tests**
   - Unit tests para `EditorContainer`, resaltado, find/replace y formatter.
   - Compose UI tests para abrir/editar/guardar archivos y manejar errores.
   - Smoke tests sobre proyectos de ejemplo (paths, permisos, encoding).
3. **Performance & UX**
   - Medir tiempos de carga en archivos >5k líneas y consumo de memoria del
     minimapa/caches.
   - Ajustar accesibilidad (focus visible, lectores de pantalla, contraste).
4. **Documentación & Soporte**
   - Elaborar guía de atajos y troubleshooting.

- Publicar checklist de QA/performance y buenas prácticas de extensibilidad.
- Mantener este documento como fuente única en cada release.

## 📚 Playbooks y Soporte (pendiente)

- Guía de atajos de teclado.
- Procedimientos de troubleshooting comunes.
- Checklist de QA manual y performance.

## 🔄 Seguimiento

- Revisar el estado del backlog en cada retro/planning de la tribu IDE.
- Crear issues en el tracker para cada bloque (funcional, testing, performance,
  docs) y enlazarlos en release notes.
- Considerar la migración cerrada cuando los cuatro bloques estén verificados en
  CI/CD y la guía de soporte esté publicada.
