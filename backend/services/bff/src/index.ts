// This is the main entry point for the Backend for Frontend (BFF) service.
// It will initialize the server (e.g., Express), set up middleware for
// authentication and logging, and register the API routes.
// It orchestrates the core business logic, interacting with various GCP services.
//
// Interacts with:
// - `./routes/`: Defines the API endpoints exposed to the mobile client.
// - `Firebase Authentication`: To secure the endpoints.
// - `Firestore`: To fetch or modify data.
// - `Google Cloud Storage`: To manage file operations.
