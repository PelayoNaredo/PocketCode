// This file extends the default Express Request type to include a 'user' property.
// This is useful for storing the authenticated user object in middleware.

declare namespace Express {
  export interface Request {
    user?: any; // In a real app, you'd define a proper User type here
  }
}
