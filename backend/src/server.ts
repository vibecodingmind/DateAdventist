import express from 'express';
import cors from 'cors';
import http from 'http';
import multer from 'multer';
import path from 'path';
import crypto from 'crypto';
import { config, assertLiveConfig, corsOriginOption } from './config';
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
import { TERMS_TEXT, TERMS_TITLE, PRIVACY_TEXT, PRIVACY_TITLE, legalHtml } from './legal/content';

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
app.disable('x-powered-by');
app.set('trust proxy', 1);
app.use(cors({ origin: corsOriginOption() }));
app.use((_req, res, next) => {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('Referrer-Policy', 'same-origin');
  if (config.publicBaseUrl.startsWith('https://')) {
    res.setHeader('Strict-Transport-Security', 'max-age=31536000; includeSubDomains');
  }
  next();
});
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
app.get('/legal/terms', (_req, res) => {
  res.type('html').send(legalHtml(TERMS_TITLE, TERMS_TEXT));
});
app.get('/legal/privacy', (_req, res) => {
  res.type('html').send(legalHtml(PRIVACY_TITLE, PRIVACY_TEXT));
});
app.get('/api/v1/legal/terms', (_req, res) => {
  res.json({ success: true, data: { title: TERMS_TITLE, text: TERMS_TEXT } });
});
app.get('/api/v1/legal/privacy', (_req, res) => {
  res.json({ success: true, data: { title: PRIVACY_TITLE, text: PRIVACY_TEXT } });
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
    assertLiveConfig();
    const { prisma } = await import('./db/prisma');
    const existing = await prisma.user.count();
    const canSeed = config.env !== 'production' || config.allowDemoSeed;
    if (existing === 0 && canSeed) {
      const { seedDatabase } = await import('./seed');
      console.log('Empty database — loading AdventHearts demo accounts...');
      await seedDatabase();
    } else if (existing === 0 && config.env === 'production') {
      console.log('Empty production database — demo seed skipped. Create a real admin account after start.');
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
