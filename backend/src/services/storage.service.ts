import path from 'path';
import fs from 'fs';
import { config } from '../config';

const uploadDir = path.resolve(process.cwd(), process.env.UPLOAD_DIR || 'uploads');

export function ensureUploadDir() {
  if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
  }
  return uploadDir;
}

export function publicBaseUrl(): string {
  return (process.env.PUBLIC_BASE_URL || `http://localhost:${config.port}`).replace(/\/$/, '');
}

export function publicFileUrl(filename: string): string {
  return `${publicBaseUrl()}/uploads/${filename}`;
}

export function getUploadDir() {
  return ensureUploadDir();
}
