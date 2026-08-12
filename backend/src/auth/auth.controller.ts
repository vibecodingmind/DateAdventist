import { Request, Response } from 'express';
import { z } from 'zod';
import { AuthService } from './auth.service';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { dobFromAge, stringifyStringArray } from '../utils/json';
import { toAuthPayload, toProfileDto } from '../utils/mappers';
import { AuthenticatedRequest } from '../middleware/auth.middleware';

const registerSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
  fullName: z.string().min(2),
  dateOfBirth: z.string().optional(),
  age: z.number().int().min(18).optional(),
  gender: z.string().optional(),
  country: z.string().optional(),
  city: z.string().optional(),
  adventistAffiliation: z.string().optional(),
  relationshipIntention: z.string().optional(),
});

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(1),
});

export class AuthController {
  static async register(req: Request, res: Response) {
    try {
      const body = registerSchema.parse(req.body);
      const existing = await prisma.user.findUnique({ where: { email: body.email.toLowerCase() } });
      if (existing) {
        return fail(res, 'EMAIL_EXISTS', 'User with this email already exists.', 409);
      }

      const passwordHash = await AuthService.hashPassword(body.password);
      const dateOfBirth = body.dateOfBirth ? new Date(body.dateOfBirth) : dobFromAge(body.age ?? 25);
      const userId = `usr_${Date.now()}`;

      const user = await prisma.user.create({
        data: {
          id: userId,
          email: body.email.toLowerCase(),
          passwordHash,
          role: 'USER',
          isEmailVerified: false,
          profile: {
            create: {
              fullName: body.fullName,
              dateOfBirth,
              gender: body.gender || 'Unspecified',
              country: body.country || '',
              city: body.city || '',
              relationshipIntention: body.relationshipIntention || 'Marriage',
              bio: 'Faithful Adventist looking for a Christian partner to share Sabbath, life, and ministry.',
              photoUrls: stringifyStringArray([]),
              interests: stringifyStringArray(['Sabbath Nature Walks', 'Bible Study']),
            },
          },
          faithProfile: {
            create: {
              adventistAffiliation: body.adventistAffiliation || 'Seventh-day Adventist Member',
              yearsAsAdventist: Math.max(0, (body.age ?? 25) - 5),
              localChurch: 'Local SDA Church',
              sabbathObservance: stringifyStringArray(['Church Service', 'Sunset to Sunset Rest']),
              ministryInterests: stringifyStringArray(['Youth', 'Bible Study']),
              favoriteVerse: 'John 3:16',
            },
          },
          preference: { create: {} },
        },
      });

      const verificationToken = AuthService.generateEmailVerificationToken(user.id);
      await prisma.user.update({
        where: { id: user.id },
        data: { verificationToken },
      });

      const tokens = AuthService.generateTokens({ userId: user.id, email: user.email, role: user.role });
      return ok(res, { ...toAuthPayload(user, body.fullName, tokens), verificationToken }, 201);
    } catch (err: any) {
      if (err.name === 'ZodError') {
        return fail(res, 'VALIDATION_ERROR', err.errors?.[0]?.message || 'Invalid input data.');
      }
      return fail(res, 'VALIDATION_ERROR', err.message || 'Invalid input data.');
    }
  }

  static async login(req: Request, res: Response) {
    try {
      const { email, password } = loginSchema.parse(req.body);
      const user = await prisma.user.findUnique({
        where: { email: email.toLowerCase() },
        include: { profile: true },
      });

      if (!user) {
        return fail(res, 'INVALID_CREDENTIALS', 'Invalid email or password.', 401);
      }

      if (user.status === 'BANNED' || user.status === 'SUSPENDED' || user.status === 'DELETED') {
        return fail(res, 'ACCOUNT_INACTIVE', 'This account is not allowed to sign in.', 403);
      }

      const isValidPassword = await AuthService.verifyPassword(user.passwordHash, password);
      if (!isValidPassword) {
        return fail(res, 'INVALID_CREDENTIALS', 'Invalid email or password.', 401);
      }

      await prisma.user.update({ where: { id: user.id }, data: { lastActiveAt: new Date() } });
      const tokens = AuthService.generateTokens({ userId: user.id, email: user.email, role: user.role });
      return ok(res, toAuthPayload(user, user.profile?.fullName, tokens));
    } catch (err: any) {
      return fail(res, 'VALIDATION_ERROR', err.message || 'Invalid login payload.');
    }
  }

  static async verifyEmail(req: Request, res: Response) {
    const { token } = req.body;
    if (!token) {
      return fail(res, 'MISSING_TOKEN', 'Verification token required.');
    }

    const payload = AuthService.verifyEmailToken(token);
    if (!payload) {
      return fail(res, 'INVALID_TOKEN', 'Token is invalid or expired.');
    }

    await prisma.user.update({
      where: { id: payload.userId },
      data: { isEmailVerified: true, verificationToken: null },
    });

    return ok(res, { message: 'Email verified successfully.' });
  }

  static async refreshToken(req: Request, res: Response) {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      return fail(res, 'MISSING_TOKEN', 'Refresh token required.');
    }

    const payload = AuthService.verifyRefreshToken(refreshToken);
    if (!payload) {
      return fail(res, 'INVALID_REFRESH_TOKEN', 'Refresh token is expired or invalid.', 401);
    }

    const user = await prisma.user.findUnique({ where: { id: payload.userId } });
    if (!user) {
      return fail(res, 'INVALID_REFRESH_TOKEN', 'Refresh token is expired or invalid.', 401);
    }

    const tokens = AuthService.generateTokens({ userId: user.id, email: user.email, role: user.role });
    return ok(res, tokens);
  }

  static async logout(_req: Request, res: Response) {
    return ok(res, { message: 'Logged out successfully.' });
  }

  static async getMe(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) {
      return fail(res, 'UNAUTHORIZED', 'Authentication token required.', 401);
    }

    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: { profile: true, faithProfile: true, subscription: true },
    });

    if (!user) {
      return fail(res, 'NOT_FOUND', 'User not found.', 404);
    }

    return ok(res, {
      ...toAuthPayload(user, user.profile?.fullName),
      profile: toProfileDto(user),
    });
  }
}
