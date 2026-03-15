#!/usr/bin/env bash
set -euo pipefail

environment="${1:-staging}"
app_host="${2:-api.onlystack.dev}"

generate_secret() {
  openssl rand -base64 32 | tr -d '\n'
}

cat <<EOF
# Onlystack Railway environment template
# Environment: ${environment}
# Service: apps/starter-api
#
# Paste these into the Railway api service variables panel.

NODE_ENV=production
APP_URL=https://${app_host}

# Auto-injected by Railway plugins
DATABASE_URL=\${{Postgres.DATABASE_URL}}
REDIS_URL=\${{Redis.REDIS_URL}}

# Required generated secrets
JWT_SECRET=$(generate_secret)
MEILISEARCH_MASTER_KEY=$(generate_secret)

# Email delivery
RESEND_API_KEY=re_...
AUTH_EMAIL_DELIVERY_MODE=email

# Cloudflare R2 media storage
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET_NAME=onlystack-media-${environment}
R2_PUBLIC_BASE_URL=https://media.onlystack.dev

# Push providers
FCM_PROJECT_ID=
FCM_CLIENT_EMAIL=
FCM_PRIVATE_KEY=
EOF
