import { Router } from 'express';
import { requireAuth } from '../middleware/auth.middleware';
import { SafetyController, NotificationController } from '../controllers/safety.controller';

export const safetyRouter = Router();
safetyRouter.post('/report', requireAuth, SafetyController.report);
safetyRouter.post('/block', requireAuth, SafetyController.block);
safetyRouter.get('/blocks', requireAuth, SafetyController.listBlocks);
safetyRouter.post('/verification', requireAuth, SafetyController.submitVerification);

export const notificationRouter = Router();
notificationRouter.get('/', requireAuth, NotificationController.list);
notificationRouter.post('/read', requireAuth, NotificationController.markRead);
