import express from 'express';
import cors from 'cors';
import http from 'http';
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

const app = express();

app.use(cors());
app.use(express.json({ limit: '2mb' }));

app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/profile', profileRoutes);
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
  server.listen(config.port, () => {
    console.log(`AdventHearts Backend API running on port ${config.port}`);
  });
}

export default app;
