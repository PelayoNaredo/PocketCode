import express from 'express';
import allRoutes from './api/routes';

// Basic server setup
const app = express();
const PORT = process.env.PORT || 3000;

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
