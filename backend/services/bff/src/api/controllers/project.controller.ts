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
