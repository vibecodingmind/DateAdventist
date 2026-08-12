import { Response } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { toProfileDto } from '../utils/mappers';
import { emitToMatch, emitToUser } from '../realtime/socket';

function isParticipant(match: { user1Id: string; user2Id: string }, userId: string) {
  return match.user1Id === userId || match.user2Id === userId;
}

export class MatchController {
  static async list(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const matches = await prisma.match.findMany({
      where: { OR: [{ user1Id: userId }, { user2Id: userId }] },
      include: {
        user1: { include: { profile: true, faithProfile: true, subscription: true } },
        user2: { include: { profile: true, faithProfile: true, subscription: true } },
        messages: { orderBy: { createdAt: 'desc' }, take: 1 },
      },
      orderBy: { matchedAt: 'desc' },
    });

    const data = matches.map((match) => {
      const other = match.user1Id === userId ? match.user2 : match.user1;
      const last = match.messages[0];
      return {
        matchId: match.id,
        user1Id: match.user1Id,
        user2Id: match.user2Id,
        compatibilityScore: match.compatibilityScore,
        conversationStarter: match.conversationStarter,
        matchedAt: match.matchedAt.toISOString(),
        lastMessage: last
          ? { messageId: last.id, text: last.text, senderId: last.senderId, createdAt: last.createdAt.toISOString(), isRead: last.isRead }
          : null,
        otherProfile: toProfileDto(other),
      };
    });

    return ok(res, data);
  }

  static async getById(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const match = await prisma.match.findUnique({
      where: { id: req.params.matchId },
      include: {
        user1: { include: { profile: true, faithProfile: true, subscription: true } },
        user2: { include: { profile: true, faithProfile: true, subscription: true } },
      },
    });

    if (!match || !isParticipant(match, userId)) {
      return fail(res, 'NOT_FOUND', 'Match not found.', 404);
    }

    const other = match.user1Id === userId ? match.user2 : match.user1;
    return ok(res, {
      matchId: match.id,
      user1Id: match.user1Id,
      user2Id: match.user2Id,
      compatibilityScore: match.compatibilityScore,
      conversationStarter: match.conversationStarter,
      matchedAt: match.matchedAt.toISOString(),
      otherProfile: toProfileDto(other),
    });
  }

  static async listMessages(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const match = await prisma.match.findUnique({ where: { id: req.params.matchId } });
    if (!match || !isParticipant(match, userId)) {
      return fail(res, 'NOT_FOUND', 'Match not found.', 404);
    }

    const messages = await prisma.message.findMany({
      where: { matchId: match.id },
      orderBy: { createdAt: 'asc' },
    });

    return ok(
      res,
      messages.map((m) => ({
        messageId: m.id,
        matchId: m.matchId,
        senderId: m.senderId,
        receiverId: m.receiverId,
        text: m.text,
        timestamp: m.createdAt.getTime(),
        createdAt: m.createdAt.toISOString(),
        isRead: m.isRead,
      }))
    );
  }

  static async sendMessage(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const text = String(req.body.text || '').trim();
    if (!text) return fail(res, 'EMPTY_MESSAGE', 'Message body cannot be empty.');

    const match = await prisma.match.findUnique({ where: { id: req.params.matchId } });
    if (!match || !isParticipant(match, userId)) {
      return fail(res, 'NOT_FOUND', 'Match not found.', 404);
    }

    const receiverId = match.user1Id === userId ? match.user2Id : match.user1Id;
    const message = await prisma.message.create({
      data: {
        matchId: match.id,
        senderId: userId,
        receiverId,
        text,
      },
    });

    const payload = {
      messageId: message.id,
      matchId: message.matchId,
      senderId: message.senderId,
      receiverId: message.receiverId,
      text: message.text,
      timestamp: message.createdAt.getTime(),
      createdAt: message.createdAt.toISOString(),
      isRead: false,
    };

    emitToMatch(match.id, 'chat:message', payload);
    emitToUser(receiverId, 'chat:message', payload);
    return ok(res, payload, 201);
  }

  static async markRead(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    await prisma.message.updateMany({
      where: { matchId: req.params.matchId, receiverId: userId, isRead: false },
      data: { isRead: true },
    });

    emitToMatch(req.params.matchId, 'chat:read_receipt', { matchId: req.params.matchId, userId });
    return ok(res, { marked: true });
  }
}
