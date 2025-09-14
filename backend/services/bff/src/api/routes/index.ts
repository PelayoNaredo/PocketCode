import { Router } from 'express';
import projectRoutes from './project.routes';

const router = Router();

// Mount the project routes
router.use('/projects', projectRoutes);

export default router;
