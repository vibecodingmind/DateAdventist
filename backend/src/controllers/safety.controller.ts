import { Response } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';

export class SafetyController {
  static async report(req: AuthenticatedRequest, res: Response) {
    const reporterId = req.user?.userId;
    if (!reporterId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const reportedUserId = String(req.body.reportedUserId || '');
    const reason = String(req.body.reason || '').trim();
    const details = String(req.body.details || '');
    if (!reportedUserId || !reason) {
      return fail(res, 'VALIDATION_ERROR', 'reportedUserId and reason are required.');
    }

    const report = await prisma.report.create({
      data: { reporterId, reportedUserId, reason, details },
    });

    return ok(res, {
      reportId: report.id,
      reporterId,
      reportedUserId,
      reason,
      details,
      status: report.status,
      timestamp: report.createdAt.getTime(),
    }, 201);
  }

  static async block(req: AuthenticatedRequest, res: Response) {
    const blockerId = req.user?.userId;
    if (!blockerId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const blockedUserId = String(req.body.blockedUserId || req.params.userId || '');
    if (!blockedUserId) return fail(res, 'VALIDATION_ERROR', 'blockedUserId is required.');

    await prisma.block.upsert({
      where: { blockerId_blockedUserId: { blockerId, blockedUserId } },
      update: {},
      create: { blockerId, blockedUserId },
    });

    return ok(res, { blocked: true, blockedUserId });
  }

  static async listBlocks(req: AuthenticatedRequest, res: Response) {
    const blockerId = req.user?.userId;
    if (!blockerId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const blocks = await prisma.block.findMany({ where: { blockerId } });
    return ok(res, blocks.map((b) => b.blockedUserId));
  }

  static async submitVerification(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const selfie = String(req.body.verificationSelfie || req.body.selfieUri || '');
    if (!selfie) return fail(res, 'VALIDATION_ERROR', 'verificationSelfie is required.');

    await prisma.profile.update({
      where: { userId },
      data: { verificationSelfie: selfie, verificationStatus: 'PENDING' },
    });

    return ok(res, { verificationStatus: 'PENDING' });
  }
}

export class NotificationController {
  static async list(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const notifications = await prisma.notification.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
    });
    return ok(
      res,
      notifications.map((n) => ({
        id: n.id,
        userId: n.userId,
        title: n.title,
        body: n.body,
        type: n.type,
        isRead: n.isRead,
        timestamp: n.createdAt.getTime(),
        createdAt: n.createdAt.toISOString(),
      }))
    );
  }

  static async markRead(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    await prisma.notification.updateMany({ where: { userId }, data: { isRead: true } });
    return ok(res, { marked: true });
  }
}
