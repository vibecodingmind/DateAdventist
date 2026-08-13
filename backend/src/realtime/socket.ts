import { Server as HttpServer } from 'http';
import { Server } from 'socket.io';
import { AuthService } from '../auth/auth.service';
import { corsOriginOption } from '../config';

let io: Server | null = null;

export function initRealtime(httpServer: HttpServer) {
  io = new Server(httpServer, {
    cors: { origin: corsOriginOption(), methods: ['GET', 'POST'] },
  });

  io.use((socket, next) => {
    const token =
      (socket.handshake.auth?.token as string | undefined) ||
      (socket.handshake.headers.authorization?.toString().replace('Bearer ', '') ?? '');
    const payload = token ? AuthService.verifyAccessToken(token) : null;
    if (!payload) {
      return next(new Error('UNAUTHORIZED'));
    }
    socket.data.userId = payload.userId;
    next();
  });

  io.on('connection', (socket) => {
    const userId = socket.data.userId as string;
    socket.join(`user:${userId}`);

    socket.on('chat:join', (matchId: string) => {
      if (matchId) socket.join(`chat:${matchId}`);
    });

    socket.on('chat:typing', (payload: { matchId: string }) => {
      if (payload?.matchId) {
        socket.to(`chat:${payload.matchId}`).emit('chat:typing', { userId, matchId: payload.matchId });
      }
    });

    socket.on('disconnect', () => {
      socket.leave(`user:${userId}`);
    });
  });

  return io;
}

export function emitToUser(userId: string, event: string, data: unknown) {
  io?.to(`user:${userId}`).emit(event, data);
}

export function emitToMatch(matchId: string, event: string, data: unknown) {
  io?.to(`chat:${matchId}`).emit(event, data);
}

export function getIo() {
  return io;
}
