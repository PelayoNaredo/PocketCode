import * as admin from 'firebase-admin';

export const initializeFirebaseAdmin = () => {
    if (admin.apps.length > 0) {
        return;
    }

    const serviceAccountKey = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;

    if (!serviceAccountKey) {
        throw new Error('FIREBASE_SERVICE_ACCOUNT_KEY environment variable is not set.');
    }

    try {
        const serviceAccount = JSON.parse(Buffer.from(serviceAccountKey, 'base64').toString('ascii'));
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount),
        });
        console.log('Firebase Admin SDK initialized successfully.');
    } catch (error) {
        console.error('Error initializing Firebase Admin SDK:', error);
        process.exit(1);
    }
};

export const firestore = () => admin.firestore();
export const auth = () => admin.auth();
