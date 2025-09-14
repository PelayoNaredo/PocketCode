# Frontend Architecture for PocketCode IDE

This document outlines the proposed frontend architecture for the PocketCode IDE, a native Android application. The design is based on the principles of modern, scalable, and maintainable application development.

## 1. Core Architectural Principles

- **Clean Architecture:** The project will follow the principles of Clean Architecture to separate concerns, improve testability, and ensure the codebase is maintainable in the long term. This creates a unidirectional flow of dependencies, with inner layers being independent of the outer layers.
- **Multi-Module Structure:** The application will be divided into multiple Gradle modules. This approach improves build times, enforces separation of concerns, and allows for better team collaboration.
- **UI Pattern - MVI (Model-View-Intent):** The presentation layer will use the MVI pattern with Jetpack Compose. MVI provides a unidirectional data flow, which makes the state of the UI predictable and easy to debug.
- **Dependency Injection:** Hilt will be used for dependency injection to manage object lifecycles and dependencies between different parts of the application.

## 2. Technology Stack

- **UI Toolkit:** Jetpack Compose (Declarative UI)
- **Language:** Kotlin
- **Asynchronous Programming:** Kotlin Coroutines & Flows
- **Dependency Injection:** Hilt
- **Navigation:** Jetpack Navigation Compose

## 3. Module Structure

The project will be organized into three main layers: **Feature**, **Domain**, and **Data**, along with several **Core** modules for shared functionality. The architecture is designed to be highly modular to support advanced future features.

| Module Gradle           | Type                | Responsibility                                                                    | Dependencies                                |
| ----------------------- | ------------------- | --------------------------------------------------------------------------------- | ------------------------------------------- |
| `:app`                  | App                 | Entry point, orchestrates navigation, contains `AndroidManifest.xml`.               | All `:features:*` modules                   |
| `:features:editor`      | Android Library     | UI for the code editor, text editor, syntax highlighting, etc.                    | `:domain:ide`, `:core:ui`, `:core:utils`    |
| `:features:project`     | Android Library     | UI for project management, file explorer, Git sync, etc.                          | `:domain:project`, `:core:ui`, `:core:utils`|
| `:features:settings`    | Android Library     | UI for user settings, themes, and account management.                             | `:domain:ide`, `:core:ui`                   |
| `:features:designer`    | Android Library     | UI for the Visual (WYSIWYG) Jetpack Compose designer.                             | `:domain:ide`, `:core:ui`                   |
| `:features:marketplace` | Android Library     | UI for the community asset marketplace (templates, components).                   | `:domain:marketplace`, `:core:ui`           |
| `:features:ai`          | Android Library     | UI for the AI Assistant panel, code generation prompts, and reviews.              | `:domain:ai`, `:core:ui`                    |
| `:domain:ide`           | Kotlin Library      | Core IDE business logic (use cases for code generation, refactoring).             | `:domain:project`, `:core:api`              |
| `:domain:project`       | Kotlin Library      | Core project management business logic (file use cases, data models).             | `:core:api`                                 |
| `:domain:marketplace`   | Kotlin Library      | Business logic for the marketplace (fetching assets, user interactions).          | `:core:api`                                 |
| `:domain:ai`            | Kotlin Library      | Business logic for the AI assistant (use cases for generation, analysis).         | `:core:api`                                 |
| `:data:ide`             | Kotlin Library      | Implements IDE repositories, manages AI API calls via the BFF.                    | `:domain:ide`, `:core:network`              |
| `:data:project`         | Kotlin Library      | Implements project repositories, manages local database and filesystem access.    | `:domain:project`, `:core:storage`          |
| `:data:marketplace`     | Kotlin Library      | Implements marketplace repositories, communicates with the marketplace backend service. | `:domain:marketplace`, `:core:network`    |
| `:data:ai`              | Kotlin Library      | Implements AI repositories, handling advanced interactions with the AI proxy.     | `:domain:ai`, `:core:network`               |
| `:core:ui`              | Android Library     | Shared Jetpack Compose components, themes, and UI utilities.                      | -                                           |
| `:core:utils`           | Kotlin Library      | General-purpose utilities and helper functions (validators, etc.).                | -                                           |
| `:core:network`         | Kotlin Library      | Shared HTTP client (e.g., Ktor or Retrofit) and configuration for API calls.      | -                                           |
| `:core:storage`         | Kotlin Library      | Abstraction classes for local filesystem and database (e.g., Room) access.        | -                                           |
| `:core:p2p`             | Kotlin Library      | Handles peer-to-peer communication for the "Device Mesh" feature.                 | -                                           |
| `:core:api`             | Kotlin Library      | Interfaces and data models (DTOs) for communication between layers.               | -                                           |

## 4. Screens and UI Components

### Screen: Project Dashboard
- **Purpose:** First screen after launch. Shows a list of user projects.
- **Components:**
  - `ProjectList`: A lazy list of project cards.
  - `CreateProjectButton`: Floating action button to start a new project.
  - `OpenProjectButton`: To open a project from device storage.
  - `TopAppBar`: Contains access to global settings, user profile, and the Marketplace.

### Screen: Main IDE Workspace
- **Purpose:** The central hub for all development activities.
- **Components:**
  - **`EditorView` (Tabbed):** A container that can switch between:
    - **`CodeEditor`:** The traditional text-based editor.
    - **`VisualDesigner`:** The new WYSIWYG UI designer.
  - **`FileExplorerPanel`:** A side panel (collapsible) with a tree view of the project structure.
  - **`AIAssistantPanel`:** A collapsible/overlay panel for interacting with the AI assistant.
  - **`TopActionToolbar`:**
    - `RunButton`: Triggers the remote CI/CD build pipeline.
    - `GitControls`: Buttons for commit, push, pull, and branch management.
    - `DeviceMeshButton`: To connect and manage devices in the mesh.
  - **`BottomPanel` (Tabbed Layout):**
    - `TerminalView`: An integrated terminal emulator.
    - `BuildOutputView`: Displays logs from the remote build process.
    - `DebuggerView`: UI for the debugger.

### Screen: Community Marketplace
- **Purpose:** Browse, search, and download community-created assets.
- **Components:**
  - `AssetGrid/List`: Displays assets like templates and components.
  - `SearchAndFilterBar`: To find specific assets.
  - `AssetDetailView`: Screen showing details for a single asset.
  - `UploadAssetScreen`: A form for users to upload their own creations.

### Screen: Settings
- **Purpose:** Allows users to configure the IDE.
- **Components:**
  - `EditorPreferences`: Settings for font, theme, keybindings.
  - `AccountPreferences`: Manage user account and subscription.
  - `BYOKPreferences`: Section for users to input their own API keys.
  - `MarketplacePreferences`: Settings related to asset uploads and creator profile.

### Screen: Git Operations
- **Purpose:** A dedicated screen for managing Git commits.
- **Components:**
  - `DiffViewer`: Shows staged changes.
  - `CommitMessageInput`: Text field for the commit message.
  - `CommitButton`: Executes the commit.

## 5. Project Directory Structure

This is a representation of the project's folder and file structure, illustrating the expanded multi-module approach.

```
.
├── app/
│   └── src/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
├── core/
│   ├── api/
│   ├── network/
│   ├── p2p/
│   ├── storage/
│   ├── ui/
│   └── utils/
├── data/
│   ├── ai/
│   ├── ide/
│   ├── marketplace/
│   └── project/
├── domain/
│   ├── ai/
│   ├── ide/
│   ├── marketplace/
│   └── project/
└── features/
    ├── ai/
    ├── designer/
    ├── editor/
    ├── marketplace/
    ├── project/
    └── settings/
```
