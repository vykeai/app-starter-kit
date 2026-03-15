# Onlystack

Onlystack is the monorepo for the default product stack: runnable starter apps,
shared reusable libraries, and the doctrine that ties them together.

The current executable reference is still the mobile starter app:
**NestJS API + iOS (SwiftUI) + Android (Compose)**.

Magic link auth, social sign-in, design tokens, offline indicator, force update,
review prompt, subscriptions, admin panel, Railway deploy baseline — all wired up and
ready to reuse.

Shared library boundaries in this monorepo:
- `auth-core`
- `network-core`
- `notifications-core`
- `contracts`
- `user-core`
- `media-core`
- `billing-core`
- `analytics-core`

Runnable starter surfaces in this monorepo:
- `starter-api`
- `starter-ios`
- `starter-android`
- `starter-web-public`
- `starter-web-admin`
- `starter-web-app`

Stack doctrine lives in:
- [STACK.md](/Users/ted/dev/onlystack/docs/STACK.md)
- [CONVENTIONS.md](/Users/ted/dev/onlystack/docs/CONVENTIONS.md)
- [HOSTING.md](/Users/ted/dev/onlystack/docs/HOSTING.md)

---

## Stack

| Platform | Language | UI | DB | Auth |
|----------|----------|----|----|------|
| Backend | TypeScript | NestJS 10 | PostgreSQL 16 (Prisma 5) | Magic link + JWT + Social |
| iOS | Swift | SwiftUI | — | Keychain |
| Android | Kotlin | Jetpack Compose | Room | EncryptedSharedPreferences |

**Backend services:** Redis (BullMQ) · Swagger at `/api/docs` · Rate limiting · FCM push stub

---

## Quick Start

### 1. Clone and rename

```bash
# Use this repo as a GitHub template, then run the interactive rename script:
./scripts/rename.sh

# Or non-interactively:
./scripts/rename.sh "MyApp" "com.mycompany.myapp"
```

The script replaces all occurrences of `StarterApp`, `com.onlystack.starterapp`, and `starter-app` across every platform, renames source directories, and regenerates the xcodeproj.

### 2. Start local infrastructure

```bash
./scripts/start-local.sh
# Starts postgres:16 + redis:7 via Docker Compose, runs migrations + seed
```

### 3. Backend

```bash
cd apps/starter-api
cp .env.example .env    # fill in JWT_SECRET, SMTP settings, etc.
npm install
npm run start:dev       # http://localhost:3000/api/v1
# Swagger UI: http://localhost:3000/api/docs
```

### 4. iOS

```bash
cd apps/starter-ios
brew install xcodegen   # if not installed
xcodegen generate
open StarterApp.xcodeproj
# Select scheme StarterApp-Dev and run
```

### 5. Android

```bash
cd apps/starter-android
./gradlew assembleDevDebug
# Or open in Android Studio, select dev flavour
```

### 6. Web

```bash
cd apps/starter-web-public && npm install && npm run dev
cd apps/starter-web-admin && npm install && npm run dev
cd apps/starter-web-app && npm install && npm run dev
```

### 7. Runtime mock mode

```bash
# Android fixture-backed runtime mode
cd apps/starter-android
./gradlew assembleMockDebug

# iOS fixture-backed runtime mode
# Add launch argument: -UITestMode
```

---

## What's Included

### Authentication
- Magic link (email OTP) — no passwords, no friction
- JWT + refresh tokens with auto-rotation and server-side revocation
- Token refresh interceptor (silent 401 retry on both platforms)
- Logout (client-side clear + server token revocation)
- Keychain (iOS) / EncryptedSharedPreferences (Android) secure storage
- Biometric auth (Face ID / Touch ID on iOS, BiometricPrompt on Android)
- Deep link handler (magic link tap-to-login auto-fills OTP field)
- **Sign in with Apple** (iOS only — full `ASAuthorizationController` impl)
- **Sign in with Google** (iOS + Android — SDK stubs with TODO comments)

Auth standard docs:
- [AUTH_SYSTEM.md](/Users/ted/dev/onlystack/docs/AUTH_SYSTEM.md) defines the canonical backend contract, frontend state machine, deep-link model, mock-mode auth, and non-email environment behavior.
- [STARTER_KIT_EXTRACTION_AUDIT.md](/Users/ted/dev/onlystack/docs/STARTER_KIT_EXTRACTION_AUDIT.md) captures the broader systems that should live in the starter kit.
- [EXTRACTION_DETAILED_AUDIT.md](/Users/ted/dev/onlystack/docs/EXTRACTION_DETAILED_AUDIT.md) records which patterns are actually strongest in `univiirse`, `goala`, `sitches`, and `fitkind`, and which ones should not be copied blindly.

### Design System
- Sentinel-generated design tokens (colours, spacing, typography, radius)
- Dark mode first
- Components: AppButton, AppCard, AppTextField, EmptyStateView, LoadingView
- Toast / Snackbar notification system with auto-dismiss
- Shimmer skeleton loading
- Haptics helper (iOS)
- `SignInWithAppleButton` (UIViewRepresentable) + `SignInWithGoogleButton` (SwiftUI)

### NFRs
- Offline banner (NWPathMonitor on iOS / ConnectivityManager on Android)
- Force update screen (blocking, undismissable) + soft update banner
- In-app review prompt (SKStoreReviewController / Play In-App Review)
- Crashlytics stub (Firebase, ready to configure)

### Subscriptions
- `SubscriptionManager` stub on both platforms (RevenueCat-shaped API)
- `SubscriptionTier` enum (FREE / TRACKER)
- `TrackerFeature` gating (free features always return `true`)
- `PaywallView` (iOS) with trial CTA + restore
- TODOs for adding RevenueCat SDK

### Backend API
- Magic link auth with BullMQ email queue
- `POST /auth/social` endpoint (Apple + Google — verify TODOs)
- Rate limiting (`@nestjs/throttler`, 5 req/60s on auth endpoints)
- OpenAPI / Swagger (`/api/docs`)
- Config validation with joi (fail-fast on missing env vars)
- Global exception filter — structured error envelope, Prisma error mapping
- Correlation ID middleware (`X-Correlation-ID` on every request/response)
- Logging interceptor (`METHOD /path → status [Xms]`)
- Pagination helpers: cursor-based + offset, opaque base64 cursor, `paginateQuery<T>`
- Soft delete Prisma extension (auto-injects `deletedAt: null` on all queries)
- Admin role (`UserRole` enum) + `AdminGuard` + `GET /admin/users` + `GET /admin/stats`
- Push notification stub module (FCM-ready)
- App version enforcement endpoint (`GET /app/version-check`)

### CRUD Resource Template

`apps/starter-api/src/_templates/resource/` — copy-paste scaffold for any new domain:

```
resource/
├── entity.ts          # Prisma-mapped class
├── create-resource.dto.ts
├── update-resource.dto.ts
├── resource-response.dto.ts
├── resource.service.ts  # cursor pagination + soft delete included
├── resource.controller.ts
├── resource.module.ts
├── resource.service.spec.ts
└── README.md
```

### Infrastructure

| Tool | Location | Purpose |
|------|----------|---------|
| Docker Compose | `docker-compose.yml` | Local postgres 16 + redis 7 |
| Dockerfile | `apps/starter-api/Dockerfile` | Multi-stage, non-root, health check |
| Railway guide | `infra/RAILWAY.md` | Standard API deploy path |
| Hosting doctrine | `docs/HOSTING.md` | Railway + Cloudflare R2 + web hosting defaults |
| Deploy workflow | `.github/workflows/deploy.yml` | Manual Railway deploy (staging / production) |

### Testing

| Layer | Tool | Location |
|-------|------|----------|
| Backend unit | Jest | `apps/starter-api/src/**/*.spec.ts` |
| Backend E2E | Supertest | `apps/starter-api/test/` |
| iOS unit | XCTest | `apps/starter-ios/StarterAppTests/` |
| iOS E2E | Maestro | `apps/starter-ios/e2e/` |
| Android unit | JUnit 5 + MockK | `apps/starter-android/app/src/test/` |
| Android E2E | Detox | `apps/starter-android/e2e/` |
| Web public | Astro | `apps/starter-web-public/` |
| Web admin | Next.js | `apps/starter-web-admin/` |
| Web app | Next.js | `apps/starter-web-app/` |
| Load tests | k6 | `load-tests/` |
| Mutation tests | Stryker | `apps/starter-api/stryker.conf.json` |

**k6 load test profiles:**
- `npm run smoke` — 1 VU, 30s (sanity check)
- `npm run load` — ramp to 50 VUs over 1 min, hold 3 min
- `npm run stress` — ramp to 200 VUs (observation mode)

### Developer Experience
- `scripts/rename.sh` — interactive project rename (name + bundle ID)
- `scripts/start-local.sh` — one-command local stack (Docker + migrations + seed)
- `scripts/reset-local.sh` — destructive local reset with confirmation prompt
- Sentinel schema validation + code generation
- Claude Code hooks (block generated file edits, warn on dangerous git)
- Fastlane (iOS: TestFlight + App Store lanes; Android: Play Store internal + production)
- GitHub Actions CI (backend + iOS + Android, on push to main + PRs)
- GitHub Actions deploy (manual `workflow_dispatch`, staging + production)
- Dependabot (npm, Swift packages, Gradle, GitHub Actions — weekly)
- Pre-commit hook: runs `npx sentinel schema:validate` before every commit
- Postman collection with token-saving test scripts (`apps/starter-api/postman/`)
- Stryker mutation testing config (targeting auth + common)

---

## Environments

| Env | iOS Scheme | Android Flavour | API URL |
|-----|-----------|-----------------|---------|
| Dev | StarterApp-Dev | dev | `http://localhost:3000/api/v1` (Android: `http://10.0.2.2:3000/api/v1`) |
| Mock | StarterApp-Dev + `-UITestMode` | mock | fixture-backed runtime transport |
| Staging | StarterApp-Staging | staging | `https://api-staging.yourapp.com/api/v1` |
| Production | StarterApp-Release | prod | `https://api.yourapp.com/api/v1` |

Update URLs in `apps/starter-ios/Configs/*.xcconfig` and `apps/starter-android/app/build.gradle.kts`.

## Auth Environment Modes

Starter-kit auth is expected to run in three modes:

- `AUTH_DELIVERY_MODE=email`
  - production/staging email delivery
- `AUTH_DELIVERY_MODE=console`
  - local development and CI; backend logs the code and link token instead of sending email
- `AUTH_DELIVERY_MODE=disabled`
  - challenge creation without delivery, for tightly controlled fixture flows

Optional non-production bypass variables:

- `AUTH_DEV_BYPASS_ENABLED`
- `AUTH_DEV_BYPASS_EMAIL`
- `AUTH_DEV_BYPASS_CODE`
- `AUTH_DEV_BYPASS_LINK_TOKEN`

Recommended local setup:

```bash
AUTH_DELIVERY_MODE=console
AUTH_LINK_BASE_URL=appstarterkit://auth/verify
AUTH_DEV_BYPASS_ENABLED=true
AUTH_DEV_BYPASS_EMAIL=reviewer@yourapp.com
AUTH_DEV_BYPASS_CODE=11112222
AUTH_DEV_BYPASS_LINK_TOKEN=dev-review-link-token
```

How auth should work in each environment:

- real email environments
  - request code normally
  - email contains both a manual code and a one-tap deep link
- local/CI without email delivery
  - backend returns and logs `code`, `linkToken`, and `linkUrl`
  - mobile/web can use the same auth UI and either type the code or open the deep link
- review/demo environments
  - use the allowlisted bypass credentials above
  - clients still hit the normal auth endpoints and receive the normal auth envelope

## Starter Systems

The canonical cross-cutting systems that should be reused across products are
documented in [docs/STARTER_KIT_SYSTEMS.md](docs/STARTER_KIT_SYSTEMS.md).

This includes:

- deep-link routing conventions
- fixture-backed mock transport
- screenshot proof loop with `simemu` + Maestro + Sentinel
- force update
- offline monitor/banner
- review prompt manager
- common backend module
- push notification skeleton
- subscription/paywall skeleton
- Sentinel contract layer
- notification preferences data structure
- media upload/read
- networking library
- `AUTH_REVIEW_EMAIL`
- `AUTH_REVIEW_CODE`

Rules:

- bypass must never be active in production
- mock mode must not fork UI logic
- auth deep links should always exercise the same frontend state machine as production

---

## Project Structure

```
onlystack/
├── apps/starter-api/
│   ├── src/
│   │   ├── auth/             # Magic link auth + social auth + JWT
│   │   ├── user/             # User profile
│   │   ├── admin/            # Admin-only endpoints
│   │   ├── health/           # GET /health
│   │   ├── app-version/      # Force update check
│   │   ├── notification/     # FCM push stub
│   │   ├── common/           # Filters, interceptors, middleware, pagination
│   │   ├── config/           # Joi-validated config schema
│   │   ├── prisma/           # PrismaService + soft-delete extension
│   │   └── _templates/resource/  # CRUD scaffold template
│   ├── prisma/schema.prisma
│   ├── postman/              # Postman collection + environment
│   ├── Dockerfile
│   └── stryker.conf.json
├── apps/starter-ios/
│   ├── project.yml
│   ├── Configs/              # xcconfig per env
│   ├── fastlane/
│   ├── e2e/                  # Maestro flows
│   └── StarterApp/
│       ├── App/              # Entry point, RootView, AppState
│       ├── Features/
│       │   ├── Auth/         # Welcome → Email → Code + SocialAuth/
│       │   ├── Home/
│       │   └── More/         # ProfileView, PaywallView
│       ├── DesignSystem/     # Tokens + Components
│       └── Core/             # APIClient, Network, Keychain, Biometric, Subscription
├── apps/starter-android/
│   ├── app/src/main/kotlin/com/onlystack/starterapp/
│   │   ├── app/              # Application + MainActivity
│   │   ├── features/auth/    # Auth flow
│   │   ├── features/more/    # ProfileScreen
│   │   ├── design/           # Tokens + Components
│   │   └── core/             # Network, Auth, Biometric, Subscription, DeepLink
│   ├── fastlane/
│   └── e2e/                  # Detox tests
├── apps/starter-web-public/  # Astro public site starter
├── apps/starter-web-admin/   # Next.js admin starter
├── apps/starter-web-app/     # Next.js authenticated app starter
├── infra/terraform/          # GCP infrastructure
├── load-tests/               # k6 smoke/load/stress
├── sentinel/                 # Schema source of truth
│   └── schemas/
├── scripts/
│   ├── rename.sh
│   ├── start-local.sh
│   └── reset-local.sh
├── docker-compose.yml
├── SETUP.md                  # Full setup walkthrough
└── docs/ARCHITECTURE.md      # Auth flow, token lifecycle, module map
```

---

## Customising for a New Project

1. **Run** `./scripts/rename.sh "YourApp" "com.yourcompany.yourapp"`
2. **Colours**: update `AppTokens.Color.primary` (iOS) and `AppColors.Primary` (Android)
3. **API URLs**: update `apps/starter-ios/Configs/*.xcconfig` and `apps/starter-android/app/build.gradle.kts`
4. **App Store / Play Store IDs**: search `YOUR_APP_ID` in `HardUpdateView.swift` / `ForceUpdateComponents.kt`
5. **Email**: implement `apps/starter-api/src/email/email.processor.ts` with your SMTP provider
6. **Social auth**: follow TODO comments in `AppleSignInHelper.swift`, `GoogleSignInHelper.swift`, `GoogleSignInHelper.kt`, and `apps/starter-api/src/auth/auth.service.ts`
7. **Web strategy**: see [WEB_STARTER_STRATEGY.md](/Users/ted/dev/onlystack/docs/WEB_STARTER_STRATEGY.md)
7. **Subscriptions**: follow TODO comments in `SubscriptionManager.swift` / `SubscriptionManager.kt` for RevenueCat SDK
8. **Firebase**: add `google-services.json` (Android) / `GoogleService-Info.plist` (iOS) and uncomment Crashlytics dependencies
9. **Terraform**: fill in `infra/terraform/variables.tf` and `envs/staging.tfvars` with your GCP project
