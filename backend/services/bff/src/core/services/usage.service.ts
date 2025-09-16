import { firestore } from '../../config/firebase';
import * as admin from 'firebase-admin';

const usageCollection = firestore().collection('usage');

interface UserUsage {
    userId: string;
    buildMinutes: number;
    aiCalls: number;
    lastReset: Date;
}

const FREE_TIER_LIMITS = {
    buildMinutes: 100, // example limit
    aiCalls: 1000,     // example limit
};

/**
 * Gets the current usage for a user. If the user has no usage data,
 * it initializes it with default values.
 * @param userId The user's ID.
 * @returns The user's usage data.
 */
export const getUserUsage = async (userId: string): Promise<UserUsage> => {
    const docRef = usageCollection.doc(userId);
    const doc = await docRef.get();

    if (!doc.exists) {
        const newUsage: UserUsage = {
            userId,
            buildMinutes: 0,
            aiCalls: 0,
            lastReset: new Date(),
        };
        await docRef.set(newUsage);
        return newUsage;
    }

    return doc.data() as UserUsage;
};

/**
 * Increments the build minutes for a user.
 * @param userId The user's ID.
 * @param minutes The number of minutes to add.
 */
export const incrementBuildMinutes = async (userId: string, minutes: number) => {
    const docRef = usageCollection.doc(userId);
    await docRef.update({
        buildMinutes: admin.firestore.FieldValue.increment(minutes),
    });
};

/**
 * Increments the AI calls for a user.
 * @param userId The user's ID.
 */
export const incrementAiCalls = async (userId: string) => {
    const docRef = usageCollection.doc(userId);
    await docRef.update({
        aiCalls: admin.firestore.FieldValue.increment(1),
    });
};

/**
 * Checks if a user has exceeded their free tier limits.
 * @param userId The user's ID.
 * @returns True if the user has exceeded their limits, false otherwise.
 */
export const hasExceededFreeTier = async (userId: string): Promise<boolean> => {
    const usage = await getUserUsage(userId);
    // Here you might also check subscription status from another service/collection
    // For now, we assume everyone is on the free tier.
    return usage.buildMinutes >= FREE_TIER_LIMITS.buildMinutes || usage.aiCalls >= FREE_TIER_LIMITS.aiCalls;
};
