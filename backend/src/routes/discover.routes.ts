import { Router } from 'express';
import { requireAuth } from '../middleware/auth.middleware';
import { requireSubscription } from '../middleware/subscription.middleware';
import { DiscoverController } from '../controllers/discover.controller';

const router = Router();

router.get('/', requireAuth, DiscoverController.list);
router.post('/advanced-search', requireAuth, requireSubscription('PLUS'), DiscoverController.advancedSearch);
router.post('/like', requireAuth, DiscoverController.like);
router.post('/:userId/like', requireAuth, DiscoverController.like);
router.post('/:userId/super-like', requireAuth, requireSubscription('GOLD'), DiscoverController.superLike);
router.post('/:userId/pass', requireAuth, DiscoverController.pass);
router.post('/pass', requireAuth, DiscoverController.pass);

export default router;
