# Infrastructure — Railway

This project deploys to Railway. No Terraform, no IaC, no GCP account needed.

## Services

Add these in your Railway project dashboard:

| Service | Type | Notes |
|---------|------|-------|
| **api** | GitHub repo deploy | Root dir: `apps/starter-api/` |
| **PostgreSQL** | Plugin | Auto-wires `DATABASE_URL` |
| **Redis** | Plugin | Auto-wires `REDIS_URL` |
| **Meilisearch** | Docker image | `getmeili/meilisearch:v1.5` |

## Required Environment Variables (api service)

```bash
# Auto-injected by Railway plugins
DATABASE_URL=${{Postgres.DATABASE_URL}}
REDIS_URL=${{Redis.REDIS_URL}}

# Generate these
JWT_SECRET=                   # openssl rand -base64 32
MEILISEARCH_MASTER_KEY=       # openssl rand -base64 32

# Resend (email)
RESEND_API_KEY=re_...

# Cloudflare R2 (media storage)
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET_NAME=

# Firebase (push + crashlytics)
FCM_PROJECT_ID=
FCM_CLIENT_EMAIL=
FCM_PRIVATE_KEY=

# App
NODE_ENV=production
APP_URL=https://api.yourapp.com
```

## CLI Deploy

```bash
npm install -g @railway/cli
railway login
railway link          # link to existing project
./scripts/prepare-railway-env.sh staging api.onlystack.dev
./scripts/deploy-api-railway.sh staging
railway logs          # tail logs
```

## Environments

Use Railway Environments for staging vs production:

```bash
railway environment create staging
./scripts/deploy-api-railway.sh staging
```

## Media Storage — Cloudflare R2

1. Cloudflare dashboard → R2 → Create Bucket
   - Production: `yourapp-media-prod`
   - Staging: `yourapp-media-staging`
2. R2 → Manage API Tokens → Create Token (Object Read & Write)
3. Add credentials to Railway environment variables
4. Optional: enable public access + custom domain `media.yourapp.com`

See [TheOnlyStack HOSTING.md](https://github.com/czaku/theonlystack/blob/main/docs/HOSTING.md) for full setup guide.
