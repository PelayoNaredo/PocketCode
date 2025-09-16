import express from 'express';
import dotenv from 'dotenv';
import cors from 'cors';
import { initializeFirebaseAdmin } from './config/firebase';
import allRoutes from './api/routes';

// Load environment variables
dotenv.config();

// Initialize Firebase Admin SDK
initializeFirebaseAdmin();

// Basic server setup
const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for all routes
app.use(cors());

// Middleware to parse JSON bodies
app.use(express.json());

// Main application routes
app.use('/api/v1', allRoutes);

// Simple health check endpoint
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'ok' });
});

app.listen(PORT, () => {
    console.log(`BFF service listening on port ${PORT}`);
});
