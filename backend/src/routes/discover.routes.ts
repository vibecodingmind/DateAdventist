import { Router, Response } from 'express';
import { requireAuth, AuthenticatedRequest } from '../middleware/auth.middleware';
import { requireSubscription } from '../middleware/subscription.middleware';

const router = Router();

// Standard discovery endpoints (available to all authenticated users)
router.get('/', requireAuth, (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: [
      { id: 'usr_1', fullName: 'Sarah Jenkins', age: 26, isPremium: true, tier: 'GOLD' },
      { id: 'usr_2', fullName: 'David Miller', age: 29, isPremium: false, tier: 'FREE' }
    ]
  });
});

// PREMIUM-ONLY ENDPOINT 1: Advanced Faith & Lifestyle Search
router.post('/advanced-search', requireAuth, requireSubscription('PLUS'), (req: AuthenticatedRequest, res: Response) => {
  const { SabbathObservance, dietaryPreference, churchAttendance } = req.body;
  res.json({
    success: true,
    data: {
      appliedFilters: { SabbathObservance, dietaryPreference, churchAttendance },
      matchesCount: 14,
      message: 'Advanced filters successfully applied via Gold/Platinum subscription verification.'
    }
  });
});

// PREMIUM-ONLY ENDPOINT 2: Super Like
router.post('/:userId/super-like', requireAuth, requireSubscription('GOLD'), (req: AuthenticatedRequest, res: Response) => {
  const targetUserId = req.params.userId;
  res.json({
    success: true,
    data: {
      targetUserId,
      isSuperLiked: true,
      isMatch: true,
      matchId: `match_${Date.now()}`,
      message: 'Super Like successfully sent! Server validated active Gold/Platinum subscription.'
    }
  });
});

export default router;
