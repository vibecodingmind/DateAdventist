import { Router } from 'express';
import { requireAuth } from '../middleware/auth.middleware';
import { MatchController } from '../controllers/match.controller';
import { DiscoverController } from '../controllers/discover.controller';

const router = Router();

router.get('/', requireAuth, MatchController.list);
router.get('/:matchId', requireAuth, MatchController.getById);
router.get('/:matchId/messages', requireAuth, MatchController.listMessages);
router.post('/:matchId/messages', requireAuth, MatchController.sendMessage);
router.post('/:matchId/read', requireAuth, MatchController.markRead);

export const likesRouter = Router();
likesRouter.get('/', requireAuth, DiscoverController.likesReceived);

export default router;
