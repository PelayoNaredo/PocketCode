import { Router, Request, Response } from 'express';
import { protect } from '../../middleware/auth.middleware';
import { checkUsageLimits } from '../../middleware/subscription.middleware';
import { incrementAiCalls } from '../../core/services/usage.service';

const router = Router();

// This middleware is just for testing since auth.middleware is a placeholder
const mockAuth = (req: Request, res: Response, next: Function) => {
    (req as any).user = { uid: 'test-user-id' }; // Mock user
    next();
};

// This would eventually forward the request to the api-proxy service
router.post('/generate', protect, mockAuth, checkUsageLimits, async (req: Request, res: Response) => {
    const { prompt } = req.body;
    const userId = (req as any).user.uid;

    if (!prompt) {
        return res.status(400).json({ error: 'Prompt is required' });
    }

    try {
        // Increment the AI call count for the user
        await incrementAiCalls(userId);

        // In a real implementation, we'd call the api-proxy here.
        // For now, just echo the prompt.
        res.json({
            message: "Request received by BFF, would be proxied.",
            prompt: prompt,
            generated_code: `// Code generated for: ${prompt}`
        });
    } catch (error) {
        console.error('Error during AI code generation:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
});

export default router;
