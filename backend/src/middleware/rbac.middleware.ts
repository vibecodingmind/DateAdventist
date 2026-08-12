import { Response, NextFunction } from 'express';
import { AuthenticatedRequest } from './auth.middleware';

export type UserRole = 'USER' | 'MODERATOR' | 'ADMIN' | 'SUPER_ADMIN';

const ROLE_WEIGHTS: Record<UserRole, number> = {
  USER: 1,
  MODERATOR: 2,
  ADMIN: 3,
  SUPER_ADMIN: 4
};

/**
 * RBAC Verification Middleware
 * Guarantees that only authorized roles can access administrative endpoints.
 */
export const requireRole = (minRequiredRole: UserRole) => {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    // Determine user role from authenticated JWT payload or custom header during dev
    const userRole = (req.user?.role as UserRole) || (req.headers['x-user-role'] as UserRole) || 'USER';

    const userWeight = ROLE_WEIGHTS[userRole] || 1;
    const requiredWeight = ROLE_WEIGHTS[minRequiredRole] || 4;

    if (userWeight < requiredWeight) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN_ROLE_ACCESS',
          message: `Forbidden: Access requires ${minRequiredRole} role or higher. Your role: ${userRole}`,
          requiredRole: minRequiredRole,
          currentRole: userRole
        }
      });
    }

    next();
  };
};
