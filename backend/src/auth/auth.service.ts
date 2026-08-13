import argon2 from 'argon2';
import jwt from 'jsonwebtoken';
import { config } from '../config';

export interface TokenPayload {
  userId: string;
  email: string;
  role: string;
}

export class AuthService {
  /**
   * Hashes plain text password using secure Argon2id configuration
   */
  static async hashPassword(password: string): Promise<string> {
    return argon2.hash(password, {
      type: argon2.argon2id,
      memoryCost: config.argonMemoryCost,
      timeCost: config.argonTimeCost,
    });
  }

  /**
   * Verifies plain text password against Argon2id hash
   */
  static async verifyPassword(hash: string, plainText: string): Promise<boolean> {
    try {
      return await argon2.verify(hash, plainText);
    } catch {
      return false;
    }
  }

  /**
   * Generates Access Token & Refresh Token pair
   */
  static generateTokens(payload: TokenPayload): { accessToken: string; refreshToken: string } {
    const accessToken = jwt.sign(payload, config.jwtSecret, {
      expiresIn: config.jwtExpiresIn as any,
    });
    const refreshToken = jwt.sign(payload, config.jwtRefreshSecret, {
      expiresIn: config.jwtRefreshExpiresIn as any,
    });
    return { accessToken, refreshToken };
  }

  /**
   * Verifies Access Token
   */
  static verifyAccessToken(token: string): TokenPayload | null {
    try {
      return jwt.verify(token, config.jwtSecret) as TokenPayload;
    } catch {
      return null;
    }
  }

  /**
   * Verifies Refresh Token
   */
  static verifyRefreshToken(token: string): TokenPayload | null {
    try {
      return jwt.verify(token, config.jwtRefreshSecret) as TokenPayload;
    } catch {
      return null;
    }
  }

  /**
   * Generates email verification token
   */
  static generateEmailVerificationToken(userId: string): string {
    return jwt.sign({ userId, purpose: 'email_verification' }, config.jwtSecret, { expiresIn: '24h' });
  }

  static verifyEmailToken(token: string): { userId: string } | null {
    try {
      const decoded = jwt.verify(token, config.jwtSecret) as { userId?: string; purpose?: string };
      if (decoded.purpose === 'email_verification' && decoded.userId) {
        return { userId: decoded.userId };
      }
      return null;
    } catch {
      return null;
    }
  }

  static generatePasswordResetToken(userId: string): string {
    return jwt.sign({ userId, purpose: 'password_reset' }, config.jwtSecret, { expiresIn: '1h' });
  }

  static verifyPasswordResetToken(token: string): { userId: string } | null {
    try {
      const decoded = jwt.verify(token, config.jwtSecret) as any;
      if (decoded.purpose === 'password_reset') {
        return { userId: decoded.userId };
      }
      return null;
    } catch {
      return null;
    }
  }
}
