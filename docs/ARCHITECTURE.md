# Architecture

## Overview

Onlystack is a monorepo containing the starter app plus shared reusable layers.

- **iOS** — SwiftUI + `@Observable` MVVM, Keychain storage, iOS 17+
- **Android** — Jetpack Compose + ViewModel + StateFlow, Hilt DI, API 26+
- **Backend** — NestJS + Prisma + PostgreSQL, BullMQ + Redis for async jobs

There is no shared code layer. Each platform independently reimplements business logic. Sentinel schemas are the machine-readable source of truth that enforces consistency across platforms without sharing runtime code.

```
sentinel/schemas/
  ├── design/tokens.json          →  apps/starter-ios/.../DesignSystem/Tokens/AppTokens.swift  (generated)
  │                               →  apps/starter-android/.../design/tokens/AppTokens.kt        (generated)
  ├── platform/feature-flags.json →  apps/starter-ios/.../Core/FeatureFlags.swift               (generated)
  │                               →  apps/starter-android/.../core/FeatureFlags.kt              (generated)
  ├── platform/navigation.json    →  (validated — not generated)
  └── features/*.json             →  (validated — informs implementation)
```

Generated files must never be hand-edited. Edit the schema and run `npx sentinel schema:generate`.

---

## Directory structure

```
onlystack/
├── apps/starter-api/                  # NestJS API
│   ├── src/
│   │   ├── auth/             # Magic link: request + verify + refresh + logout
│   │   ├── user/             # GET /user/me, PATCH /user/me
│   │   ├── health/           # GET /health
│   │   ├── app-version/      # GET /app/version-check (force update)
│   │   └── prisma/           # Global PrismaService
│   ├── prisma/schema.prisma  # User, MagicLink, RefreshToken, UserPreferences, AppVersion
│   └── docker-compose.yml    # postgres:15 + redis:7
├── apps/starter-ios/
│   ├── project.yml           # XcodeGen — 3 schemes: Dev, Staging, Release
│   ├── Configs/              # xcconfig per scheme (API_BASE_URL, ENVIRONMENT)
│   └── StarterApp/
│       ├── App/              # StarterApp.swift, AppState.swift, RootView.swift
│       ├── Features/Auth/    # WelcomeView, EmailInputView, CodeEntryView
│       ├── Features/Home/    # HomeView placeholder
│       ├── DesignSystem/     # AppTokens.swift (generated), components
│       └── Core/             # APIClient, NetworkMonitor, KeychainHelper, NFRs
├── apps/starter-android/
│   └── app/src/main/kotlin/com/onlystack/starterapp/
│       ├── app/              # Application (Hilt), MainActivity
│       ├── features/auth/    # AuthNavHost, AuthViewModel, screens
│       ├── features/home/    # HomeScreen placeholder
│       ├── design/           # AppTokens.kt (generated), components
│       ├── core/             # ApiClient, NetworkMonitor, SecurePreferences, DI
│       └── nfr/              # ForceUpdateViewModel, ReviewManager
├── sentinel/schemas/         # Source of truth (see Schema-driven workflow below)
├── scripts/                  # rename.sh, pre-commit, validate-fixtures.js
├── docs/                     # Architecture, setup guides
└── .github/workflows/        # CI (manual trigger by default)
```

---

## Backend module structure

| Module | Responsibility | Key files |
|--------|---------------|-----------|
| `auth` | Magic link generation, OTP verification, JWT issuance, refresh, logout | `auth.service.ts`, `auth.controller.ts`, `magic-link.strategy.ts` |
| `user` | User profile read and update | `user.service.ts`, `user.controller.ts` |
| `app-version` | Force update / soft update version check | `app-version.service.ts`, `app-version.controller.ts` |
| `health` | Liveness probe for load balancers and CI | `health.controller.ts` |
| `prisma` | Global Prisma client singleton | `prisma.service.ts` |

All modules use `class-validator` + `class-transformer` on DTOs. Config is loaded via `ConfigModule.forRoot({ isGlobal: true })` — environment variables are validated at startup and the process exits if required vars are missing.

The email queue uses **BullMQ** backed by Redis. The `auth` module enqueues an `email.send` job when a magic link is requested. The email processor (`email.processor.ts`) consumes the queue — wire in your SMTP or transactional email provider (Postmark, Resend, SendGrid) there.

---

## Authentication flow

The starter uses magic link (email OTP) authentication. There are no passwords.

```
1.  Client         POST /api/v1/auth/magic-link/request  { email }
2.  Server         Generates 8-digit code via crypto.randomInt
                   Stores MagicLink row (15-minute expiry)
                   Enqueues email job via BullMQ
3.  BullMQ worker  Sends email with the 8-digit code
4.  User           Reads code from email
5.  Client         POST /api/v1/auth/magic-link/verify   { email, code }
6.  Server         Validates code, marks MagicLink used
                   Returns { accessToken (15m), refreshToken (30d), user }
7.  Client         Stores accessToken + refreshToken securely
                   iOS:     Keychain via KeychainHelper
                   Android: EncryptedSharedPreferences via SecurePreferences
8.  Client         Attaches accessToken as Bearer on every API request
9.  Server → 401   Client calls POST /api/v1/auth/refresh { refreshToken }
                   Receives new { accessToken }
                   Retries the original request transparently
10. Logout         Client POST /api/v1/auth/logout { refreshToken }   [requires JWT]
                   Server revokes the RefreshToken row
                   Client clears stored tokens
```

### Deeplink (tap-to-login)

When the backend sends a magic link email, it includes a deeplink URL in the format:

```
yourapp://auth/verify?code=12345678
```

The iOS `AppDelegate` / Android `MainActivity` intercepts the URL and navigates the user directly to `CodeEntryScreen` with the code pre-populated.

---

## Token lifecycle

| Token | TTL | Storage | Rotation |
|-------|-----|---------|----------|
| Access token (JWT) | 15 minutes | Memory only (not persisted) | Issued fresh on each `/auth/refresh` call |
| Refresh token | 30 days | Keychain (iOS) / EncryptedSharedPreferences (Android) | Replaced on each use (rotation) |

On logout, the refresh token is revoked server-side. The access token expires naturally within 15 minutes — no server-side access token revocation list is maintained.

---

## Offline / network handling

**iOS** — `NetworkMonitor` wraps `NWPathMonitor` and publishes a `isConnected: Bool` via `@Observable`. `RootView` overlays `OfflineBanner` when `isConnected` is false. `APIClient` does not queue offline requests — callers receive an error and are expected to surface it or retry when connectivity is restored.

**Android** — `NetworkMonitor` wraps `ConnectivityManager` and exposes a `isConnected: StateFlow<Boolean>`. `OfflineBanner` is a Compose composable that observes this flow and renders an amber banner at the top of the screen. It auto-dismisses when connectivity is restored.

Both implementations auto-hide the banner within one second of network restoration.

---

## Force update / soft update

Backend endpoint: `GET /api/v1/app/version-check?platform=ios&version=1.0.0`

Response:

```json
{
  "isUpdateRequired": false,
  "isUpdateRecommended": true,
  "minimumVersion": "1.0.0",
  "latestVersion": "1.2.0"
}
```

| Case | iOS | Android |
|------|-----|---------|
| `isUpdateRequired: true` | `HardUpdateView` — full-screen, undismissable | `HardUpdateScreen` |
| `isUpdateRecommended: true` | `SoftUpdateBanner` — dismissable top banner | `SoftUpdateBanner` |
| Both false | No UI shown | No UI shown |
| Request fails | Silent — no UI shown | Silent — no UI shown |

The version check runs on app foreground. Failure never blocks the user.

---

## Schema-driven workflow

Sentinel schemas in `sentinel/schemas/` are the single source of truth for design tokens, feature flags, and navigation structure. The workflow for any cross-platform change is:

1. **Edit** the relevant schema in `sentinel/schemas/`
2. **Run** `npx sentinel schema:generate` to regenerate platform files
3. **Implement** the feature using generated types and tokens
4. **Commit** schema + generated files + implementation together in a single commit

Never hand-edit generated files:

| Generated file | Source schema |
|---------------|---------------|
| `apps/starter-ios/.../DesignSystem/Tokens/AppTokens.swift` | `sentinel/schemas/design/tokens.json` |
| `apps/starter-ios/.../Core/FeatureFlags.swift` | `sentinel/schemas/platform/feature-flags.json` |
| `apps/starter-ios/.../Core/Models/Models.swift` | `sentinel/schemas/models/*.json` |
| `apps/starter-ios/.../Core/Network/APIClientProtocol.swift` | `sentinel/schemas/features/*.json` |
| `apps/starter-android/.../design/tokens/AppTokens.kt` | `sentinel/schemas/design/tokens.json` |
| `apps/starter-android/.../core/FeatureFlags.kt` | `sentinel/schemas/platform/feature-flags.json` |
| `apps/starter-android/.../core/models/Models.kt` | `sentinel/schemas/models/*.json` |
| `apps/starter-android/.../core/network/APIClient.kt` | `sentinel/schemas/features/*.json` |

Validate schemas before every commit:

```bash
npx sentinel schema:validate
```

---

## Adding a new feature

1. Create `sentinel/schemas/features/my-feature.json` with the feature schema
2. Add any new user-facing strings to `sentinel/schemas/design/strings.json`
3. Run `npx sentinel schema:generate` to regenerate platform files
4. Add any new feature flags to `sentinel/schemas/platform/feature-flags.json` and regenerate
5. Implement the feature on each platform using the generated types and tokens
6. Add fixture JSON files for any new API endpoints
7. Write unit tests (ViewModel / service) and contract tests (fixture validation)
8. Commit: schema + generated files + implementation + tests together

---

## Build environments

Three environments exist on every platform:

| Environment | iOS Scheme | Android Flavour | API Base URL |
|-------------|-----------|-----------------|--------------|
| Dev | StarterApp-Dev | devDebug | `http://localhost:3000/api/v1` (iOS) / `http://10.0.2.2:3000/api/v1` (Android emulator) |
| Staging | StarterApp-Staging | stagingDebug | `https://api-staging.yourapp.com/api/v1` |
| Production | StarterApp-Release | prodRelease | `https://api.yourapp.com/api/v1` |

iOS API URLs are set in `apps/starter-ios/Configs/Dev.xcconfig`, `Staging.xcconfig`, `Release.xcconfig`.
Android API URLs are set via `buildConfigField` in `apps/starter-android/app/build.gradle.kts`.

---

## CI

Workflow files live in `.github/workflows/`. All three workflows use `on: workflow_dispatch` (manual trigger only) by default. See `docs/setup/GITHUB_ACTIONS.md` to activate automatic runs on push and pull requests.

| Workflow | Runner | What it does |
|----------|--------|-------------|
| `backend.yml` | ubuntu-latest | lint, typecheck, unit tests, E2E tests, fixture validation |
| `ios.yml` | macos-14 | xcodegen, build (Dev Debug), unit tests |
| `android.yml` | ubuntu-latest | assemble dev/staging/prod, unit tests |
