import { Response } from 'express';
import { z } from 'zod';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { stringifyStringArray } from '../utils/json';
import { toProfileDto } from '../utils/mappers';

const updateProfileSchema = z.object({
  fullName: z.string().min(2).optional(),
  bio: z.string().max(1000).optional(),
  country: z.string().optional(),
  city: z.string().optional(),
  occupation: z.string().optional(),
  education: z.string().optional(),
  relationshipIntention: z.string().optional(),
  primaryPhoto: z.string().optional(),
  photoUrls: z.array(z.string()).optional(),
  diet: z.string().optional(),
  alcohol: z.string().optional(),
  smoking: z.string().optional(),
  wantsChildren: z.string().optional(),
  hasChildren: z.boolean().optional(),
  interests: z.array(z.string()).optional(),
  gender: z.string().optional(),
  isPaused: z.boolean().optional(),
  verificationSelfie: z.string().optional(),
  verificationStatus: z.string().optional(),
});

const faithProfileSchema = z.object({
  adventistAffiliation: z.string().min(2).optional(),
  yearsAsAdventist: z.number().int().min(0).optional(),
  localChurch: z.string().optional(),
  isBaptized: z.boolean().optional(),
  faithImportance: z.string().optional(),
  churchInvolvement: z.string().optional(),
  sabbathObservance: z.array(z.string()).optional(),
  ministryInterests: z.array(z.string()).optional(),
  personalBibleStudy: z.string().optional(),
  favoriteVerse: z.string().optional(),
});

async function loadUser(userId: string) {
  return prisma.user.findUnique({
    where: { id: userId },
    include: { profile: true, faithProfile: true, subscription: true },
  });
}

export class ProfileController {
  static async getProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const user = await loadUser(userId);
    if (!user?.profile) return fail(res, 'NOT_FOUND', 'Profile not found.', 404);
    return ok(res, toProfileDto(user));
  }

  static async updateProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    try {
      const body = updateProfileSchema.parse(req.body);
      const data: Record<string, unknown> = { ...body };
      if (body.photoUrls) data.photoUrls = stringifyStringArray(body.photoUrls);
      if (body.interests) data.interests = stringifyStringArray(body.interests);
      delete data.verificationStatus;

      if (body.verificationSelfie) {
        data.verificationSelfie = body.verificationSelfie;
        data.verificationStatus = 'PENDING';
      }

      await prisma.profile.upsert({
        where: { userId },
        update: data,
        create: {
          userId,
          fullName: body.fullName || 'AdventHearts Member',
          dateOfBirth: new Date('1998-01-01'),
          gender: body.gender || 'Unspecified',
          country: body.country || '',
          city: body.city || '',
          ...data,
        },
      });

      const user = await loadUser(userId);
      return ok(res, toProfileDto(user!));
    } catch (error: any) {
      return fail(res, 'VALIDATION_ERROR', 'Invalid profile payload', 400, { details: error.errors });
    }
  }

  static async getFaithProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const faith = await prisma.faithProfile.findUnique({ where: { userId } });
    if (!faith) return fail(res, 'NOT_FOUND', 'Faith profile not found.', 404);

    const user = await loadUser(userId);
    return ok(res, toProfileDto(user!));
  }

  static async updateFaithProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    try {
      const body = faithProfileSchema.parse(req.body);
      const data: Record<string, unknown> = { ...body };
      if (body.sabbathObservance) data.sabbathObservance = stringifyStringArray(body.sabbathObservance);
      if (body.ministryInterests) data.ministryInterests = stringifyStringArray(body.ministryInterests);

      await prisma.faithProfile.upsert({
        where: { userId },
        update: data,
        create: {
          userId,
          adventistAffiliation: body.adventistAffiliation || 'Seventh-day Adventist Member',
          yearsAsAdventist: body.yearsAsAdventist ?? 0,
          localChurch: body.localChurch || '',
          isBaptized: body.isBaptized ?? true,
          faithImportance: body.faithImportance || 'Very important',
          churchInvolvement: body.churchInvolvement || 'Active',
          sabbathObservance: stringifyStringArray(body.sabbathObservance),
          ministryInterests: stringifyStringArray(body.ministryInterests),
          personalBibleStudy: body.personalBibleStudy || 'Daily',
          favoriteVerse: body.favoriteVerse,
        },
      });

      const user = await loadUser(userId);
      return ok(res, toProfileDto(user!));
    } catch (error: any) {
      return fail(res, 'VALIDATION_ERROR', 'Invalid faith profile payload', 400, { details: error.errors });
    }
  }
}
