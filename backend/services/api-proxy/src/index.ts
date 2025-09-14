import express from 'express';
import { proxyRequestHandler } from './handlers/proxy.handler';

// A placeholder for an auth middleware that would attach user info to the request.
const authMiddleware = (req: any, res: any, next: any) => {
    // In a real scenario, this would validate a token and fetch user details.
    req.user = { id: 'user-123' }; // Mock user
    next();
};


const app = express();
const PORT = process.env.PORT || 3001;

app.use(express.json());

// All proxy routes are protected and require authentication.
app.use(authMiddleware);

// A generic proxy endpoint. The specific service is determined by the URL parameter.
app.all('/proxy/:service/*', proxyRequestHandler);

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'ok' });
});

app.listen(PORT, () => {
    console.log(`API Proxy service listening on port ${PORT}`);
});
