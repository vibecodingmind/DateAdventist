import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from './auth.middleware';
import { fail } from '../utils/http';

export type UserRole = 'USER' | 'MODERATOR' | 'ADMIN' | 'SUPER_ADMIN';

const ROLE_WEIGHTS: Record<UserRole, number> = {
  USER: 1,
  MODERATOR: 2,
  ADMIN: 3,
  SUPER_ADMIN: 4,
};

export const requireRole = (minRequiredRole: UserRole) => {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    const userRole = (req.user?.role as UserRole) || 'USER';
    const userWeight = ROLE_WEIGHTS[userRole] || 1;
    const requiredWeight = ROLE_WEIGHTS[minRequiredRole] || 4;

    if (userWeight < requiredWeight) {
      return fail(
        res,
        'FORBIDDEN_ROLE_ACCESS',
        `Forbidden: Access requires ${minRequiredRole} role or higher. Your role: ${userRole}`,
        403,
        { requiredRole: minRequiredRole, currentRole: userRole }
      );
    }

    next();
  };
};
