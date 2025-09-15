import { Router } from 'express';
import projectRoutes from './project.routes';
import aiRoutes from './ai.routes';
import userRoutes from './user.routes';

const router = Router();

// Mount the project routes
router.use('/projects', projectRoutes);

// Mount the AI routes
router.use('/ai', aiRoutes);

// Mount the User routes
router.use('/user', userRoutes);

export default router;
