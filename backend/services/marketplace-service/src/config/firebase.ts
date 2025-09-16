import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { getStorage } from 'firebase-admin/storage';

// In a real-world scenario, the service account key would be loaded
// from a secure location, like Google Secret Manager.
// For this development environment, we assume it's provided via
// an environment variable or a local file.

// const serviceAccount = require('/path/to/serviceAccountKey.json');

initializeApp({
  // credential: cert(serviceAccount),
  // If running in Google Cloud environment (e.g., Cloud Run),
  // credentials are automatically discovered.
});

const db = getFirestore();
const storage = getStorage();

export { db, storage };
