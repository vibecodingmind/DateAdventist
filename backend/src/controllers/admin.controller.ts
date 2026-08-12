import { Request, Response } from 'express';
import { z } from 'zod';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { AuthService } from '../auth/auth.service';
import { toAuthPayload, toProfileDto } from '../utils/mappers';

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(1),
});

async function writeAudit(actorId: string, action: string, target: string, details?: string, ip?: string) {
  await prisma.auditLog.create({ data: { actorId, action, target, details, ipAddress: ip } });
}

export class AdminController {
  static async login(req: Request, res: Response) {
    try {
      const { email, password } = loginSchema.parse(req.body);
      const user = await prisma.user.findUnique({
        where: { email: email.toLowerCase() },
        include: { profile: true },
      });
      if (!user) return fail(res, 'INVALID_CREDENTIALS', 'Invalid admin credentials.', 401);

      const valid = await AuthService.verifyPassword(user.passwordHash, password);
      if (!valid) return fail(res, 'INVALID_CREDENTIALS', 'Invalid admin credentials.', 401);

      if (!['MODERATOR', 'ADMIN', 'SUPER_ADMIN'].includes(user.role)) {
        return fail(res, 'FORBIDDEN_ROLE_ACCESS', 'This account does not have administrative access.', 403);
      }

      const tokens = AuthService.generateTokens({ userId: user.id, email: user.email, role: user.role });
      await writeAudit(user.id, 'ADMIN_LOGIN', user.id, 'Administrator signed in');
      return ok(res, toAuthPayload(user, user.profile?.fullName, tokens));
    } catch (err: any) {
      return fail(res, 'VALIDATION_ERROR', err.message || 'Invalid login payload.');
    }
  }

  static async dashboard(_req: AuthenticatedRequest, res: Response) {
    const [totalUsers, pendingVerifications, openReports, matches, activeSubs] = await Promise.all([
      prisma.user.count({ where: { status: { not: 'DELETED' } } }),
      prisma.profile.count({ where: { verificationStatus: 'PENDING' } }),
      prisma.report.count({ where: { status: 'OPEN' } }),
      prisma.match.count(),
      prisma.subscription.count({ where: { status: 'ACTIVE' } }),
    ]);

    return ok(res, {
      metrics: {
        totalUsers,
        activeToday: totalUsers,
        pendingVerifications,
        openReports,
        totalMatches: matches,
        activeSubscriptions: activeSubs,
        totalRevenueUsd: activeSubs * 14.99,
      },
      activeUsers: totalUsers,
      totalMatches: matches,
      pendingVerifications,
      openReports,
      monthlyRevenue: activeSubs * 14.99,
      systemHealth: 'OPERATIONAL',
    });
  }

  static async users(_req: AuthenticatedRequest, res: Response) {
    const users = await prisma.user.findMany({
      include: { profile: true, faithProfile: true, subscription: true },
      orderBy: { createdAt: 'desc' },
    });

    return ok(
      res,
      users.map((user) => ({
        userId: user.id,
        id: user.id,
        fullName: user.profile?.fullName ?? '',
        email: user.email,
        role: user.role,
        isVerified: user.profile?.isVerified ?? false,
        isPremium: user.profile?.isPremium ?? false,
        subscriptionStatus: user.subscription?.status === 'ACTIVE' ? user.subscription.plan : 'FREE',
        accountStatus: user.status,
        createdAt: user.createdAt.toISOString(),
        profile: toProfileDto(user),
      }))
    );
  }

  static async updateUserStatus(req: AuthenticatedRequest, res: Response) {
    const actorId = req.user?.userId;
    if (!actorId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const status = String(req.body.status || '').toUpperCase();
    if (!['ACTIVE', 'PAUSED', 'SUSPENDED', 'BANNED'].includes(status)) {
      return fail(res, 'VALIDATION_ERROR', 'status must be ACTIVE, PAUSED, SUSPENDED, or BANNED.');
    }

    const user = await prisma.user.update({
      where: { id: req.params.userId },
      data: { status: status as any },
    });

    await writeAudit(actorId, `USER_${status}`, user.id, req.body.reason);
    return ok(res, { userId: user.id, status: user.status });
  }

  static async verifications(_req: AuthenticatedRequest, res: Response) {
    const pending = await prisma.profile.findMany({
      where: { verificationStatus: 'PENDING' },
      include: { user: true },
    });
    return ok(
      res,
      pending.map((p) => ({
        id: p.userId,
        userId: p.userId,
        fullName: p.fullName,
        selfieUrl: p.verificationSelfie,
        status: p.verificationStatus,
      }))
    );
  }

  static async approveVerification(req: AuthenticatedRequest, res: Response) {
    const actorId = req.user?.userId;
    if (!actorId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    await prisma.profile.update({
      where: { userId: req.params.userId },
      data: { verificationStatus: 'VERIFIED', isVerified: true },
    });
    await writeAudit(actorId, 'VERIFY_APPROVE', req.params.userId, 'Approved selfie verification');
    return ok(res, { userId: req.params.userId, verificationStatus: 'VERIFIED' });
  }

  static async rejectVerification(req: AuthenticatedRequest, res: Response) {
    const actorId = req.user?.userId;
    if (!actorId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    await prisma.profile.update({
      where: { userId: req.params.userId },
      data: { verificationStatus: 'REJECTED', isVerified: false },
    });
    await writeAudit(actorId, 'VERIFY_REJECT', req.params.userId, req.body.reason);
    return ok(res, { userId: req.params.userId, verificationStatus: 'REJECTED' });
  }

  static async reports(_req: AuthenticatedRequest, res: Response) {
    const reports = await prisma.report.findMany({
      include: { reporter: { include: { profile: true } }, reportedUser: { include: { profile: true } } },
      orderBy: { createdAt: 'desc' },
    });
    return ok(
      res,
      reports.map((r) => ({
        id: r.id,
        reportId: r.id,
        reporterId: r.reporterId,
        reportedUserId: r.reportedUserId,
        reason: r.reason,
        details: r.details,
        status: r.status,
        timestamp: r.createdAt.getTime(),
        createdAt: r.createdAt.toISOString(),
      }))
    );
  }

  static async updateReport(req: AuthenticatedRequest, res: Response) {
    const actorId = req.user?.userId;
    if (!actorId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const status = String(req.body.status || 'RESOLVED');
    const report = await prisma.report.update({
      where: { id: req.params.reportId },
      data: { status },
    });
    await writeAudit(actorId, 'REPORT_UPDATE', report.id, status);
    return ok(res, { reportId: report.id, status: report.status });
  }

  static async analytics(_req: AuthenticatedRequest, res: Response) {
    const [totalUsers, matchesMade, pendingVerifications, totalReports, activeSubscriptions] = await Promise.all([
      prisma.user.count(),
      prisma.match.count(),
      prisma.profile.count({ where: { verificationStatus: 'PENDING' } }),
      prisma.report.count(),
      prisma.subscription.count({ where: { status: 'ACTIVE' } }),
    ]);
    const verified = await prisma.profile.count({ where: { isVerified: true } });
    const verificationRatePercent = totalUsers === 0 ? 0 : Math.round((verified / totalUsers) * 100);

    return ok(res, {
      totalUsers,
      dailyActiveUsers: totalUsers,
      monthlyActiveUsers: totalUsers,
      matchesMade,
      verificationRatePercent,
      mrrUsd: activeSubscriptions * 14.99,
      subscriptionConversionRatePct: totalUsers === 0 ? 0 : Number(((activeSubscriptions / totalUsers) * 100).toFixed(1)),
      activeSubscriptions,
      totalReports,
      pendingVerifications,
      totalRevenueUsd: activeSubscriptions * 14.99,
    });
  }

  static async settings(_req: AuthenticatedRequest, res: Response) {
    const rows = await prisma.systemSettings.findMany();
    const map = Object.fromEntries(rows.map((r) => [r.key, r.value]));
    return ok(res, {
      provider: map.provider ?? 'Stripe',
      isSandboxMode: (map.isSandboxMode ?? 'true') === 'true',
      monthlyPriceUsd: map.monthlyPriceUsd ?? '14.99',
      annualPriceUsd: map.annualPriceUsd ?? '99.99',
      autoApproveVerifications: (map.autoApproveVerifications ?? 'false') === 'true',
      requireSelfieVerification: (map.requireSelfieVerification ?? 'true') === 'true',
      maintenanceMode: (map.maintenanceMode ?? 'false') === 'true',
    });
  }

  static async updateSettings(req: AuthenticatedRequest, res: Response) {
    const actorId = req.user?.userId;
    if (!actorId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const entries = Object.entries(req.body || {});
    for (const [key, value] of entries) {
      await prisma.systemSettings.upsert({
        where: { key },
        update: { value: String(value) },
        create: { key, value: String(value) },
      });
    }
    await writeAudit(actorId, 'SETTINGS_UPDATE', 'system', JSON.stringify(req.body));
    return AdminController.settings(req, res);
  }

  static async auditLogs(_req: AuthenticatedRequest, res: Response) {
    const logs = await prisma.auditLog.findMany({
      include: { actor: { include: { profile: true } } },
      orderBy: { createdAt: 'desc' },
      take: 100,
    });
    return ok(
      res,
      logs.map((log) => ({
        id: log.id,
        timestamp: log.createdAt.getTime(),
        actor: log.actor.email,
        actionType: log.action,
        details: log.details ?? '',
        targetUserId: log.target,
        createdAt: log.createdAt.toISOString(),
      }))
    );
  }
}
