import { Request, Response } from 'express';
import { storageService } from '../services/storage.service';
import { firestoreService } from '../services/firestore.service';

/**
 * Handles the asset upload request.
 * 1. Validates request body and file.
 * 2. Uploads the file to Google Cloud Storage.
 * 3. Creates a new asset document in Firestore.
 * 4. Returns the created asset data.
 */
export const uploadAsset = async (req: Request, res: Response) => {
    try {
        // 1. Validate request
        if (!req.file) {
            return res.status(400).json({ message: 'No file uploaded. The file should be in a field named "assetFile".' });
        }

        const { name, description, authorId } = req.body;
        if (!name || !description || !authorId) {
            return res.status(400).json({ message: 'Missing required fields in body: name, description, authorId.' });
        }

        // 2. Upload file to GCS
        const filePath = await storageService.uploadFile(req.file);

        // 3. Save asset metadata to Firestore
        const assetData = { name, description, authorId, filePath };
        const newAsset = await firestoreService.createAsset(assetData);

        // 4. Return success response
        return res.status(201).json(newAsset);

    } catch (error) {
        console.error('Error uploading asset:', error);
        // It's good practice to check the error type and provide a more specific message if possible.
        if (error instanceof Error) {
            return res.status(500).json({ message: 'Internal Server Error', error: error.message });
        }
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

/**
 * Retrieves all reviews for a specific asset.
 */
export const getReviews = async (req: Request, res: Response) => {
    try {
        const { assetId } = req.params;
        const reviews = await firestoreService.getReviewsForAsset(assetId);
        return res.status(200).json(reviews);
    } catch (error) {
        console.error(`Error getting reviews for asset ${req.params.assetId}:`, error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

/**
 * Adds a review to a specific asset.
 */
export const addReview = async (req: Request, res: Response) => {
    try {
        const { assetId } = req.params;
        const { userId, comment } = req.body;

        if (!userId || !comment) {
            return res.status(400).json({ message: 'Missing required fields: userId, comment.' });
        }

        const reviewData = { userId, comment };
        const newReview = await firestoreService.addReviewToAsset(assetId, reviewData);

        return res.status(201).json(newReview);
    } catch (error) {
        console.error(`Error adding review to asset ${req.params.assetId}:`, error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

/**
 * Adds a rating to a specific asset.
 */
export const addRating = async (req: Request, res: Response) => {
    try {
        const { assetId } = req.params;
        const { userId, value } = req.body;

        if (!userId || !value) {
            return res.status(400).json({ message: 'Missing required fields: userId, value.' });
        }

        if (typeof value !== 'number' || value < 1 || value > 5) {
            return res.status(400).json({ message: 'Rating value must be a number between 1 and 5.' });
        }

        const ratingData = { userId, value };
        const result = await firestoreService.addRatingToAsset(assetId, ratingData);

        return res.status(201).json(result);
    } catch (error) {
        console.error(`Error adding rating to asset ${req.params.assetId}:`, error);
        if (error.message === 'Asset not found') {
            return res.status(404).json({ message: 'Asset not found' });
        }
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

/**
 * Retrieves all assets.
 */
export const getAllAssets = async (req: Request, res: Response) => {
    try {
        const assets = await firestoreService.getAllAssets();
        return res.status(200).json(assets);
    } catch (error) {
        console.error('Error getting all assets:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

/**
 * Retrieves a single asset by its ID.
 */
export const getAssetById = async (req: Request, res: Response) => {
    try {
        const { assetId } = req.params;
        const asset = await firestoreService.getAssetById(assetId);

        if (!asset) {
            return res.status(404).json({ message: 'Asset not found' });
        }

        return res.status(200).json(asset);
    } catch (error) {
        console.error(`Error getting asset by ID ${req.params.assetId}:`, error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};
