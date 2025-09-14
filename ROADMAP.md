# PocketCode: Vision and Roadmap

This document outlines the strategic vision for PocketCode, detailing its core functionalities, planned improvements, and a roadmap of innovative features designed to establish it as the market leader for mobile development environments.

---

### **Part 1: Core Functionalities and Modules (The Final Application)**

This section synthesizes the planned features from all architectural documents into a cohesive overview.

#### **A. Frontend: The Native Android IDE**

The frontend is a feature-rich, native Android application built with Jetpack Compose, a multi-module architecture, and the MVI pattern.

*   **Modules:** The application will be structured into the following Gradle modules, ensuring separation of concerns and scalability:
    *   **App:** `:app`
    *   **Features:** `:features:editor`, `:features:project`, `:features:settings`
    *   **Domain:** `:domain:ide`, `:domain:project`
    *   **Data:** `:data:ide`, `:data:project`
    *   **Core:** `:core:ui`, `:core:utils`, `:core:network`, `:core:storage`, `:core:api`

*   **Core IDE Functionalities:**
    1.  **Smart Code Editor:**
        *   Syntax highlighting for Kotlin, Java, C++, and XML.
        *   Intelligent, local code autocompletion.
        *   Line numbering, indentation guides, and code formatting.
    2.  **Project & File Management:**
        *   Full support for standard Android project structures (manifests, Gradle files, resource folders).
        *   A responsive file explorer with tree view.
        *   Custom file management system to ensure interoperability with other tools like Termux.
    3.  **Integrated Tools:**
        *   A built-in terminal emulator.
        *   A full-featured Git client for version control (commit, push, pull, branch management, diff viewer).
        *   A lightweight, on-device performance monitor for CPU and memory usage.

*   **UI/UX (Based on the Design System):**
    1.  **Dual Theming:** A clean, minimal UI with both Light and Dark modes.
    2.  **Responsive Layout:** The interface will adapt seamlessly to phones, tablets, and foldable devices.
    3.  **Intuitive Gestures:** Will incorporate swipe actions, pull-to-refresh, and long-press for context menus.

#### **B. Backend: Serverless Cloud Infrastructure**

The backend is built on Google Cloud Platform, designed to be serverless, scalable, and secure.

*   **Services:**
    1.  **Backend for Frontend (BFF):** A Cloud Run service that acts as the single, mobile-optimized entry point for the app.
    2.  **Secure API Proxy:** A separate Cloud Run service to manage all third-party API keys (e.g., for AI services), ensuring no sensitive keys are ever exposed on the client.

*   **Data & File Management:**
    1.  **Cloud Firestore:** For storing user profiles, project metadata, build logs, and securely stored user API keys.
    2.  **Google Cloud Storage:** For storing project source code and build artifacts (APKs/AABs).

#### **C. "Code-to-Cloud" CI/CD Pipeline**

This is the application's cornerstone feature, offloading heavy compilation tasks to the cloud.

*   **Engine:** GitHub Actions.
*   **Workflow:**
    1.  **Trigger:** A user action in the IDE triggers a `git push` to a private GitHub repository.
    2.  **Execution:** A GitHub Actions workflow automatically builds the Android project.
    3.  **Secure Signing:** The application's release key is stored securely in GitHub Secrets and used to sign the build artifact.
    4.  **Distribution:** The signed APK/AAB is uploaded to Google Cloud Storage for the user to download.

#### **D. Monetization Strategy**

*   **Freemium Model:**
    *   **Free Tier:** Core IDE features are free, with a limited monthly allowance of cloud builds and AI assistant calls.
    *   **Pro Tier:** A subscription unlocks generous quotas for cloud builds, advanced AI features, and team collaboration tools.
*   **Bring Your Own Key (BYOK):** Users can use their own API keys for AI services.

---

### **Part 2: List of Possible Improvements**

These are enhancements to the existing planned features that would add significant value.

1.  **Enhanced Editor:**
    *   Advanced refactoring tools (e.g., "extract method").
    *   Support for popular keybinding sets like Vim and Emacs.
    *   Real-time pair programming functionality.
2.  **Smarter CI/CD:**
    *   Implement remote build caching to dramatically speed up subsequent builds.
    *   Provide out-of-the-box templates for other frameworks like Flutter or Kotlin Multiplatform.
3.  **Deeper Debugging:**
    *   Integrate a remote debugger that can connect to the cloud build process.
4.  **UI/Layout Customization:**
    *   Allow users to rearrange the IDE's layout (dockable panels).
    *   A community theme marketplace.

---

### **Part 3: Unplanned Features to Dominate the Market**

These are innovative, forward-thinking features that could make this app the leader in its category.

1.  **True AI-Powered Development Assistant:**
    *   An AI assistant that can **generate entire boilerplate modules** from a prompt, suggest architectural improvements, and perform automated AI-driven code reviews.
2.  **Fully Automated App Store Publishing:**
    *   Extend the CI/CD pipeline to **automatically manage the entire Google Play Store submission process**, including generating release notes and managing release tracks.
3.  **Integrated Visual UI Designer:**
    *   A **WYSIWYG (What You See Is What You Get) editor for Jetpack Compose**, allowing users to visually build their UI.
4.  **"Device Mesh" Development Environment:**
    *   Allow a developer to connect multiple Android devices into a single, cohesive workspace (e.g., tablet for code, phone for live preview).
5.  **Community & Asset Marketplace:**
    *   An integrated platform where developers can publish, share, and sell reusable project templates, UI components, and modules.
