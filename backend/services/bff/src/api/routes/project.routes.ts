import { Router } from 'express';
import { getProject, createProject } from '../controllers/project.controller';
import { protect } from '../../middleware/auth.middleware';

const router = Router();

// All routes in this file are protected
router.use(protect);

router.get('/:id', getProject);
router.post('/', createProject);

export default router;
