import { Request, Response, NextFunction } from 'express';
import { hasExceededFreeTier } from '../core/services/usage.service';
import { getUserSubscriptionStatus } from '../core/services/subscription.service'; // Assuming this service exists

/**
 * Middleware to check if a user is on the free tier and has exceeded their usage limits.
 * This should run AFTER the authentication middleware.
 */
export const checkUsageLimits = async (req: Request, res: Response, next: NextFunction) => {
    // This assumes that the auth middleware has already attached the user object to the request.
    const userId = (req as any).user?.uid;

    if (!userId) {
        // This case should ideally be handled by the auth middleware, but as a safeguard:
        return res.status(401).json({ message: 'Unauthorized. User not authenticated.' });
    }

    try {
        // First, check the user's subscription status
        const subscription = await getUserSubscriptionStatus(userId);

        // If the user has an active subscription, they bypass the usage check
        if (subscription.isActive) {
            return next();
        }

        // If the user is on the free tier, check their usage
        const hasExceeded = await hasExceededFreeTier(userId);
        if (hasExceeded) {
            return res.status(403).json({
                message: 'You have exceeded the limits of the free tier. Please upgrade to continue.',
                code: 'limits_exceeded',
            });
        }

        next();
    } catch (error) {
        console.error('Error in subscription middleware:', error);
        return res.status(500).json({ message: 'Internal server error while checking usage limits.' });
    }
};
