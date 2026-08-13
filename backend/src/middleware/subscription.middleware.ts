import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from './auth.middleware';
import { prisma } from '../db/prisma';
import { fail } from '../utils/http';

const TIER_WEIGHTS: Record<string, number> = {
  FREE: 0,
  PLUS: 1,
  GOLD: 2,
  PLATINUM: 3,
};

export async function getUserTier(userId: string): Promise<string> {
  const [subscription, profile] = await Promise.all([
    prisma.subscription.findUnique({ where: { userId } }),
    prisma.profile.findUnique({ where: { userId } }),
  ]);

  if (subscription && subscription.status === 'ACTIVE' && subscription.currentPeriodEnd > new Date()) {
    const plan = subscription.plan.toUpperCase();
    if (plan.includes('PLATINUM')) return 'PLATINUM';
    if (plan.includes('GOLD')) return 'GOLD';
    if (plan.includes('PLUS')) return 'PLUS';
    return 'GOLD';
  }

  if (profile?.isPremium) return 'GOLD';
  return 'FREE';
}

export const requireSubscription = (minRequiredTier: 'PLUS' | 'GOLD' | 'PLATINUM' = 'PLUS') => {
  return async (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    const userId = req.user?.userId;
    if (!userId) {
      return fail(res, 'UNAUTHORIZED', 'Authentication required.', 401);
    }

    const effectiveTier = await getUserTier(userId);
    const userWeight = TIER_WEIGHTS[effectiveTier] || 0;
    const requiredWeight = TIER_WEIGHTS[minRequiredTier] || 1;

    if (userWeight < requiredWeight) {
      return fail(
        res,
        'PREMIUM_SUBSCRIPTION_REQUIRED',
        `Access denied. This endpoint requires an active ${minRequiredTier} tier or higher subscription.`,
        403,
        { requiredTier: minRequiredTier, currentTier: effectiveTier, upgradeUrl: '/api/v1/subscriptions/checkout' }
      );
    }

    next();
  };
};
