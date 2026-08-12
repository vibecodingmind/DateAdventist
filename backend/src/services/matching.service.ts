import { prisma } from '../db/prisma';
import { CompatibilityService } from './compatibility.service';
import { emitToUser } from '../realtime/socket';

export class MatchingService {
  static async like(fromUserId: string, toUserId: string, isSuperLike = false) {
    if (fromUserId === toUserId) {
      throw Object.assign(new Error('Cannot like your own profile.'), { status: 400, code: 'INVALID_TARGET' });
    }

    const target = await prisma.user.findUnique({ where: { id: toUserId } });
    if (!target) {
      throw Object.assign(new Error('Target user not found.'), { status: 404, code: 'USER_NOT_FOUND' });
    }

    const blocked = await prisma.block.findFirst({
      where: {
        OR: [
          { blockerId: fromUserId, blockedUserId: toUserId },
          { blockerId: toUserId, blockedUserId: fromUserId },
        ],
      },
    });
    if (blocked) {
      throw Object.assign(new Error('Unable to interact with this profile.'), { status: 403, code: 'BLOCKED' });
    }

    const like = await prisma.like.upsert({
      where: { fromUserId_toUserId: { fromUserId, toUserId } },
      update: { isSuperLike: isSuperLike || undefined },
      create: { fromUserId, toUserId, isSuperLike },
    });

    const reciprocal = await prisma.like.findUnique({
      where: { fromUserId_toUserId: { fromUserId: toUserId, toUserId: fromUserId } },
    });

    const fromUser = await prisma.user.findUnique({
      where: { id: fromUserId },
      include: { profile: true, faithProfile: true },
    });
    const toUser = await prisma.user.findUnique({
      where: { id: toUserId },
      include: { profile: true, faithProfile: true },
    });

    if (reciprocal) {
      const [user1Id, user2Id] = [fromUserId, toUserId].sort();
      const existing = await prisma.match.findUnique({
        where: { user1Id_user2Id: { user1Id, user2Id } },
      });

      const score = CompatibilityService.score(fromUser ?? {}, toUser ?? {});
      const starter = CompatibilityService.conversationStarter(fromUser ?? {}, toUser ?? {});

      const match =
        existing ??
        (await prisma.match.create({
          data: {
            user1Id,
            user2Id,
            compatibilityScore: score,
            conversationStarter: starter,
          },
        }));

      await prisma.notification.createMany({
        data: [
          {
            userId: fromUserId,
            title: "It's a Match! ❤️",
            body: `You and ${toUser?.profile?.fullName ?? 'someone'} liked each other!`,
            type: 'MATCH',
          },
          {
            userId: toUserId,
            title: "It's a Match! ❤️",
            body: `You and ${fromUser?.profile?.fullName ?? 'someone'} liked each other!`,
            type: 'MATCH',
          },
        ],
      });

      emitToUser(fromUserId, 'match:new', { matchId: match.id, userId: toUserId, compatibilityScore: score });
      emitToUser(toUserId, 'match:new', { matchId: match.id, userId: fromUserId, compatibilityScore: score });

      return {
        isMatch: true,
        matchId: match.id,
        compatibilityScore: score,
        conversationStarter: starter,
        likeId: like.id,
        isSuperLike: like.isSuperLike,
        targetUserId: toUserId,
      };
    }

    await prisma.notification.create({
      data: {
        userId: toUserId,
        title: isSuperLike ? '⭐ Super Like Received!' : 'Someone Liked You!',
        body: `${fromUser?.profile?.fullName ?? 'An Adventist single'} sent you a ${isSuperLike ? 'Super Like' : 'Like'}.`,
        type: isSuperLike ? 'SUPERLIKE' : 'LIKE',
      },
    });

    emitToUser(toUserId, 'like:new', { fromUserId, isSuperLike });

    return {
      isMatch: false,
      matchId: null,
      compatibilityScore: CompatibilityService.score(fromUser ?? {}, toUser ?? {}),
      likeId: like.id,
      isSuperLike: like.isSuperLike,
      targetUserId: toUserId,
    };
  }

  static async pass(fromUserId: string, toUserId: string) {
    await prisma.pass.upsert({
      where: { fromUserId_toUserId: { fromUserId, toUserId } },
      update: {},
      create: { fromUserId, toUserId },
    });
    return { passed: true, targetUserId: toUserId };
  }
}
