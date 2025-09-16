# Stage 1: Builder
# This stage installs dependencies and builds the TypeScript source code into JavaScript.
FROM node:18-alpine AS builder

# Set the working directory
WORKDIR /usr/src/app

# Copy package.json and package-lock.json (if available)
COPY package*.json ./

# Install all dependencies, including devDependencies for building
RUN npm install

# Copy the rest of the source code
COPY . .

# Run the build script to compile TypeScript to JavaScript
RUN npm run build

# Stage 2: Runner
# This stage creates the final, lightweight image for production.
FROM node:18-alpine

WORKDIR /usr/src/app

# Copy package.json and package-lock.json (if available)
COPY package*.json ./

# Install only production dependencies
RUN npm install --omit=dev

# Copy the compiled code from the builder stage
COPY --from=builder /usr/src/app/dist ./dist

# Expose the port the app runs on (defaulting to 3001 for this service)
EXPOSE 3001

# Set the default command to run the application
CMD [ "node", "dist/index.js" ]
