import express from 'express';
import assetsRouter from './routes/assets';
import './config/firebase'; // Initializes Firebase Admin

const app = express();
const PORT = process.env.PORT || 3002;

app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'ok' });
});

// Mount the assets router
app.use('/assets', assetsRouter);

app.listen(PORT, () => {
    console.log(`Marketplace service listening on port ${PORT}`);
});
