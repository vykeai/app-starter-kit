# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies (cached layer)
COPY package*.json ./
COPY prisma ./prisma/
RUN npm ci --only=production=false

# Generate Prisma client
RUN npx prisma generate

# Build TypeScript
COPY . .
RUN npm run build

# ── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only what's needed
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/prisma ./prisma
COPY package*.json ./

# Ownership
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000/health', (r) => process.exit(r.statusCode === 200 ? 0 : 1))"

CMD ["node", "dist/main.js"]
