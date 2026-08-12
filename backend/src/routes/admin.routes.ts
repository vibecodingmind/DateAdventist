import { Router } from 'express';
import { requireAuth } from '../middleware/auth.middleware';
import { requireRole } from '../middleware/rbac.middleware';
import { AdminController } from '../controllers/admin.controller';

const router = Router();

router.post('/login', AdminController.login);
router.get('/dashboard', requireAuth, requireRole('MODERATOR'), AdminController.dashboard);
router.get('/users', requireAuth, requireRole('ADMIN'), AdminController.users);
router.post('/users/:userId/status', requireAuth, requireRole('ADMIN'), AdminController.updateUserStatus);
router.get('/verifications', requireAuth, requireRole('MODERATOR'), AdminController.verifications);
router.post('/verifications/:userId/approve', requireAuth, requireRole('MODERATOR'), AdminController.approveVerification);
router.post('/verifications/:userId/reject', requireAuth, requireRole('MODERATOR'), AdminController.rejectVerification);
router.get('/reports', requireAuth, requireRole('MODERATOR'), AdminController.reports);
router.post('/reports/:reportId', requireAuth, requireRole('MODERATOR'), AdminController.updateReport);
router.get('/analytics', requireAuth, requireRole('ADMIN'), AdminController.analytics);
router.get('/settings', requireAuth, requireRole('SUPER_ADMIN'), AdminController.settings);
router.put('/settings', requireAuth, requireRole('SUPER_ADMIN'), AdminController.updateSettings);
router.get('/audit-logs', requireAuth, requireRole('SUPER_ADMIN'), AdminController.auditLogs);

export default router;
