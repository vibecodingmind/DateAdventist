import { execSync } from 'child_process';
import path from 'path';
import request from 'supertest';
import app from './server';
import { prisma } from './db/prisma';
import { seedDatabase } from './seed';

const backendRoot = path.resolve(__dirname, '..');

beforeAll(async () => {
  execSync('npx prisma db push --force-reset --accept-data-loss', {
    cwd: backendRoot,
    env: { ...process.env, DATABASE_URL: process.env.DATABASE_URL },
    stdio: 'pipe',
  });
  await seedDatabase();
});

afterAll(async () => {
  await prisma.$disconnect();
});

describe('AdventHearts API', () => {
  let memberToken = '';
  let adminToken = '';
  let secondUserToken = '';
  let secondUserId = '';
  let matchId = 'match_sarah_joshua';

  it('reports healthy status', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.status).toBe('HEALTHY');
  });

  it('logs in a seeded member', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'john.adventist@gmail.com',
      password: 'password123',
    });
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.userId).toBe('usr_me');
    expect(res.body.data.accessToken).toBeTruthy();
    memberToken = res.body.data.accessToken;
  });

  it('rejects invalid credentials', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'john.adventist@gmail.com',
      password: 'wrong-password',
    });
    expect(res.status).toBe(401);
    expect(res.body.success).toBe(false);
  });

  it('registers a new member and returns tokens', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      email: 'new.member@adventhearts.com',
      password: 'Password123!',
      fullName: 'Naomi Advent',
      age: 27,
      gender: 'Female',
      country: 'United States',
      city: 'Orlando',
    });
    expect(res.status).toBe(201);
    expect(res.body.data.accessToken).toBeTruthy();
    secondUserToken = res.body.data.accessToken;
    secondUserId = res.body.data.userId;
  });

  it('returns the current profile', async () => {
    const res = await request(app).get('/api/v1/profile').set('Authorization', `Bearer ${memberToken}`);
    expect(res.status).toBe(200);
    expect(res.body.data.fullName).toBe('Joshua Miller');
    expect(res.body.data.localChurch).toContain('Pioneer');
  });

  it('returns discovery candidates excluding self', async () => {
    const res = await request(app).get('/api/v1/discover').set('Authorization', `Bearer ${memberToken}`);
    expect(res.status).toBe(200);
    const ids = res.body.data.map((p: { userId: string }) => p.userId);
    expect(ids).not.toContain('usr_me');
    expect(ids.length).toBeGreaterThan(0);
    expect(res.body.data[0].compatibilityScore).toBeGreaterThanOrEqual(60);
  });

  it('creates a match when likes are reciprocal', async () => {
    const likeRes = await request(app)
      .post('/api/v1/discover/like')
      .set('Authorization', `Bearer ${memberToken}`)
      .send({ toUserId: 'usr_hannah', isSuperLike: false });
    expect(likeRes.status).toBe(200);
    expect(likeRes.body.data.isMatch).toBe(true);
    expect(likeRes.body.data.matchId).toBeTruthy();
  });

  it('lists matches for the member', async () => {
    const res = await request(app).get('/api/v1/matches').set('Authorization', `Bearer ${memberToken}`);
    expect(res.status).toBe(200);
    expect(res.body.data.length).toBeGreaterThan(0);
    const sarah = res.body.data.find((m: { matchId: string }) => m.matchId === 'match_sarah_joshua');
    expect(sarah.otherProfile.fullName).toBe('Sarah Moretz');
  });

  it('sends and lists chat messages', async () => {
    const send = await request(app)
      .post(`/api/v1/matches/${matchId}/messages`)
      .set('Authorization', `Bearer ${memberToken}`)
      .send({ text: 'Would you like to join Sabbath vespers this week?' });
    expect(send.status).toBe(201);
    expect(send.body.data.text).toContain('vespers');

    const list = await request(app)
      .get(`/api/v1/matches/${matchId}/messages`)
      .set('Authorization', `Bearer ${memberToken}`);
    expect(list.status).toBe(200);
    expect(list.body.data.some((m: { text: string }) => m.text.includes('vespers'))).toBe(true);
  });

  it('records a safety report and block', async () => {
    const report = await request(app)
      .post('/api/v1/safety/report')
      .set('Authorization', `Bearer ${memberToken}`)
      .send({ reportedUserId: secondUserId, reason: 'Spam', details: 'Test report' });
    expect(report.status).toBe(201);

    const block = await request(app)
      .post('/api/v1/safety/block')
      .set('Authorization', `Bearer ${memberToken}`)
      .send({ blockedUserId: secondUserId });
    expect(block.status).toBe(200);
    expect(block.body.data.blocked).toBe(true);
  });

  it('activates a subscription after checkout confirmation', async () => {
    const checkout = await request(app)
      .post('/api/v1/subscriptions/checkout')
      .set('Authorization', `Bearer ${secondUserToken}`)
      .send({ planId: 'plan_gold_monthly', paymentProvider: 'stripe' });
    expect(checkout.status).toBe(200);

    const confirm = await request(app)
      .post('/api/v1/subscriptions/confirm-payment')
      .set('Authorization', `Bearer ${secondUserToken}`)
      .send({ transactionId: checkout.body.data.transactionId, planId: 'plan_gold_monthly' });
    expect(confirm.status).toBe(200);
    expect(confirm.body.data.tier).toBe('GOLD');
  });

  it('enforces admin RBAC', async () => {
    const denied = await request(app)
      .get('/api/v1/admin/users')
      .set('Authorization', `Bearer ${memberToken}`);
    expect(denied.status).toBe(403);

    const login = await request(app).post('/api/v1/admin/login').send({
      email: 'admin@adventhearts.com',
      password: 'AdminPass2026!',
    });
    expect(login.status).toBe(200);
    adminToken = login.body.data.accessToken;

    const users = await request(app).get('/api/v1/admin/users').set('Authorization', `Bearer ${adminToken}`);
    expect(users.status).toBe(200);
    expect(users.body.data.length).toBeGreaterThan(3);

    const settingsDenied = await request(app)
      .get('/api/v1/admin/settings')
      .set('Authorization', `Bearer ${adminToken}`);
    expect(settingsDenied.status).toBe(403);
  });

  it('lets a super admin read settings', async () => {
    const login = await request(app).post('/api/v1/admin/login').send({
      email: 'superadmin@adventhearts.com',
      password: 'AdminPass2026!',
    });
    const settings = await request(app)
      .get('/api/v1/admin/settings')
      .set('Authorization', `Bearer ${login.body.data.accessToken}`);
    expect(settings.status).toBe(200);
    expect(settings.body.data.provider).toBe('Stripe');
  });
});
