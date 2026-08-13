import { Response } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { ageFromDob } from '../utils/json';
import { toProfileDto } from '../utils/mappers';
import { CompatibilityService } from '../services/compatibility.service';
import { MatchingService } from '../services/matching.service';
import { getUserTier } from '../middleware/subscription.middleware';

export class DiscoverController {
  static async list(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const me = await prisma.user.findUnique({
      where: { id: userId },
      include: { profile: true, faithProfile: true, preference: true },
    });
    if (!me?.profile) return fail(res, 'PROFILE_REQUIRED', 'Complete your profile before discovering members.', 400);

    const [likes, passes, blocks] = await Promise.all([
      prisma.like.findMany({ where: { fromUserId: userId }, select: { toUserId: true } }),
      prisma.pass.findMany({ where: { fromUserId: userId }, select: { toUserId: true } }),
      prisma.block.findMany({
        where: { OR: [{ blockerId: userId }, { blockedUserId: userId }] },
      }),
    ]);

    const excluded = new Set<string>([
      userId,
      ...likes.map((l) => l.toUserId),
      ...passes.map((p) => p.toUserId),
      ...blocks.map((b) => (b.blockerId === userId ? b.blockedUserId : b.blockerId)),
    ]);

    const minAge = Number(req.query.minAge ?? me.preference?.minAge ?? 18);
    const maxAge = Number(req.query.maxAge ?? me.preference?.maxAge ?? 99);
    const gender = String(req.query.gender ?? me.preference?.preferredGender ?? 'Any');
    const verifiedOnly = String(req.query.verifiedOnly ?? 'false') === 'true';
    const search = String(req.query.q ?? '').toLowerCase();

    const candidates = await prisma.user.findMany({
      where: {
        id: { notIn: Array.from(excluded) },
        status: 'ACTIVE',
        profile: { isPaused: false },
      },
      include: { profile: true, faithProfile: true, subscription: true },
    });

    const filtered = candidates
      .map((user) => {
        if (!user.profile) return null;
        const age = ageFromDob(user.profile.dateOfBirth);
        if (age < minAge || age > maxAge) return null;
        if (gender !== 'Any' && gender !== 'All' && user.profile.gender !== gender) return null;
        if (verifiedOnly && !user.profile.isVerified) return null;
        if (search) {
          const haystack = `${user.profile.fullName} ${user.profile.city} ${user.profile.bio} ${user.faithProfile?.localChurch ?? ''}`.toLowerCase();
          if (!haystack.includes(search)) return null;
        }
        const compatibilityScore = CompatibilityService.score(me, user);
        return toProfileDto(user, { compatibilityScore });
      })
      .filter((item): item is NonNullable<typeof item> => item !== null)
      .sort((a, b) => (b.compatibilityScore ?? 0) - (a.compatibilityScore ?? 0));

    return ok(res, filtered);
  }

  static async advancedSearch(req: AuthenticatedRequest, res: Response) {
    req.query = { ...req.query, ...(req.body || {}) };
    return DiscoverController.list(req, res);
  }

  static async like(req: AuthenticatedRequest, res: Response) {
    const fromUserId = req.user?.userId;
    if (!fromUserId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const toUserId = (req.params.userId || req.body.toUserId || req.body.targetUserId) as string;
    const isSuperLike = Boolean(req.body.isSuperLike);
    if (!toUserId) return fail(res, 'VALIDATION_ERROR', 'targetUserId is required.');

    try {
      const result = await MatchingService.like(fromUserId, toUserId, isSuperLike);
      return ok(res, result);
    } catch (err: any) {
      return fail(res, err.code || 'LIKE_FAILED', err.message, err.status || 400);
    }
  }

  static async superLike(req: AuthenticatedRequest, res: Response) {
    req.body = { ...req.body, isSuperLike: true };
    return DiscoverController.like(req, res);
  }

  static async pass(req: AuthenticatedRequest, res: Response) {
    const fromUserId = req.user?.userId;
    if (!fromUserId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const toUserId = (req.params.userId || req.body.toUserId) as string;
    if (!toUserId) return fail(res, 'VALIDATION_ERROR', 'targetUserId is required.');
    const result = await MatchingService.pass(fromUserId, toUserId);
    return ok(res, result);
  }

  static async likesReceived(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const tier = await getUserTier(userId);
    const likes = await prisma.like.findMany({
      where: { toUserId: userId },
      include: { fromUser: { include: { profile: true, faithProfile: true, subscription: true } } },
      orderBy: { createdAt: 'desc' },
    });

    const me = await prisma.user.findUnique({
      where: { id: userId },
      include: { profile: true, faithProfile: true },
    });

    const data = likes.map((like) => ({
      likeId: like.id,
      fromUserId: like.fromUserId,
      toUserId: like.toUserId,
      isSuperLike: like.isSuperLike,
      createdAt: like.createdAt.toISOString(),
      profile: tier === 'FREE' ? { userId: like.fromUserId, isBlurred: true } : toProfileDto(like.fromUser, {
        compatibilityScore: CompatibilityService.score(me ?? {}, like.fromUser),
      }),
    }));

    return ok(res, data);
  }
}
