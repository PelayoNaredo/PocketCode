// This is the main entry point for the Secure API Proxy service.
// It initializes a server that listens for requests from the mobile client,
// authenticates them, injects the necessary API key (either the platform's or a user's),
// and forwards the request to the target third-party service.
//
// Interacts with:
// - `./utils/`: For helper functions, e.g., retrieving API keys.
// - `Firebase Authentication`: To secure the proxy endpoint.
// - `Firestore`: To fetch API keys if using the "Bring Your Own Key" model.
// - `External APIs`: Such as Google Gemini or other AI services.
