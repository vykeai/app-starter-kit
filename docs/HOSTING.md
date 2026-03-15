# Hosting Guide — Railway + Cloudflare R2

Everything you need to go from zero to production.

This guide is intentionally biased toward **Lane A**, the default TheOnlyStack launch path:

- Railway for the application control plane
- Cloudflare R2 for object storage
- Cloudflare Pages or Vercel for web surfaces
- Firebase for push and mobile crash reporting

Do not start on GCP unless the product shape clearly requires it.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Railway Project                       │
│                                                             │
│  ┌──────────────┐  ┌────────────┐  ┌─────────────────────┐ │
│  │  NestJS API  │  │ PostgreSQL │  │  Redis (BullMQ)     │ │
│  │  (service)   │  │ (plugin)   │  │  (plugin)           │ │
│  └──────────────┘  └────────────┘  └─────────────────────┘ │
│                                                             │
│  ┌──────────────┐  ┌────────────┐                          │
│  │ Meilisearch  │  │  PostHog   │                          │
│  │  (service)   │  │ (service)  │                          │
│  └──────────────┘  └────────────┘                          │
└─────────────────────────────────────────────────────────────┘

┌──────────────┐   ┌──────────────┐   ┌───────────────────┐
│  Cloudflare  │   │    Vercel    │   │  Cloudflare       │
│  R2 (media)  │   │ (Next.js     │   │  Pages            │
│              │   │  admin/app)  │   │  (Astro public)   │
└──────────────┘   └──────────────┘   └───────────────────┘
```

---

## Phased Hosting Doctrine

### Default path

| Phase | Recommendation |
|-------|----------------|
| Local development | Run Postgres + Redis locally, use provider sandboxes, keep production services out of the critical path |
| Public alpha | Railway + R2 |
| Public beta | Railway + R2 with staging and production separation |
| Launch | Railway + R2 |
| Under 1M users | Stay on Railway + R2 unless product shape forces a control-plane migration |
| Under 5M users | Railway or graduate app services to GCP; keep R2 unless there is a concrete reason to move |

### Migration rules

- **Do not migrate off Railway because of a user-count milestone alone.**
- **Do not migrate off R2 because the company becomes large.**
- Move the control plane only when you need capabilities Railway is no longer the cheapest correct answer for.
- Keep object storage boring. R2 is meant to stay.

### Railway vs GCP

Use Railway until one of these becomes true:

- multi-region traffic and latency are now first-order problems
- you need more advanced database topology or tighter operational control
- compliance or networking requirements outgrow Railway's model
- support, observability, or SLO pressure require a heavier platform

If those are not true, staying on Railway is usually the correct decision.

### Cloudflare R2 doctrine

R2 is the default for uploads, media, exports, and backups in both early and large-scale phases.

- Keep using the AWS S3 SDK against R2's endpoint so migration stays cheap if you ever need it.
- Do not prematurely swap R2 for S3 or GCS.
- Treat R2 as long-term infrastructure, not just launch infrastructure.

---

## Railway Setup

### 1. Create Project

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Create new project
railway init
```

Or via dashboard: railway.app → New Project.

### 2. Add Services

In the Railway dashboard, add these services to your project:

**Databases (plugins):**
- PostgreSQL — click "Add Plugin" → PostgreSQL
- Redis — click "Add Plugin" → Redis

**Application services:**
- API (NestJS) — connect your GitHub repo, set root directory to `apps/starter-api/`
- Meilisearch — use Docker image `getmeili/meilisearch:v1.5`
- PostHog — use Docker image `posthog/posthog:latest` (or share one instance across projects)

### 3. Environment Variables

Set these on the NestJS API service:

```bash
DATABASE_URL=${{Postgres.DATABASE_URL}}   # Railway auto-injects
REDIS_URL=${{Redis.REDIS_URL}}            # Railway auto-injects
JWT_SECRET=<generate with: openssl rand -base64 32>
RESEND_API_KEY=re_...
MEILISEARCH_URL=http://${{meilisearch.RAILWAY_PRIVATE_DOMAIN}}:7700
MEILISEARCH_KEY=<generate with: openssl rand -base64 32>
FCM_PROJECT_ID=...
FCM_CLIENT_EMAIL=...
FCM_PRIVATE_KEY=...
R2_ACCOUNT_ID=...
R2_ACCESS_KEY_ID=...
R2_SECRET_ACCESS_KEY=...
R2_BUCKET_NAME=...
NODE_ENV=production
```

### 4. Deploy

Railway deploys automatically on push to your configured branch (usually `main`).

```bash
# Manual deploy
railway up

# View logs
railway logs

# Open service
railway open
```

### 5. Custom Domain

Dashboard → your API service → Settings → Custom Domain. Add a CNAME pointing `api.yourapp.com` to the Railway-provided domain.

---

## Cloudflare R2 Setup

### 1. Create Bucket

Cloudflare dashboard → R2 → Create Bucket.

Name it `<project>-media-prod` (and `<project>-media-dev` for development).

### 2. Generate API Credentials

R2 → Manage R2 API Tokens → Create API Token.

Permissions: `Object Read & Write` on your specific bucket.

Note down:
- Account ID (from R2 overview page)
- Access Key ID
- Secret Access Key
- Bucket name
- Endpoint: `https://<account-id>.r2.cloudflarestorage.com`

### 3. SDK Usage (S3-compatible)

```typescript
import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';

const r2 = new S3Client({
  region: 'auto',
  endpoint: `https://${process.env.R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
  credentials: {
    accessKeyId: process.env.R2_ACCESS_KEY_ID,
    secretAccessKey: process.env.R2_SECRET_ACCESS_KEY,
  },
});

// Upload
await r2.send(new PutObjectCommand({
  Bucket: process.env.R2_BUCKET_NAME,
  Key: `users/${userId}/avatar.jpg`,
  Body: fileBuffer,
  ContentType: 'image/jpeg',
}));
```

### 4. Public Access (CDN)

For publicly readable files (avatars, badge images):

R2 → your bucket → Settings → Public Access → Enable.

This gives you a public URL: `https://pub-<id>.r2.dev/<key>`.

For custom domain (recommended): R2 → bucket → Settings → Custom Domain → add `media.yourapp.com`.

---

## Vercel (Next.js admin/app)

```bash
# Install Vercel CLI
npm install -g vercel

# Deploy
cd web-admin
vercel --prod
```

Set environment variables in Vercel dashboard → Project → Settings → Environment Variables.

---

## Cloudflare Pages (Astro public site)

```bash
# Build locally first to verify
cd web-public && npm run build

# Deploy via Cloudflare dashboard
# Pages → Create Application → Connect to Git → select repo
# Build command: npm run build
# Output directory: dist
```

Or use `wrangler`:
```bash
npx wrangler pages deploy dist --project-name=<project>-public
```

---

## Firebase (FCM + Crashlytics)

Firebase is used for push notifications and crash reporting only — not for hosting.

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Create project (or use existing)
firebase projects:create <project-id>
```

In Firebase console:
- Enable Cloud Messaging (FCM) — get `google-services.json` (Android) and `GoogleService-Info.plist` (iOS)
- Enable Crashlytics — add SDK to both mobile apps

---

## Staging vs Production

Use Railway environments for staging:

```bash
# Create staging environment
railway environment create staging

# Deploy to staging
railway up --environment staging
```

Staging uses the same Railway project but a separate PostgreSQL plugin instance and separate env vars.

Default rule:

- separate PostgreSQL per environment
- separate Redis per environment when queues or cache pollution matter
- separate Meilisearch indexes per environment
- separate PostHog project/API key per environment if product analytics quality matters

Do not share environments casually just because the services are easy to create.

---

## Graduation Path: When Railway Stops Being Enough

If the product outgrows Railway, move the control plane first and leave storage alone unless storage itself is the problem.

Typical graduation target:

- Cloud Run for API and worker services
- Cloud SQL for PostgreSQL
- managed Redis
- Cloud CDN / Cloud Storage where needed
- keep Cloudflare R2 if it is still doing the storage job cheaply and reliably

This keeps migrations smaller:

- app services can move without rewriting media/storage code
- object storage stays S3-compatible
- the team solves one platform problem at a time

---

## Cost Estimates

| Scale | Monthly infra |
|-------|--------------|
| Development (0 users) | ~$0–20/mo (Railway Hobby: $5/mo, Cloudflare free tier) |
| Beta (100–1K users) | ~$25–50/mo |
| Early traction (1K–10K users) | ~$50–150/mo |
| Growth (10K–100K users) | ~$150–300/mo |
| Scale (100K–1M users) | ~$450–650/mo |

Railway's free tier covers development. Move to Railway Pro ($20/mo) at first paid user.
