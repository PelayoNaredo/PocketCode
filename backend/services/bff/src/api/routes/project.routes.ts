import { Router, Request, Response } from 'express';
import { getProject, createProject, startBuildForProject } from '../controllers/project.controller';
import { protect } from '../../middleware/auth.middleware';
import { checkUsageLimits } from '../../middleware/subscription.middleware';

const router = Router();

// This middleware is just for testing since auth.middleware is a placeholder
const mockAuth = (req: Request, res: Response, next: Function) => {
    (req as any).user = { uid: 'test-user-id' }; // Mock user
    next();
};

// All routes in this file are protected
router.use(protect, mockAuth);

router.get('/:id', getProject);
router.post('/', createProject);

// Route to start a build, protected by usage limits
router.post('/:id/build', checkUsageLimits, startBuildForProject);


export default router;
