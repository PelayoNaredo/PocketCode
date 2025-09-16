import { firestore } from '../../config/firebase';

const subscriptionsCollection = firestore().collection('subscriptions');

export interface Subscription {
    userId: string;
    plan: 'free' | 'pro';
    status: 'active' | 'inactive' | 'cancelled';
    isActive: boolean;
}

/**
 * Gets the subscription status for a user.
 * For this MVP, we will mock a simple check. In a real app,
 * this would integrate with a service like RevenueCat.
 * @param userId The user's ID.
 * @returns The user's subscription status.
 */
export const getUserSubscriptionStatus = async (userId: string): Promise<Subscription> => {
    const docRef = subscriptionsCollection.doc(userId);
    const doc = await docRef.get();

    if (!doc.exists) {
        // By default, users are on the free plan
        return {
            userId,
            plan: 'free',
            status: 'active',
            isActive: false, // `isActive` refers to a paid, active subscription
        };
    }

    const data = doc.data();

    return {
        userId,
        plan: data?.plan || 'free',
        status: data?.status || 'inactive',
        // A subscription is considered active if the plan is 'pro' and status is 'active'
        isActive: data?.plan === 'pro' && data?.status === 'active',
    };
};
