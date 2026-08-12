import { Router } from 'express';
import { requireAuth } from '../middleware/auth.middleware';
import { SubscriptionController } from '../controllers/subscription.controller';

const router = Router();

router.get('/plans', SubscriptionController.plans);
router.get('/current', requireAuth, SubscriptionController.current);
router.post('/checkout', requireAuth, SubscriptionController.checkout);
router.post('/confirm-payment', requireAuth, SubscriptionController.confirmPayment);

export default router;
