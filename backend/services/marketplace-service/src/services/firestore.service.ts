import { db } from '../config/firebase';

import { FieldValue } from 'firebase-admin/firestore';

interface AssetData {
    name: string;
    description: string;
    authorId: string;
    filePath: string;
}

interface ReviewData {
    userId: string;
    comment: string;
}

interface RatingData {
    userId: string;
    value: number; // 1-5
}

class FirestoreService {
    private assetsCollection = db.collection('marketplace-assets');

    /**
     * Creates a new asset document in Firestore.
     * @param assetData The data for the new asset.
     * @returns The newly created asset object with its ID.
     */
    async createAsset(assetData: AssetData) {
        const timestamp = new Date();
        const docRef = await this.assetsCollection.add({
            ...assetData,
            createdAt: timestamp,
            updatedAt: timestamp,
            averageRating: 0,
            ratingCount: 0,
        });

        const newAsset = await docRef.get();
        return {
            id: newAsset.id,
            ...newAsset.data(),
        };
    }

    /**
     * Retrieves all assets from Firestore.
     */
    async getAllAssets() {
        const snapshot = await this.assetsCollection.orderBy('createdAt', 'desc').get();
        if (snapshot.empty) {
            return [];
        }
        return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    }

    /**
     * Retrieves a single asset by its document ID.
     * @param assetId The ID of the asset to retrieve.
     */
    async getAssetById(assetId: string) {
        const docRef = this.assetsCollection.doc(assetId);
        const doc = await docRef.get();

        if (!doc.exists) {
            return null;
        }

        return { id: doc.id, ...doc.data() };
    }

    /**
     * Adds a review to an asset.
     */
    async addReviewToAsset(assetId: string, reviewData: ReviewData) {
        const reviewCollection = this.assetsCollection.doc(assetId).collection('reviews');
        const timestamp = new Date();
        const docRef = await reviewCollection.add({
            ...reviewData,
            createdAt: timestamp,
        });
        return { id: docRef.id, ...reviewData };
    }

    /**
     * Adds a rating to an asset and updates the asset's average rating.
     * This is done within a transaction to ensure atomicity.
     */
    async addRatingToAsset(assetId: string, ratingData: RatingData) {
        const assetRef = this.assetsCollection.doc(assetId);

        return db.runTransaction(async (transaction) => {
            const assetDoc = await transaction.get(assetRef);
            if (!assetDoc.exists) {
                throw new Error("Asset not found");
            }

            // Add the new rating to a subcollection
            const ratingRef = assetRef.collection('ratings').doc(ratingData.userId);
            transaction.set(ratingRef, ratingData);

            // Calculate the new average rating
            const assetData = assetDoc.data();
            const currentRatingCount = assetData.ratingCount || 0;
            const currentAverageRating = assetData.averageRating || 0;
            const newRatingCount = currentRatingCount + 1;
            const newAverageRating = ((currentAverageRating * currentRatingCount) + ratingData.value) / newRatingCount;

            // Update the asset document with the new rating info
            transaction.update(assetRef, {
                ratingCount: newRatingCount,
                averageRating: newAverageRating,
                updatedAt: new Date(),
            });

            return { newAverageRating, newRatingCount };
        });
    }

    /**
     * Retrieves all reviews for a specific asset.
     */
    async getReviewsForAsset(assetId: string) {
        const reviewCollection = this.assetsCollection.doc(assetId).collection('reviews').orderBy('createdAt', 'desc');
        const snapshot = await reviewCollection.get();
        if (snapshot.empty) {
            return [];
        }
        return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    }
}

export const firestoreService = new FirestoreService();
