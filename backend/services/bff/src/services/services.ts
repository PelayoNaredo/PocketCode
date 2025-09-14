// This file (or directory) contains the core business logic for the BFF.
// It abstracts the interactions with backend data sources like Firestore and Cloud Storage.
// For example, a `ProjectService` might have functions like `createProject(...)`
// which would handle creating a record in Firestore and a corresponding folder in GCS.
//
// This keeps the route handlers clean and focused on HTTP-level concerns.
//
// Interacts with:
// - `Firestore`: To access the `projects`, `users`, and `builds` collections.
// - `Google Cloud Storage`: To manage source code and asset files.
// - `../routes/`: This is where the services are called from.
