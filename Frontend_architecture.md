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

The project will be organized into three main layers: **Feature**, **Domain**, and **Data**, along with several **Core** modules for shared functionality.

| Module Gradle           | Type                | Responsibility                                                                    | Dependencies                                |
| ----------------------- | ------------------- | --------------------------------------------------------------------------------- | ------------------------------------------- |
| `:app`                  | App                 | Entry point, orchestrates navigation, contains `AndroidManifest.xml`.               | `:features:editor`, `:features:project`, etc. |
| `:features:editor`      | Android Library     | UI logic for the code editor, text editor, syntax highlighting, etc.              | `:domain:ide`, `:core:ui`, `:core:utils`    |
| `:features:project`     | Android Library     | UI logic for project management, file explorer, Git sync, etc.                    | `:domain:project`, `:core:ui`, `:core:utils`|
| `:features:settings`    | Android Library     | UI logic for user settings, themes, and account management.                       | `:domain:ide`, `:core:ui`                   |
| `:domain:ide`           | Kotlin Library      | Core IDE business logic (use cases for code generation, refactoring).             | `:domain:project`, `:core:api`              |
| `:domain:project`       | Kotlin Library      | Core project management business logic (file use cases, data models).             | `:core:api`                                 |
| `:data:ide`             | Kotlin Library      | Implements IDE repositories, manages AI API calls via the BFF.                    | `:domain:ide`, `:core:network`              |
| `:data:project`         | Kotlin Library      | Implements project repositories, manages local database and filesystem access.    | `:domain:project`, `:core:storage`          |
| `:core:ui`              | Android Library     | Shared Jetpack Compose components, themes, and UI utilities.                      | -                                           |
| `:core:utils`           | Kotlin Library      | General-purpose utilities and helper functions (validators, etc.).                | -                                           |
| `:core:network`         | Kotlin Library      | Shared HTTP client (e.g., Ktor or Retrofit) and configuration for API calls.      | -                                           |
| `:core:storage`         | Kotlin Library      | Abstraction classes for local filesystem and database (e.g., Room) access.        | -                                           |
| `:core:api`             | Kotlin Library      | Interfaces and data models (DTOs) for communication between layers.               | -                                           |

## 4. Screens and UI Components

### Screen: Project Dashboard
- **Purpose:** First screen after launch. Shows a list of user projects.
- **Components:**
  - `ProjectList`: A lazy list of project cards.
  - `CreateProjectButton`: Floating action button to start a new project.
  - `OpenProjectButton`: To open a project from device storage.
  - `TopAppBar`: Contains access to global settings and user profile.

### Screen: Main IDE Workspace
- **Purpose:** The central hub for all development activities.
- **Components:**
  - **`CodeEditor`:** A custom editor view supporting:
    - Syntax highlighting for Kotlin, Java, XML.
    - Code completion suggestions.
    - Line numbers and indentation guides.
  - **`FileExplorerPanel`:** A side panel (collapsible) with a tree view of the project structure.
    - Context menus for file operations (create, rename, delete).
  - **`TopActionToolbar`:**
    - `RunButton`: Triggers the remote CI/CD build pipeline.
    - `GitControls`: Buttons for commit, push, pull, and branch management.
  - **`BottomPanel` (Tabbed Layout):**
    - `TerminalView`: An integrated terminal emulator.
    - `BuildOutputView`: Displays logs from the remote build process.
    - `DebuggerView`: UI for the debugger.

### Screen: Settings
- **Purpose:** Allows users to configure the IDE.
- **Components:**
  - `EditorPreferences`: Settings for font, theme, keybindings.
  - `AccountPreferences`: Manage user account and subscription.
  - `BYOKPreferences`: Section for users to input their own API keys (Bring Your Own Key).

### Screen: Git Operations
- **Purpose:** A dedicated screen for managing Git commits.
- **Components:**
  - `DiffViewer`: Shows staged changes.
  - `CommitMessageInput`: Text field for the commit message.
  - `CommitButton`: Executes the commit.

## 5. Project Directory Structure

This is a representation of the project's folder and file structure, illustrating the multi-module approach.

```
.
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/pocketcode/
│           ├── PocketCodeApp.kt
│           └── ui/
│               ├── MainActivity.kt
│               └── navigation/
│                   └── AppNavHost.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
├── core/
│   ├── api/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/pocketcode/core/api/
│   ├── network/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/pocketcode/core/network/
│   ├── storage/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/pocketcode/core/storage/
│   ├── ui/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/pocketcode/core/ui/
│   └── utils/
│       ├── build.gradle.kts
│       └── src/main/java/com/pocketcode/core/utils/
├── data/
│   ├── ide/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/pocketcode/data/ide/
│   └── project/
│       ├── build.gradle.kts
│       └── src/main/java/com/pocketcode/data/project/
├── domain/
│   ├── ide/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/pocketcode/domain/ide/
│   └── project/
│       ├── build.gradle.kts
│       └── src/main/java/com/pocketcode/domain/project/
└── features/
    ├── editor/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/pocketcode/features/editor/
    ├── project/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/pocketcode/features/project/
    └── settings/
        ├── build.gradle.kts
        └── src/main/java/com/pocketcode/features/settings/
```
