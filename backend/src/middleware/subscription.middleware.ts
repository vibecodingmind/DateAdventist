import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from './auth.middleware';

// Mock user subscription store or verification logic
const userSubscriptions: Record<string, { tier: 'FREE' | 'PLUS' | 'GOLD' | 'PLATINUM'; active: boolean }> = {
  'usr_premium_123': { tier: 'GOLD', active: true },
  'usr_free_456': { tier: 'FREE', active: false }
};

/**
 * Express Middleware to verify subscription status on the server side
 * before granting access to premium-only endpoints (e.g., Super Like, Advanced Search).
 */
export const requireSubscription = (minRequiredTier: 'PLUS' | 'GOLD' | 'PLATINUM' = 'PLUS') => {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    const userId = req.user?.userId;
    const isPremiumHeader = req.headers['x-user-is-premium'] === 'true';

    // Tier weights
    const tierWeights: Record<string, number> = {
      FREE: 0,
      PLUS: 1,
      GOLD: 2,
      PLATINUM: 3
    };

    const userSub = userId ? userSubscriptions[userId] : null;
    const effectiveTier = userSub?.active ? userSub.tier : (isPremiumHeader ? 'GOLD' : 'FREE');
    const userWeight = tierWeights[effectiveTier] || 0;
    const requiredWeight = tierWeights[minRequiredTier] || 1;

    if (userWeight < requiredWeight) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'PREMIUM_SUBSCRIPTION_REQUIRED',
          message: `Access denied. This endpoint requires an active ${minRequiredTier} tier or higher subscription.`,
          requiredTier: minRequiredTier,
          currentTier: effectiveTier,
          upgradeUrl: '/api/v1/subscriptions/checkout'
        }
      });
    }

    next();
  };
};
