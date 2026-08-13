import { Response } from 'express';
import path from 'path';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { publicFileUrl } from '../services/storage.service';
import { stringifyStringArray, parseStringArray } from '../utils/json';
import { toProfileDto } from '../utils/mappers';

const ALLOWED = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/jpg']);

export class UploadController {
  static async photo(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const file = (req as any).file as Express.Multer.File | undefined;
    if (!file) return fail(res, 'MISSING_FILE', 'A photo file is required (field name: photo).');
    if (file.mimetype && !ALLOWED.has(file.mimetype)) {
      return fail(res, 'INVALID_FILE_TYPE', 'Only JPEG, PNG, and WebP images are allowed.');
    }

    const kind = String(req.body?.kind || req.query.kind || 'profile');
    const filename = path.basename(file.filename || file.path);
    const url = publicFileUrl(filename);

    const profile = await prisma.profile.findUnique({ where: { userId } });
    if (!profile) return fail(res, 'NOT_FOUND', 'Profile not found.', 404);

    if (kind === 'verification') {
      await prisma.profile.update({
        where: { userId },
        data: { verificationSelfie: url, verificationStatus: 'PENDING' },
      });
    } else if (kind === 'gallery') {
      const photos = parseStringArray(profile.photoUrls);
      photos.push(url);
      await prisma.profile.update({
        where: { userId },
        data: {
          photoUrls: stringifyStringArray(photos.slice(-6)),
          primaryPhoto: profile.primaryPhoto || url,
        },
      });
    } else {
      const photos = parseStringArray(profile.photoUrls);
      if (!photos.includes(url)) photos.unshift(url);
      await prisma.profile.update({
        where: { userId },
        data: {
          primaryPhoto: url,
          photoUrls: stringifyStringArray(photos.slice(0, 6)),
        },
      });
    }

    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: { profile: true, faithProfile: true, subscription: true },
    });
    return ok(res, { url, kind, profile: toProfileDto(user!) }, 201);
  }
}
