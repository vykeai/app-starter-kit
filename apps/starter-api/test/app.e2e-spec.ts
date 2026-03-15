import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import * as request from 'supertest';
import { getQueueToken } from '@nestjs/bull';
import { PrismaService } from '../src/prisma/prisma.service';

process.env.NODE_ENV = 'development';
process.env.DATABASE_URL = 'postgresql://starter:starter@localhost:5432/starter_test';
process.env.JWT_SECRET = 'test-jwt-secret-test-jwt-secret-123';
process.env.AUTH_DELIVERY_MODE = 'console';

// eslint-disable-next-line @typescript-eslint/no-var-requires
const { AppModule } = require('../src/app.module');

// E2E smoke tests — run against a fully-assembled NestJS app with Prisma + Queue mocked.
// These verify routing, pipes, and response shapes without a real database.
// For full integration tests with a real DB, set DATABASE_URL to a test database.

const mockPrismaService = {
  user: {
    upsert: jest.fn().mockResolvedValue({
      id: 'e2e-user-id',
      email: 'test@example.com',
      displayName: null,
    }),
    findUnique: jest.fn().mockResolvedValue({
      id: 'e2e-user-id',
      email: 'test@example.com',
      displayName: null,
      preferences: {
        theme: 'system',
        pushMarketingEnabled: false,
        pushActivityEnabled: true,
        pushTransactionalEnabled: true,
        pushSystemEnabled: true,
        emailNotificationsEnabled: true,
      },
    }),
  },
  magicLink: {
    create: jest.fn().mockResolvedValue({ id: 'ml-id' }),
    findFirst: jest.fn().mockResolvedValue({
      id: 'ml-id',
      code: '12345678',
      userId: 'e2e-user-id',
      expiresAt: new Date(Date.now() + 60_000),
      usedAt: null,
    }),
    update: jest.fn().mockResolvedValue({}),
  },
  refreshToken: {
    create: jest.fn().mockResolvedValue({ token: 'test-refresh-token' }),
    findUnique: jest.fn().mockResolvedValue({
      token: 'test-refresh-token',
      userId: 'e2e-user-id',
      revokedAt: null,
      expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
    }),
    updateMany: jest.fn().mockResolvedValue({ count: 1 }),
  },
  userPreferences: {
    upsert: jest.fn().mockImplementation(async ({ create, update }) => ({
      theme: update?.theme ?? create?.theme ?? 'system',
      pushMarketingEnabled:
        update?.pushMarketingEnabled ?? create?.pushMarketingEnabled ?? false,
      pushActivityEnabled:
        update?.pushActivityEnabled ?? create?.pushActivityEnabled ?? true,
      pushTransactionalEnabled:
        update?.pushTransactionalEnabled ??
        create?.pushTransactionalEnabled ??
        true,
      pushSystemEnabled:
        update?.pushSystemEnabled ?? create?.pushSystemEnabled ?? true,
      emailNotificationsEnabled:
        update?.emailNotificationsEnabled ??
        create?.emailNotificationsEnabled ??
        true,
      updatedAt: new Date(),
    })),
  },
  pushDevice: {
    upsert: jest.fn().mockResolvedValue({
      id: 'push-device-id',
      platform: 'ios',
      token: 'push-token-123',
      locale: 'en-GB',
      appVersion: '1.0.0',
      lastSeenAt: new Date(),
      revokedAt: null,
    }),
    updateMany: jest.fn().mockResolvedValue({ count: 1 }),
  },
  appVersion: {
    findFirst: jest.fn().mockResolvedValue({
      platform: 'ios',
      minimumVersion: '1.0.0',
      latestVersion: '1.0.0',
      isUpdateRequired: false,
      isUpdateRecommended: false,
    }),
  },
};

const mockEmailQueue = {
  add: jest.fn(),
  process: jest.fn(),
};

describe('App (e2e)', () => {
  let app: INestApplication;

  beforeAll(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    })
      .overrideProvider(PrismaService)
      .useValue(mockPrismaService)
      .overrideProvider(getQueueToken('email'))
      .useValue(mockEmailQueue)
      .compile();

    app = moduleFixture.createNestApplication();
    app.setGlobalPrefix('api/v1');
    app.useGlobalPipes(
      new ValidationPipe({ whitelist: true, transform: true }),
    );
    await app.init();
  });

  afterAll(async () => {
    await app?.close();
  });

  // ── Health ──────────────────────────────────────────────────────────────────

  it('GET /health returns 200', () => {
    return request(app.getHttpServer())
      .get('/api/v1/health')
      .expect(200)
      .expect((res) => {
        expect(res.body.status).toBe('ok');
        expect(res.body.timestamp).toBeDefined();
      });
  });

  // ── Version check ───────────────────────────────────────────────────────────

  it('GET /api/v1/app/version-check returns 200 with update flags', () => {
    return request(app.getHttpServer())
      .get('/api/v1/app/version-check')
      .query({ platform: 'ios', version: '1.0.0' })
      .expect(200)
      .expect((res) => {
        expect(res.body).toHaveProperty('isUpdateRequired');
        expect(res.body).toHaveProperty('isUpdateRecommended');
        expect(res.body).toHaveProperty('minimumVersion');
        expect(res.body).toHaveProperty('latestVersion');
      });
  });

  // ── Auth — magic link request ───────────────────────────────────────────────

  it('POST /api/v1/auth/magic-link/request returns 201 with message', () => {
    return request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/request')
      .send({ email: 'test@example.com' })
      .expect(201)
      .expect((res) => {
        expect(res.body.message).toBe('Code sent');
        expect(res.body.deliveryMode).toBeDefined();
      });
  });

  it('POST /api/v1/auth/magic-link/request rejects invalid email', () => {
    return request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/request')
      .send({ email: 'not-an-email' })
      .expect(400);
  });

  it('POST /api/v1/auth/magic-link/request rejects missing body', () => {
    return request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/request')
      .send({})
      .expect(400);
  });

  // ── Auth — magic link verify ────────────────────────────────────────────────

  it('POST /api/v1/auth/magic-link/verify returns accessToken and user', () => {
    return request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/verify')
      .send({ email: 'test@example.com', code: '12345678' })
      .expect(201)
      .expect((res) => {
        expect(res.body).toHaveProperty('accessToken');
        expect(res.body).toHaveProperty('refreshToken');
        expect(res.body.user).toMatchObject({ email: 'test@example.com' });
      });
  });

  it('POST /api/v1/auth/magic-link/verify accepts linkToken payloads', async () => {
    const requestResponse = await request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/request')
      .send({ email: 'test@example.com' })
      .expect(201);

    expect(requestResponse.body.linkToken).toBeDefined();

    await request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/verify')
      .send({ linkToken: requestResponse.body.linkToken })
      .expect(201)
      .expect((res) => {
        expect(res.body).toHaveProperty('accessToken');
        expect(res.body.user).toMatchObject({ email: 'test@example.com' });
      });
  });

  it('POST /api/v1/auth/magic-link/verify rejects missing code', () => {
    return request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/verify')
      .send({ email: 'test@example.com' })
      .expect(400);
  });

  // ── Notifications ──────────────────────────────────────────────────────────

  it('GET /api/v1/notifications/preferences returns the authenticated preference snapshot', async () => {
    const verify = await request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/verify')
      .send({ email: 'test@example.com', code: '12345678' })
      .expect(201);

    await request(app.getHttpServer())
      .get('/api/v1/notifications/preferences')
      .set('Authorization', `Bearer ${verify.body.accessToken}`)
      .expect(200)
      .expect((res) => {
        expect(res.body.theme).toBeDefined();
        expect(res.body.pushActivityEnabled).toBe(true);
      });
  });

  it('PATCH /api/v1/notifications/preferences updates notification flags', async () => {
    const verify = await request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/verify')
      .send({ email: 'test@example.com', code: '12345678' })
      .expect(201);

    await request(app.getHttpServer())
      .patch('/api/v1/notifications/preferences')
      .set('Authorization', `Bearer ${verify.body.accessToken}`)
      .send({ pushMarketingEnabled: true, emailNotificationsEnabled: false })
      .expect(200)
      .expect((res) => {
        expect(res.body.pushMarketingEnabled).toBe(true);
        expect(res.body.emailNotificationsEnabled).toBe(false);
      });
  });

  it('POST /api/v1/notifications/devices registers a device token', async () => {
    const verify = await request(app.getHttpServer())
      .post('/api/v1/auth/magic-link/verify')
      .send({ email: 'test@example.com', code: '12345678' })
      .expect(201);

    await request(app.getHttpServer())
      .post('/api/v1/notifications/devices')
      .set('Authorization', `Bearer ${verify.body.accessToken}`)
      .send({
        platform: 'ios',
        token: 'push-token-123',
        locale: 'en-GB',
        appVersion: '1.0.0',
      })
      .expect(201)
      .expect((res) => {
        expect(res.body.token).toBe('push-token-123');
        expect(res.body.platform).toBe('ios');
      });
  });
});
