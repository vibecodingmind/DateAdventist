import { Response } from 'express';
import { z } from 'zod';
import { AuthenticatedRequest } from '../middleware/auth.middleware';

const profilesStore = new Map<string, any>();

const updateProfileSchema = z.object({
  fullName: z.string().min(2).optional(),
  bio: z.string().max(1000).optional(),
  country: z.string().optional(),
  city: z.string().optional(),
  occupation: z.string().optional(),
  education: z.string().optional(),
  relationshipIntention: z.string().optional(),
  primaryPhoto: z.string().url().optional(),
  photoUrls: z.array(z.string().url()).optional(),
  diet: z.string().optional(),
  alcohol: z.string().optional(),
  smoking: z.string().optional(),
  wantsChildren: z.string().optional(),
  hasChildren: z.boolean().optional(),
  interests: z.array(z.string()).optional()
});

const faithProfileSchema = z.object({
  adventistAffiliation: z.string().min(2),
  yearsAsAdventist: z.number().int().min(0),
  localChurch: z.string().min(2),
  isBaptized: z.boolean(),
  faithImportance: z.string(),
  churchInvolvement: z.string(),
  sabbathObservance: z.array(z.string()),
  ministryInterests: z.array(z.string()),
  personalBibleStudy: z.string(),
  favoriteVerse: z.string().optional()
});

export class ProfileController {
  static async getProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) {
      return res.status(401).json({ success: false, error: { code: 'UNAUTHORIZED', message: 'Authentication required' } });
    }

    const profile = profilesStore.get(userId) || {
      userId,
      fullName: 'AdventHearts Member',
      bio: 'Sabbath keeper looking for a Christ-centered relationship.',
      country: 'United States',
      city: 'Berrien Springs',
      occupation: 'Healthcare Professional',
      education: 'Bachelor Degree',
      relationshipIntention: 'Marriage',
      primaryPhoto: 'https://picsum.photos/400/600',
      photoUrls: ['https://picsum.photos/400/600'],
      isVerified: true,
      isPremium: false,
      diet: 'Vegetarian',
      alcohol: 'Never',
      smoking: 'Never',
      wantsChildren: 'Yes',
      hasChildren: false,
      interests: ['Pathfinders', 'Bible Study', 'Hiking', 'Cooking']
    };

    return res.status(200).json({
      success: true,
      data: profile
    });
  }

  static async updateProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) {
      return res.status(401).json({ success: false, error: { code: 'UNAUTHORIZED', message: 'Authentication required' } });
    }

    try {
      const validatedData = updateProfileSchema.parse(req.body);
      const existing = profilesStore.get(userId) || {};
      const updated = {
        ...existing,
        ...validatedData,
        userId,
        updatedAt: new Date().toISOString()
      };
      profilesStore.set(userId, updated);

      return res.status(200).json({
        success: true,
        data: updated
      });
    } catch (error: any) {
      return res.status(400).json({
        success: false,
        error: { code: 'VALIDATION_ERROR', message: 'Invalid profile payload', details: error.errors }
      });
    }
  }

  static async getFaithProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) {
      return res.status(401).json({ success: false, error: { code: 'UNAUTHORIZED', message: 'Authentication required' } });
    }

    return res.status(200).json({
      success: true,
      data: {
        userId,
        adventistAffiliation: 'Seventh-day Adventist (Born & Raised)',
        yearsAsAdventist: 25,
        localChurch: 'Pioneer Memorial Church',
        isBaptized: true,
        faithImportance: 'Core Priority',
        churchInvolvement: 'Active Leader (Pathfinders / Sabbath School)',
        sabbathObservance: ['Sunset to Sunset', 'No Media/Work', 'Fellowship & Nature'],
        ministryInterests: ['Youth Ministry', 'Community Services', 'Music Ministry'],
        personalBibleStudy: 'Daily',
        favoriteVerse: 'Jeremiah 29:11'
      }
    });
  }

  static async updateFaithProfile(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) {
      return res.status(401).json({ success: false, error: { code: 'UNAUTHORIZED', message: 'Authentication required' } });
    }

    try {
      const validatedData = faithProfileSchema.parse(req.body);
      return res.status(200).json({
        success: true,
        data: {
          userId,
          ...validatedData,
          updatedAt: new Date().toISOString()
        }
      });
    } catch (error: any) {
      return res.status(400).json({
        success: false,
        error: { code: 'VALIDATION_ERROR', message: 'Invalid faith profile payload', details: error.errors }
      });
    }
  }
}
