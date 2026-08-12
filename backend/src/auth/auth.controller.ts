import { Request, Response } from 'express';
import { z } from 'zod';
import { AuthService } from './auth.service';

// In-memory user store for mock/seed fallback before DB connection
const mockUsers = new Map<string, any>();

// Seed initial system accounts for auth testing
(async () => {
  const hash = await AuthService.hashPassword("AdminPass2026!");
  mockUsers.set("usr_super_admin", {
    id: "usr_super_admin",
    email: "superadmin@adventhearts.com",
    passwordHash: hash,
    fullName: "AdventHearts Executive Super Admin",
    role: "SUPER_ADMIN",
    isEmailVerified: true,
    createdAt: new Date().toISOString()
  });
  mockUsers.set("usr_admin", {
    id: "usr_admin",
    email: "admin@adventhearts.com",
    passwordHash: hash,
    fullName: "AdventHearts Operations Admin",
    role: "ADMIN",
    isEmailVerified: true,
    createdAt: new Date().toISOString()
  });
  mockUsers.set("usr_moderator", {
    id: "usr_moderator",
    email: "moderator@adventhearts.com",
    passwordHash: hash,
    fullName: "AdventHearts Community Moderator",
    role: "MODERATOR",
    isEmailVerified: true,
    createdAt: new Date().toISOString()
  });
})();

const registerSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
  fullName: z.string().min(2),
  dateOfBirth: z.string(),
  gender: z.string(),
  country: z.string(),
  city: z.string(),
  adventistAffiliation: z.string().optional(),
});

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string(),
});

export class AuthController {
  static async register(req: Request, res: Response) {
    try {
      const body = registerSchema.parse(req.body);
      const existingUser = Array.from(mockUsers.values()).find(u => u.email === body.email);

      if (existingUser) {
        return res.status(409).json({
          success: false,
          error: { code: 'EMAIL_EXISTS', message: 'User with this email already exists.' }
        });
      }

      const passwordHash = await AuthService.hashPassword(body.password);
      const userId = `usr_${Date.now()}`;
      
      const user = {
        id: userId,
        email: body.email,
        passwordHash,
        fullName: body.fullName,
        role: 'USER',
        isEmailVerified: false,
        createdAt: new Date().toISOString()
      };

      mockUsers.set(userId, user);

      const verificationToken = AuthService.generateEmailVerificationToken(userId);
      const tokens = AuthService.generateTokens({ userId, email: user.email, role: user.role });

      return res.status(201).json({
        success: true,
        data: {
          user: { id: user.id, email: user.email, fullName: user.fullName, role: user.role, isEmailVerified: false },
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          verificationToken
        }
      });
    } catch (err: any) {
      return res.status(400).json({
        success: false,
        error: { code: 'VALIDATION_ERROR', message: err.message || 'Invalid input data.' }
      });
    }
  }

  static async login(req: Request, res: Response) {
    try {
      const { email, password } = loginSchema.parse(req.body);
      const user = Array.from(mockUsers.values()).find(u => u.email === email);

      if (!user) {
        return res.status(401).json({
          success: false,
          error: { code: 'INVALID_CREDENTIALS', message: 'Invalid email or password.' }
        });
      }

      const isValidPassword = await AuthService.verifyPassword(user.passwordHash, password);
      if (!isValidPassword) {
        return res.status(401).json({
          success: false,
          error: { code: 'INVALID_CREDENTIALS', message: 'Invalid email or password.' }
        });
      }

      const tokens = AuthService.generateTokens({ userId: user.id, email: user.email, role: user.role });

      return res.status(200).json({
        success: true,
        data: {
          user: { id: user.id, email: user.email, fullName: user.fullName, role: user.role, isEmailVerified: user.isEmailVerified },
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken
        }
      });
    } catch (err: any) {
      return res.status(400).json({
        success: false,
        error: { code: 'VALIDATION_ERROR', message: err.message || 'Invalid login payload.' }
      });
    }
  }

  static async verifyEmail(req: Request, res: Response) {
    const { token } = req.body;
    if (!token) {
      return res.status(400).json({
        success: false,
        error: { code: 'MISSING_TOKEN', message: 'Verification token required.' }
      });
    }

    const payload = AuthService.verifyEmailToken(token);
    if (!payload) {
      return res.status(400).json({
        success: false,
        error: { code: 'INVALID_TOKEN', message: 'Token is invalid or expired.' }
      });
    }

    const user = mockUsers.get(payload.userId);
    if (user) {
      user.isEmailVerified = true;
    }

    return res.status(200).json({
      success: true,
      data: { message: 'Email verified successfully.' }
    });
  }

  static async refreshToken(req: Request, res: Response) {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      return res.status(400).json({
        success: false,
        error: { code: 'MISSING_TOKEN', message: 'Refresh token required.' }
      });
    }

    const payload = AuthService.verifyRefreshToken(refreshToken);
    if (!payload) {
      return res.status(401).json({
        success: false,
        error: { code: 'INVALID_REFRESH_TOKEN', message: 'Refresh token is expired or invalid.' }
      });
    }

    const tokens = AuthService.generateTokens({ userId: payload.userId, email: payload.email, role: payload.role });
    return res.status(200).json({
      success: true,
      data: tokens
    });
  }

  static async getMe(req: Request, res: Response) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        error: { code: 'UNAUTHORIZED', message: 'Authentication token required.' }
      });
    }

    const token = authHeader.split(' ')[1];
    const payload = AuthService.verifyAccessToken(token);
    if (!payload) {
      return res.status(401).json({
        success: false,
        error: { code: 'INVALID_TOKEN', message: 'Token is invalid or expired.' }
      });
    }

    const user = mockUsers.get(payload.userId) || Array.from(mockUsers.values()).find(u => u.id === payload.userId || u.email === payload.email);

    return res.status(200).json({
      success: true,
      data: {
        user: user ? {
          id: user.id,
          email: user.email,
          fullName: user.fullName,
          role: user.role,
          isEmailVerified: user.isEmailVerified
        } : payload
      }
    });
  }
}
