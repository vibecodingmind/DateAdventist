import { Request, Response, NextFunction } from 'express';

const buckets = new Map<string, { count: number; resetAt: number }>();

export function rateLimit({ windowMs = 60_000, max = 30, key = 'global' }: { windowMs?: number; max?: number; key?: string } = {}) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (process.env.NODE_ENV === 'test') return next();
    const id = `${key}:${req.ip}`;
    const now = Date.now();
    const current = buckets.get(id);
    if (!current || current.resetAt < now) {
      buckets.set(id, { count: 1, resetAt: now + windowMs });
      return next();
    }
    current.count += 1;
    if (current.count > max) {
      return res.status(429).json({
        success: false,
        error: { code: 'RATE_LIMITED', message: 'Too many requests. Please wait and try again.' },
      });
    }
    next();
  };
}
