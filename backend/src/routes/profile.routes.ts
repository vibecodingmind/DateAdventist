import { Router } from 'express';
import { requireAuth } from '../middleware/auth.middleware';
import { ProfileController } from '../profile/profile.controller';

const router = Router();

router.get('/', requireAuth, ProfileController.getProfile);
router.put('/', requireAuth, ProfileController.updateProfile);
router.get('/faith', requireAuth, ProfileController.getFaithProfile);
router.put('/faith', requireAuth, ProfileController.updateFaithProfile);

export default router;
