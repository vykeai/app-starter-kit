#!/usr/bin/env bash
# Reset the local development database to a clean state.
# ⚠ This DELETES all local data.
#
# Usage: bash scripts/reset-local.sh

set -euo pipefail

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$REPO_ROOT"

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

echo ""
echo -e "${RED}⚠ This will DELETE all local database data.${NC}"
read -rp "Continue? [y/N] " CONFIRM
[[ "$CONFIRM" =~ ^[Yy]$ ]] || { echo "Aborted."; exit 0; }

echo ""
echo "[1/3] Stopping services and removing volumes..."
docker-compose down -v --remove-orphans 2>/dev/null || true
echo -e "      ${GREEN}✓ done${NC}"

echo ""
echo "[2/3] Starting fresh..."
docker-compose up -d

until docker-compose exec -T postgres pg_isready -U postgres &>/dev/null; do sleep 1; done
echo -e "      ${GREEN}✓ postgres ready${NC}"

echo ""
echo "[3/3] Rebuilding schema and seed data..."
(cd apps/starter-api && npx prisma migrate dev --name init 2>/dev/null || npx prisma migrate deploy)
(cd apps/starter-api && npm run db:seed 2>/dev/null || true)
echo -e "      ${GREEN}✓ done${NC}"

echo ""
echo -e "${GREEN}Reset complete. Fresh database ready.${NC}"
echo ""
