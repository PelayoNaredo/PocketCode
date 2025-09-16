import { Router } from 'express';
import {
    uploadAsset,
    getAllAssets,
    getAssetById,
    addReview,
    addRating,
    getReviews
} from '../controllers/assets.controller';
import multer from 'multer';

const router = Router();

// Configure multer to handle file uploads.
// Using memoryStorage to temporarily hold the file before uploading to GCS.
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 5 * 1024 * 1024, // 5 MB limit
  },
});

// Define the POST route for uploading a new asset.
// The 'assetFile' string must match the name attribute of the file input form on the frontend.
router.post('/', upload.single('assetFile'), uploadAsset);

// GET all assets
router.get('/', getAllAssets);

// GET a single asset by its ID
router.get('/:assetId', getAssetById);

// GET all reviews for an asset
router.get('/:assetId/reviews', getReviews);

// POST a new review for an asset
router.post('/:assetId/reviews', addReview);

// POST a new rating for an asset
router.post('/:assetId/ratings', addRating);


export default router;
