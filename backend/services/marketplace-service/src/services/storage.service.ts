import { storage } from '../config/firebase';
import { v4 as uuidv4 } from 'uuid';

// In a real app, this would come from an environment variable
const BUCKET_NAME = 'pocketcode-asset-marketplace';

class StorageService {
  /**
   * Uploads a file to Google Cloud Storage.
   * @param file The file object from multer (Express.Multer.File).
   * @returns The full path to the uploaded file in GCS.
   */
  async uploadFile(file: Express.Multer.File): Promise<string> {
    const bucket = storage.bucket(BUCKET_NAME);

    // Create a unique filename to avoid overwrites
    const uniqueFilename = `${uuidv4()}-${file.originalname}`;
    const blob = bucket.file(uniqueFilename);

    return new Promise((resolve, reject) => {
      const blobStream = blob.createWriteStream({
        resumable: false,
        contentType: file.mimetype,
      });

      blobStream.on('error', (err) => {
        reject(err);
      });

      blobStream.on('finish', () => {
        const publicPath = `gs://${BUCKET_NAME}/${uniqueFilename}`;
        resolve(publicPath);
      });

      blobStream.end(file.buffer);
    });
  }
}

export const storageService = new StorageService();
