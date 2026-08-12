import { Router, Response } from 'express';
import { requireAuth, AuthenticatedRequest } from '../middleware/auth.middleware';
import { requireRole } from '../middleware/rbac.middleware';

const router = Router();

// POST /api/v1/admin/login - Admin authentication endpoint
router.post('/login', (req, res) => {
  const { email } = req.body;
  let role = 'ADMIN';
  if (email?.includes('super')) {
    role = 'SUPER_ADMIN';
  } else if (email?.includes('mod')) {
    role = 'MODERATOR';
  }

  res.json({
    success: true,
    data: {
      token: `jwt_admin_token_${Date.now()}`,
      user: {
        id: `usr_admin_${Date.now()}`,
        email: email || 'admin@adventhearts.com',
        fullName: 'AdventHearts Administrator',
        role: role
      }
    }
  });
});

// GET /api/v1/admin/dashboard - High level admin metrics (MODERATOR or above)
router.get('/dashboard', requireAuth, requireRole('MODERATOR'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: {
      metrics: {
        totalUsers: 1248,
        activeToday: 852,
        pendingVerifications: 2,
        openReports: 3,
        totalRevenueUsd: 14280.00
      },
      systemHealth: 'OPERATIONAL'
    }
  });
});

// GET /api/v1/admin/users - User management (ADMIN or above)
router.get('/users', requireAuth, requireRole('ADMIN'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: [
      { id: 'usr_me', fullName: 'Joshua Miller', email: 'john.adventist@gmail.com', role: 'USER', isVerified: true, isPremium: true },
      { id: 'usr_sarah', fullName: 'Sarah Moretz', email: 'sarah.m@gmail.com', role: 'USER', isVerified: true, isPremium: false },
      { id: 'usr_mod', fullName: 'System Moderator', email: 'moderator@adventhearts.com', role: 'MODERATOR', isVerified: true, isPremium: true },
      { id: 'usr_admin', fullName: 'AdventHearts Admin', email: 'admin@adventhearts.com', role: 'ADMIN', isVerified: true, isPremium: true },
      { id: 'usr_super', fullName: 'First Super Admin', email: 'superadmin@adventhearts.com', role: 'SUPER_ADMIN', isVerified: true, isPremium: true }
    ]
  });
});

// GET /api/v1/admin/verifications - Pending photo verification queue (MODERATOR or above)
router.get('/verifications', requireAuth, requireRole('MODERATOR'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: [
      { id: 'usr_elena', fullName: 'Elena Rostova', selfieUrl: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2', status: 'PENDING' },
      { id: 'usr_marcus', fullName: 'Marcus Brown', selfieUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e', status: 'PENDING' }
    ]
  });
});

// POST /api/v1/admin/verifications/:userId/approve - Approve verification (MODERATOR or above)
router.post('/verifications/:userId/approve', requireAuth, requireRole('MODERATOR'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: {
      userId: req.params.userId,
      verificationStatus: 'VERIFIED',
      message: 'Verification approved successfully.'
    }
  });
});

// GET /api/v1/admin/reports - Content & profile moderation queue (MODERATOR or above)
router.get('/reports', requireAuth, requireRole('MODERATOR'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: [
      { id: 'rep_1', reportedUserId: 'usr_fake', reason: 'Inappropriate bio language', status: 'OPEN', timestamp: new Date().toISOString() }
    ]
  });
});

// GET /api/v1/admin/analytics - Platform performance analytics (ADMIN or above)
router.get('/analytics', requireAuth, requireRole('ADMIN'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: {
      totalUsers: 1248,
      activeToday: 852,
      matchesMade: 412,
      verificationRatePercent: 84,
      mrrUsd: 4950.00
    }
  });
});

// GET /api/v1/admin/settings - System settings (SUPER_ADMIN required)
router.get('/settings', requireAuth, requireRole('SUPER_ADMIN'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: {
      provider: 'Stripe',
      isSandboxMode: true,
      monthlyPriceUsd: '14.99',
      annualPriceUsd: '99.99',
      autoApproveVerifications: false,
      requireSelfieVerification: true,
      maintenanceMode: false
    }
  });
});

// GET /api/v1/admin/audit-logs - Administrative audit logs (SUPER_ADMIN required)
router.get('/audit-logs', requireAuth, requireRole('SUPER_ADMIN'), (req: AuthenticatedRequest, res: Response) => {
  res.json({
    success: true,
    data: [
      { id: 'log_1', action: 'SUPER_ADMIN_CREATED', performedBy: 'superadmin@adventhearts.com', timestamp: new Date().toISOString() },
      { id: 'log_2', action: 'PAYMENT_GATEWAY_UPDATED', performedBy: 'admin@adventhearts.com', timestamp: new Date().toISOString() }
    ]
  });
});

export default router;
