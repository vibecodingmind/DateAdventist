import { Request, Response, NextFunction } from 'express';
import { AuthService, TokenPayload } from '../auth/auth.service';
import { prisma } from '../db/prisma';
import { fail } from '../utils/http';

export interface AuthenticatedRequest extends Request {
  user?: TokenPayload;
}

export const requireAuth = async (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return fail(res, 'UNAUTHORIZED', 'Authentication required. Missing Bearer token.', 401);
  }

  const token = authHeader.split(' ')[1];
  const payload = AuthService.verifyAccessToken(token);
  if (!payload) {
    return fail(res, 'INVALID_TOKEN', 'Token is invalid or expired.', 401);
  }

  const user = await prisma.user.findUnique({ where: { id: payload.userId } });
  if (!user || user.status === 'BANNED' || user.status === 'DELETED' || user.status === 'SUSPENDED') {
    return fail(res, 'ACCOUNT_INACTIVE', 'This account is not allowed to access the platform.', 403);
  }

  req.user = { ...payload, role: user.role };
  next();
};

export const requireRole = (roles: string[]) => {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    if (!req.user || !roles.includes(req.user.role)) {
      return fail(res, 'FORBIDDEN', 'Insufficient permissions.', 403);
    }
    next();
  };
};
