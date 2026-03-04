#!/usr/bin/env bash
# Start the local development stack and prepare the database.
# Run once after cloning, or any time you need to reset to a clean state.
#
# Usage: bash scripts/start-local.sh

set -euo pipefail

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$REPO_ROOT"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo ""
echo "AppStarterKit — starting local stack"
echo "──────────────────────────────────────"

# ── Step 1: Check prerequisites ────────────────────────────────────────────

if ! command -v docker &>/dev/null; then
  echo "Error: Docker is not installed. Install Docker Desktop from https://docker.com" >&2
  exit 1
fi

if ! docker info &>/dev/null; then
  echo "Error: Docker daemon is not running. Start Docker Desktop." >&2
  exit 1
fi

if [[ ! -f "backend/.env" ]]; then
  echo -e "${YELLOW}⚠ backend/.env not found — copying from .env.example${NC}"
  cp .env.example backend/.env
  echo "  Edit backend/.env and set JWT_SECRET before proceeding."
  echo "  Generate one with: openssl rand -base64 48"
fi

# ── Step 2: Start Docker services ──────────────────────────────────────────

echo ""
echo "[1/4] Starting Docker services (postgres + redis)..."
docker-compose up -d

# Wait for postgres
echo "      Waiting for postgres..."
until docker-compose exec -T postgres pg_isready -U postgres &>/dev/null; do
  sleep 1
done
echo -e "      ${GREEN}✓ postgres ready${NC}"

# Wait for redis
echo "      Waiting for redis..."
until docker-compose exec -T redis redis-cli ping &>/dev/null; do
  sleep 1
done
echo -e "      ${GREEN}✓ redis ready${NC}"

# ── Step 3: Install backend dependencies ───────────────────────────────────

echo ""
echo "[2/4] Installing backend dependencies..."
(cd backend && npm install --silent)
echo -e "      ${GREEN}✓ done${NC}"

# ── Step 4: Run Prisma migrations ──────────────────────────────────────────

echo ""
echo "[3/4] Running database migrations..."
(cd backend && npx prisma migrate dev --name init 2>/dev/null || npx prisma migrate deploy)
echo -e "      ${GREEN}✓ migrations applied${NC}"

# ── Step 5: Seed database ──────────────────────────────────────────────────

echo ""
echo "[4/4] Seeding database..."
(cd backend && npm run db:seed 2>/dev/null || echo "      (no seed script yet — skipping)")
echo -e "      ${GREEN}✓ done${NC}"

# ── Done ───────────────────────────────────────────────────────────────────

echo ""
echo -e "${GREEN}Local stack is ready!${NC}"
echo ""
echo "  Backend:      cd backend && npm run start:dev"
echo "                → http://localhost:3000"
echo "  API docs:     http://localhost:3000/api/docs"
echo "  Health check: http://localhost:3000/health"
echo ""
echo "  Postgres:     postgresql://postgres:postgres@localhost:5432/appstarterkit_dev"
echo "  Redis:        redis://localhost:6379"
echo ""
