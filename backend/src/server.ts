import express from 'express';
import cors from 'cors';
import http from 'http';
import multer from 'multer';
import path from 'path';
import crypto from 'crypto';
import { config } from './config';
import authRoutes from './routes/auth.routes';
import profileRoutes from './routes/profile.routes';
import subscriptionRoutes from './routes/subscription.routes';
import discoverRoutes from './routes/discover.routes';
import matchRoutes, { likesRouter } from './routes/match.routes';
import adminRoutes from './routes/admin.routes';
import { safetyRouter, notificationRouter } from './routes/safety.routes';
import { errorHandler, notFoundHandler } from './middleware/error.middleware';
import { initRealtime } from './realtime/socket';
import { rateLimit } from './middleware/rate-limit.middleware';
import { requireAuth } from './middleware/auth.middleware';
import { UploadController } from './controllers/upload.controller';
import { SubscriptionController } from './controllers/subscription.controller';
import { ensureUploadDir, getUploadDir } from './services/storage.service';

ensureUploadDir();

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, getUploadDir()),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname || '.jpg') || '.jpg';
    cb(null, `${Date.now()}-${crypto.randomBytes(8).toString('hex')}${ext}`);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: 8 * 1024 * 1024 },
  fileFilter: (_req, file, cb) => {
    if (!file.mimetype || file.mimetype.startsWith('image/')) cb(null, true);
    else cb(new Error('Only image uploads are allowed'));
  },
});

const app = express();

app.use(cors());
app.use('/uploads', express.static(getUploadDir()));
app.post(
  '/api/v1/subscriptions/webhook',
  express.raw({ type: 'application/json' }),
  SubscriptionController.webhook
);
app.use(express.json({ limit: '2mb' }));
app.use(rateLimit({ windowMs: 60_000, max: 120, key: 'api' }));

app.use('/api/v1/auth', rateLimit({ windowMs: 60_000, max: 40, key: 'auth' }), authRoutes);
app.use('/api/v1/profile', profileRoutes);
app.post('/api/v1/profile/photo', requireAuth, upload.single('photo'), UploadController.photo);
app.use('/api/v1/subscriptions', subscriptionRoutes);
app.use('/api/v1/discover', discoverRoutes);
app.use('/api/v1/matches', matchRoutes);
app.use('/api/v1/conversations', matchRoutes);
app.use('/api/v1/likes', likesRouter);
app.use('/api/v1/safety', safetyRouter);
app.use('/api/v1/notifications', notificationRouter);
app.use('/api/v1/admin', adminRoutes);

app.get('/health', (_req, res) => {
  res.json({ success: true, status: 'HEALTHY', timestamp: new Date().toISOString() });
});

app.use(notFoundHandler);
app.use(errorHandler);

export function createServer() {
  const server = http.createServer(app);
  initRealtime(server);
  return server;
}

if (process.env.NODE_ENV !== 'test') {
  const server = createServer();
  const host = process.env.HOST || '0.0.0.0';

  (async () => {
    const { prisma } = await import('./db/prisma');
    const existing = await prisma.user.count();
    if (existing === 0) {
      const { seedDatabase } = await import('./seed');
      console.log('Empty database — loading AdventHearts demo accounts...');
      await seedDatabase();
    }
    server.listen(config.port, host, () => {
      console.log(`AdventHearts Backend API listening on ${host}:${config.port}`);
    });
  })().catch((err) => {
    console.error('Failed to start AdventHearts API', err);
    process.exit(1);
  });
}

export default app;
