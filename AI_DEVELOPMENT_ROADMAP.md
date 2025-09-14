# AI Development Roadmap for PocketCode

## Introduction

This document provides a detailed, phased development roadmap for the PocketCode IDE. It is intended to be used by an AI Software Engineer to guide the implementation of the project. Each phase has a clear goal, a list of contextual documents to prioritize, specific instructions, and a TODO list.

**General Instruction for AI Agent:** Before starting any phase, ensure you have fully synthesized the context from the specified markdown files. The architecture is designed to be modular, so focus on implementing one module at a time, ensuring it is well-tested before moving to the next.

---

## Phase 1: Minimum Viable Product (MVP) - The Core Local IDE

**Goal:** Create a functional, local-only Android IDE. The user must be able to create, open, edit, and manage standard Android projects entirely on their device. Cloud features are out of scope for this phase.

**Contextual MDs:**
*   `Frontend_architecture.md`: Your primary guide for the module structure and screens.
*   `DESIGN_SYSTEM.md`: Your guide for all UI components, themes, and styling.

**Instructions for AI Agent:**
*   You will work exclusively in the `frontend/` directory.
*   Begin by implementing the foundational `:core` modules, especially `:core:ui` (for shared components), `:core:storage` (for local file access), and `:core:utils`.
*   Implement the project management features (`:features:project`, `:domain:project`, `:data:project`). This includes the Project Dashboard screen and a fully functional file explorer.
*   Implement the core editor feature (`:features:editor`). Focus on a stable text view with reliable syntax highlighting for Kotlin, Java, and XML.
*   All data persistence (project files, settings) must be handled locally on the device's storage. No network calls should be implemented in this phase.

**Phase 1 TODO List:**
- [ ] Implement the Project Dashboard screen to list and create new projects.
- [ ] Implement the file explorer panel with file/folder creation, renaming, and deletion.
- [ ] Implement the core code editor view with a tabbed interface for multiple files.
- [ ] Implement syntax highlighting for Kotlin, Java, and XML.
- [ ] Implement a basic settings screen (`:features:settings`) to allow users to change the theme (light/dark) and font size.
- [ ] Integrate a basic, local-only Git client for version control (init, add, commit).
- [ ] Write comprehensive unit and integration tests for all local file operations and UI state management.

---

## Phase 2: Cloud Integration - Remote Builds & Basic AI

**Goal:** Connect the IDE to the cloud to enable the core value proposition: remote builds. This phase also introduces the first tier of AI-powered features.

**Contextual MDs:**
*   `Backend_architecture.md`: Your primary guide for all backend services and data models.
*   `README.md`: Review section 5, "El Pipeline de CI/CD", for the workflow logic.
*   `Frontend_architecture.md`: For implementing the client-side of the cloud features.

**Instructions for AI Agent:**
*   You will now work across both the `frontend/` and `backend/` directories.
*   **Backend:**
    *   Focus on implementing the three core services in `backend/services/`: `bff`, `api-proxy`, and the initial `marketplace-service` skeleton.
    *   Implement the Firestore database rules and the Cloud Storage bucket policies as defined in `Backend_architecture.md`.
    *   Define the user project CI/CD workflow (`android-publish.yml`) and test it.
*   **Frontend:**
    *   Implement user authentication using Firebase Authentication.
    *   Implement the `:core:network` module to handle all API calls to the BFF.
    *   The "Build" button must now trigger a `git push` and communicate with the backend to monitor the build process.
    *   Implement the UI (`:features:ai`) for the AI Assistant. The first version should support basic code generation through a prompt.
    *   Implement the "Bring Your Own Key" (BYOK) functionality in the settings screen.

**Phase 2 TODO List:**
- [ ] **Backend:** Implement and deploy the BFF, Secure API Proxy, and Marketplace services.
- [ ] **Backend:** Define and test the GitHub Actions workflow for building and signing an AAB.
- [ ] **Frontend:** Implement the complete user sign-up and login flow.
- [ ] **Frontend:** Connect the "Build" button to the backend to initiate a remote build.
- [ ] **Frontend:** Create a UI to display real-time build logs streamed from Firestore.
- [ ] **Frontend:** Implement the AI Assistant panel and connect it to the API proxy for code generation.
- [ ] **Frontend:** Implement the BYOK screen to allow users to store their own API keys.

---

## Phase 3: Monetization & Visual Designer MVP

**Goal:** Introduce the freemium business model and deliver the first version of a major standout feature: the Visual UI Designer.

**Contextual MDs:**
*   `README.md`: Review section 6, "Monetización y Estrategia de Negocio".
*   `ROADMAP.md`: Review Part 3 for the vision of the standout features.

**Instructions for AI Agent:**
*   Integrate a subscription management service (like RevenueCat) on both the frontend and backend.
*   The backend BFF must now have middleware to check a user's subscription status and gate access to premium features (e.g., number of build minutes, access to advanced AI models).
*   The frontend must be updated to lock/unlock features based on the subscription status returned from the backend.
*   Begin the implementation of the Visual UI Designer (`:features:designer`). The MVP should focus on rendering a canvas of Jetpack Compose components and allowing basic property modification. Two-way binding (code-to-design and design-to-code) is out of scope for the MVP.

**Phase 3 TODO List:**
- [ ] Integrate the chosen subscription management SDK on both client and server.
- [ ] **Backend:** Implement logic to track and limit resource usage (build minutes, AI calls) for free-tier users.
- [ ] **Frontend:** Develop the "Go Pro" paywall and subscription management UI.
- [ ] **Frontend:** Implement the UI for the Visual Designer (`:features:designer`).
- [ ] **Frontend:** The designer canvas should render a preview of the user's Jetpack Compose UI.
- [ ] **Frontend:** Implement a properties panel to modify basic attributes of selected components (e.g., text, color).

---

## Phase 4: Ecosystem & Market Leadership

**Goal:** Fully realize the project's vision by building out the community ecosystem and completing the advanced features.

**Contextual MDs:**
*   `ROADMAP.md`: This is your primary guide for the full vision of all features.
*   `Frontend_architecture.md` & `Backend_architecture.md`: Refer to these for the complete module and service structures.

**Instructions for AI Agent:**
*   This phase is about deep feature enrichment.
*   **Marketplace:** Implement the full functionality, allowing users to upload, rate, and comment on assets. This will require significant work on both the `marketplace-service` and the `:features:marketplace` module.
*   **CI/CD:** Extend the `android-publish.yml` workflow to connect to the Google Play API and handle automated releases.
*   **Visual Designer:** Implement two-way, real-time synchronization between the visual canvas and the code editor.
*   **Device Mesh:** Implement the P2P networking logic in `:core:p2p` to allow multi-device workflows.

**Phase 4 TODO List:**
- [ ] **Marketplace:** Implement user asset uploads, ratings, and a review system.
- [ ] **CI/CD:** Add automated publishing to the Google Play Store to the CI/CD workflow.
- [ ] **Visual Designer:** Implement design-to-code generation.
- [ ] **Visual Designer:** Implement code-to-design real-time updates.
- [ ] **AI Assistant:** Enhance the AI to support full module generation and automated code reviews.
- [ ] **Device Mesh:** Implement P2P device discovery and synchronization for a multi-screen experience.
