import { Request, Response } from 'express';
import { ProjectService } from '../../core/services/project.service';

const projectService = new ProjectService();

export const getProject = async (req: Request, res: Response) => {
    // Logic to get a project
    res.status(200).json({ message: `GET project ${req.params.id}` });
};

export const createProject = async (req: Request, res: Response) => {
    // Logic to create a project
    res.status(201).json({ message: 'Project created' });
};

export const startBuildForProject = async (req: Request, res: Response) => {
    const { id } = req.params;
    const userId = (req as any).user.uid;

    try {
        const result = await projectService.startBuild(id, userId);
        // In a real scenario, we would also increment the usage here
        // after getting the build duration, maybe via a webhook.
        // For now, we'll increment a fixed amount.
        const { incrementBuildMinutes } = await import('../../core/services/usage.service');
        await incrementBuildMinutes(userId, 2); // Increment by 2 minutes for the simulated build

        res.status(202).json({ message: 'Build started successfully', ...result });
    } catch (error) {
        console.error(`Error starting build for project ${id}:`, error);
        res.status(500).json({ message: 'Internal server error' });
    }
};
