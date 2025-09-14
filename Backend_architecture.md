# Backend Architecture for PocketCode IDE

This document outlines the backend and cloud infrastructure for the PocketCode IDE. The architecture is designed to be serverless, scalable, and secure, offloading heavy computational tasks from the mobile client to the cloud.

## 1. Core Architectural Principles

- **Serverless-First:** All backend components will be deployed on serverless platforms to ensure scalability, reduce operational overhead, and maintain a cost-effective pay-per-use model.
- **Backend for Frontend (BFF):** A dedicated BFF will serve as the single entry point for the mobile client. It will aggregate data from various services and tailor responses specifically for the needs of the frontend, reducing client-side logic.
- **Infrastructure as Code (IaC):** While not detailed here, the cloud infrastructure should ideally be managed using tools like Terraform or Google Cloud Deployment Manager for reproducibility and version control.
- **Security by Design:** Security is paramount. Sensitive credentials like API keys will never be stored on the client. All sensitive operations will be handled by a secure server-side proxy.

## 2. Technology Stack & Services

- **Primary Cloud Provider:** Google Cloud Platform (GCP)
- **Compute:** Google Cloud Run (for BFF and API Proxy)
- **Database:** Cloud Firestore (for user data, project metadata, build logs)
- **File Storage:** Google Cloud Storage (for source code, assets, and build artifacts)
- **Authentication:** Firebase Authentication (for user management)
- **CI/CD Engine:** GitHub Actions (for the remote build pipeline)

## 3. System Components

### a. Backend for Frontend (BFF)
- **Service:** Google Cloud Run
- **Purpose:** To act as the main intermediary between the mobile client and backend services.
- **Responsibilities:**
  - Expose a clean, mobile-optimized REST/GraphQL API.
  - Handle business logic that involves multiple services (e.g., creating a new project entry in Firestore and its corresponding folder in Cloud Storage).
  - Authenticate and authorize all incoming requests from the client using Firebase Auth tokens.
  - Route requests to other internal services like the API Proxy or the Marketplace Service.

### b. Secure API Proxy
- **Service:** Google Cloud Run
- **Purpose:** To securely manage and use third-party API keys (e.g., for AI services like Gemini).
- **Workflow:**
  1. The mobile client makes a request to the proxy endpoint (e.g., `/proxy/ai/generate`).
  2. The proxy verifies the user's authentication token.
  3. It retrieves the appropriate API key (either the platform's key or a user-provided key).
  4. The proxy injects the key into the request and forwards it to the external service.
  5. It can optionally cache responses to reduce latency and cost.
  6. It returns the response to the client.

### c. Community Marketplace Service
- **Service:** Google Cloud Run
- **Purpose:** To handle all logic related to the community asset marketplace.
- **Responsibilities:**
  - Manage the lifecycle of assets (templates, components) including uploads, updates, and deletions.
  - Handle user profiles, ratings, and comments for assets.
  - Provide APIs for searching, filtering, and retrieving marketplace assets.

### d. CI/CD "Code-to-Cloud-to-Store" Pipeline
- **Service:** GitHub Actions
- **Purpose:** To compile, sign, and automatically publish the user's Android project.
- **Workflow:**
  1. **Trigger:** The user action in the IDE pushes the code to a private GitHub repository.
  2. **Execution & Build:** A GitHub Actions workflow builds the AAB/APK, using remote caching to speed up the process.
  3. **Signing:** The artifact is securely signed using a key stored in GitHub Secrets.
  4. **Artifact Storage:** The signed artifact is uploaded to Google Cloud Storage.
  5. **Automated Publishing:** The pipeline connects to the Google Play Developer API (using a secure service account) to:
     - Upload the AAB to a specified track (e.g., internal testing, alpha, beta).
     - Generate release notes from commit messages.
     - Submit the new version for review.
  6. **Notification:** The build/publish status and a link to the Play Store listing are written to Firestore and sent to the client.

## 4. Data Management

### Cloud Firestore
- **`users` collection:** Stores user profiles, settings, encrypted credentials, and creator profiles for the marketplace.
- **`projects` collection:** Stores metadata for each project.
- **`builds` collection:** Stores a log of all build and publishing jobs.
- **`marketplace_assets` collection:** Stores all metadata for marketplace assets, including description, author, version, and link to files in Cloud Storage.
- **`asset_reviews` collection:** Stores user ratings and comments for each asset.

### Google Cloud Storage
- **Buckets:**
  - `project-source-files/`: Stores the raw source code for each user project.
  - `build-artifacts/`: Stores the compiled APKs and AABs.
  - `marketplace-assets/`: Stores the downloadable files for community marketplace assets.
- **Lifecycle Policies:** Policies will be implemented to move older build artifacts to cheaper storage classes.

## 5. Backend Repository & CI/CD Structure

This section outlines the monorepo structure for the backend services.

### Backend Monorepo Structure

```
.
├── services/
│   ├── bff/
│   │   ├── Dockerfile
│   │   └── src/
│   ├── api-proxy/
│   │   ├── Dockerfile
│   │   └── src/
│   └── marketplace-service/
│       ├── Dockerfile
│       └── src/
├── terraform/
│   ├── cloud_run.tf
│   ├── firestore.tf
│   ├── storage.tf
│   └── variables.tf
├── user-project-template/
│   └── .github/
│       └── workflows/
│           └── android-publish.yml
├── .gitignore
└── README.md
```

### User Project CI/CD Structure

This structure exists within each user's private project repository on GitHub.

```
.
├── app/
│   ├── src/
│   └── build.gradle.kts
├── ... (user's project files)
└── .github/
    └── workflows/
        └── android-publish.yml
```

- **`android-publish.yml`:** This file contains the full definition of the CI/CD pipeline as described in Section 3d. It defines the jobs for building, signing, uploading the artifact, and publishing to the Google Play Store.
