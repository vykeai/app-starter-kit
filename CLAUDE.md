# app-starter-kit — Development Rules

> Production-ready mobile app starter: NestJS + iOS (SwiftUI) + Android (Compose).
> Magic link auth, design tokens, force update, offline-first.
> Built as a template — rename `AppStarterKit` / `starter.app` to your app when using.

---

## Tech Stack

| Platform | Language | UI | DB | Min Version |
|----------|----------|----|----|-------------|
| Backend API | TypeScript | NestJS 10 | PostgreSQL (Prisma 5) | Node 20+ |
| iOS | Swift | SwiftUI | Keychain | iOS 17.0 |
| Android | Kotlin | Jetpack Compose | Room | API 26 (Android 8.0) |

**Backend services:** Redis (BullMQ email queue) · Swagger at `/api/docs`

---

## Directory Structure

```
app-starter-kit/
├── backend/                  # NestJS API
│   ├── src/
│   │   ├── auth/             # Magic link: request + verify + refresh + logout
│   │   ├── user/             # GET /user/me, PATCH /user/me
│   │   ├── health/           # GET /health
│   │   ├── app-version/      # GET /app/version-check (force update)
│   │   └── prisma/           # Global PrismaService
│   ├── prisma/schema.prisma  # User, MagicLink, RefreshToken, UserPreferences, AppVersion
│   └── docker-compose.yml    # postgres:15 + redis:7
├── ios/
│   ├── project.yml           # XcodeGen — 3 schemes: Dev, Staging, Release
│   ├── Configs/              # xcconfig per scheme (API_BASE_URL, ENVIRONMENT)
│   └── AppStarterKit/
│       ├── App/              # AppStarterKit.swift, AppState.swift, RootView.swift
│       ├── Features/Auth/    # WelcomeView, EmailInputView, CodeEntryView (8-digit)
│       ├── Features/Home/    # HomeView placeholder
│       ├── DesignSystem/     # AppTokens.swift, AppButton, AppCard, AppTextField, etc.
│       └── Core/             # APIClient, NetworkMonitor, KeychainHelper, NFRs
├── android/
│   └── app/src/main/kotlin/com/starter/app/
│       ├── app/              # AppStarterKit (Hilt), MainActivity
│       ├── features/auth/    # AuthNavHost, AuthViewModel, screens (Welcome → Email → Code)
│       ├── features/home/    # HomeScreen placeholder
│       ├── design/           # AppTokens, AppButton, AppCard, AppTextField, etc.
│       ├── core/             # ApiClient, NetworkMonitor, SecurePreferences, AppDatabase, DI
│       └── nfr/              # ForceUpdateViewModel, ForceUpdateComponents, ReviewManager
├── sentinel/schemas/         # Design tokens, feature flags (source of truth)
├── docs/setup/               # Setup guides (GitHub Actions, deployment, etc.)
└── .github/workflows/        # CI templates (manual trigger only — see docs/setup/GITHUB_ACTIONS.md)
```

---

## Platform Naming (Never Deviate)

| Platform | Directory | In code |
|----------|-----------|---------|
| Backend API | `backend/` | API |
| iOS | `ios/` | iOS |
| Android | `android/` | Android |

---

## Auth Flow

Magic link with 8-digit OTP. Never passwords.

```
POST /api/v1/auth/magic-link/request  { email }
  → generates 8-digit code, stores MagicLink (15min expiry), queues email job

POST /api/v1/auth/magic-link/verify   { email, code }
  → marks MagicLink used, returns { accessToken (15m), refreshToken (30d), user }

POST /api/v1/auth/refresh             { refreshToken }
  → returns { accessToken }

POST /api/v1/auth/logout              { refreshToken }  [requires JWT]
  → revokes RefreshToken
```

Token storage:
- **iOS**: Keychain via `KeychainHelper`
- **Android**: `EncryptedSharedPreferences` via `SecurePreferences`

---

## Coding Conventions

### Backend (TypeScript / NestJS)
- Module-per-domain architecture
- Prisma for all DB access — no raw SQL
- `class-validator` + `class-transformer` on all DTOs
- `ConfigModule.forRoot({ isGlobal: true })` — env vars via `process.env`
- Never commit `.env` — only `.env.example`

### iOS (Swift / SwiftUI)
- MVVM with `@Observable` macro
- `@Environment` for DI (AppState passed down from root)
- `async/await` everywhere — no Combine
- All tokens from `AppTokens` — never hardcode colours or sizes
- URLProtocol for test-time HTTP interception (see `AppStarterKitTests/Helpers/MockURLProtocol.swift`)

### Android (Kotlin / Compose)
- MVVM with `ViewModel` + `StateFlow`
- Hilt for DI throughout
- Coroutines + Flow — no RxJava
- All tokens from `AppColors` / `AppSpacing` / `AppRadius`
- MockWebServer for test-time HTTP interception

---

## NFRs

### Force Update
- Backend: `GET /api/v1/app/version-check?platform=ios&version=1.0.0`
- Returns `{ isUpdateRequired, isUpdateRecommended, minimumVersion, latestVersion }`
- iOS: `ForceUpdateChecker` → `HardUpdateView` (blocking) or `SoftUpdateBanner` (dismissable)
- Android: `ForceUpdateViewModel` → `HardUpdateScreen` or `SoftUpdateBanner`
- **Silent on failure** — a failed version check never blocks the user

### Offline Indicator
- iOS: `NetworkMonitor` (NWPathMonitor) → `OfflineBanner` overlay
- Android: `NetworkMonitor` (ConnectivityManager Flow) → `OfflineBanner` composable
- Auto-dismisses when connectivity restored

### App Review Prompt
- iOS: `ReviewManager` → `SKStoreReviewController.requestReview`
- Android: `ReviewManager` → Play In-App Review API
- 7-day cooldown between prompts — stored in `UserDefaults` / `SharedPreferences`

---

## Build Environments

Three environments on every platform:

| Env | iOS Scheme | Android Flavour |
|-----|-----------|-----------------|
| Dev | AppStarterKit-Dev | dev |
| Staging | AppStarterKit-Staging | staging |
| Prod | AppStarterKit-Release | prod |

API URLs set in xcconfig (iOS) and `buildConfigField` (Android).

---

## CI/CD

Workflow files in `.github/workflows/` use **manual trigger only** (`on: workflow_dispatch`).
See `docs/setup/GITHUB_ACTIONS.md` to activate automatic runs and add secrets.

---

## Renaming for a New Project

When using this as a template:

1. Find/replace `AppStarterKit` → your app name
2. Find/replace `starter.app` → your bundle ID prefix
3. Find/replace `com.appstarterkit.app` → your Android package name
4. Update colours in `AppTokens.swift` (iOS) and `AppTokens.kt` (Android)
5. Update API URLs in xcconfig files and `build.gradle.kts`
6. Replace `YOUR_APP_ID` in `HardUpdateView.swift` / `ForceUpdateComponents.kt` with real App Store / Play Store IDs
7. Implement `email.processor.ts` with real SMTP or transactional email provider (Postmark, Resend, SendGrid)

---

## Testing

### iOS
- `URLProtocol` mock infra: `AppStarterKitTests/Helpers/MockURLProtocol.swift`
- Register with `URLProtocol.registerClass(MockURLProtocol.self)` in test setUp

### Android
- `MockWebServer` for HTTP interception
- `MockK` for unit mocking
- JUnit5 for all tests

### Backend
- `Jest` + `@nestjs/testing` for unit tests
- Supertest for endpoint tests

---

## Before Every Task

```bash
npx sentinel schema:validate
```

```bash
# Install pre-commit hook (one-time):
cp scripts/pre-commit .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `backend/prisma/schema.prisma` | DB schema — User, MagicLink, RefreshToken, UserPreferences, AppVersion |
| `backend/src/auth/auth.service.ts` | Magic link logic |
| `backend/.env.example` | All required environment variables |
| `ios/project.yml` | XcodeGen project definition |
| `ios/Configs/*.xcconfig` | Per-environment config (API URL, bundle ID) |
| `ios/AppStarterKit/Core/NFR/` | Force update, offline banner, review prompt |
| `android/gradle/libs.versions.toml` | All Android dependency versions |
| `android/app/build.gradle.kts` | Flavour definitions |
| `android/app/src/main/kotlin/.../nfr/` | Force update, offline banner, review prompt |
| `sentinel/schemas/design/tokens.json` | Design token source of truth |
| `docs/setup/GITHUB_ACTIONS.md` | CI setup guide |

---

## Simulator / Emulator Management (simemu)

**simemu is the ONLY permitted way to interact with iOS simulators and Android emulators.**
`simemu` is installed at `~/dev/simemu`. A global hook blocks all direct `xcrun simctl`, `adb`, `emulator`, and `avdmanager` calls.

This is a template project — simulators are acquired per-use by the operator, not pre-assigned. Before doing any simulator work, ask which slug to use or check `simemu status`.

```bash
# Check what's available and reserved
simemu list ios
simemu list android
simemu status

# ── Headless by default ────────────────────────────────────────────────────
# Android boots without a window. iOS window stays behind other apps.
# Add --window to boot/acquire only when you need to watch directly.
# PROVE DELIVERABLES WITH SCREENSHOTS — never verbal claims:
#   simemu screenshot <slug> -o /tmp/<platform>_<feature>.png
#   (read the file to verify visually before committing)
# ───────────────────────────────────────────────────────────────────────────

# Use whatever slug the operator has assigned
simemu install <slug> path/to/App.app           # iOS (.app or .ipa)
simemu install <slug> path/to/app.apk           # Android
simemu launch <slug> com.example.app
simemu terminate <slug> com.example.app
simemu clear-data <slug> com.example.app        # Android: reset app data
simemu screenshot <slug> -o /tmp/screen.png --max-size 1000
# Maestro (navigation only — always use simemu screenshot to capture)
simemu animations <ios-slug> off               # disable before Maestro for stability
simemu animations <android-slug> off
simemu maestro <ios-slug>     /tmp/flow.yaml   # maestro --device <udid> test /tmp/flow.yaml
simemu maestro <android-slug> /tmp/flow.yaml   # maestro --device <serial> test /tmp/flow.yaml
simemu animations <ios-slug> on                # restore when done
simemu status-bar <slug> --time "9:41" --battery 100 --wifi 3
simemu status-bar <slug> --clear
simemu tap <slug> 195 400
simemu swipe <slug> 195 700 195 200             # swipe up
simemu long-press <slug> 195 400
simemu rotate <slug> landscape
simemu rotate <slug> portrait
simemu appearance <slug> dark
simemu appearance <slug> light
simemu key <slug> home
simemu key <ios-slug> paste                         # paste clipboard into focused field (Cmd+V)
simemu reboot <slug>
simemu focus <ios-slug>                             # bring iOS Simulator window to front
simemu biometrics <slug> match
simemu privacy <slug> grant com.example.app camera
simemu location <slug> 37.7749 -122.4194
simemu network <android-slug> airplane              # airplane | all | wifi | data | none (Android)
simemu battery <android-slug> --level 85            # fake battery % for screenshots (Android)
simemu battery <android-slug> --reset               # restore real battery
simemu input <ios-slug> "hello world"               # sets iOS clipboard — then: key paste to type
simemu input <android-slug> "hello world"           # types directly into focused field
simemu add-media <ios-slug> photo.jpg               # add photo/video to iOS Photos library
simemu add-media <android-slug> photo.jpg           # add photo/video to Android Gallery
simemu log <slug>
simemu env <slug>
```

If no slug is assigned yet, you may acquire one from the free pool for your work:

```bash
simemu list ios
simemu list android

# Acquire with descriptive slug: manifest-{purpose}  (or your project name)
simemu acquire ios starter-ios --device "Soba iPhone16 6.1in iOS18" --wait 120
simemu acquire android starter-android --device "Biscuit MedPhone 6.3in API35" --wait 120

# Always release when done
simemu release starter-ios
simemu release starter-android
```

**Rules — no exceptions:**
- **NEVER** call `xcrun simctl` or `adb` directly — the hook will block it
- If nothing is free and `--wait` times out, tell the user

---

## Screen Catalog — setup guide

When building a real app from this template, configure the screen catalog in `sentinel.yaml`. The catalog generates platform-specific screenshots in light/dark mode (plus iOS 26 glossy variants) for every screen. Luke reviews all screens from `catalog/index.html` before approving UI work.

Update the `catalog:` section in `sentinel.yaml` with your project's slugs and bundle IDs:
```yaml
catalog:
  output: catalog/
  resize: 1000
  ios18:
    slug: {your-app}-ios          # matches simemu acquire slug
    app_id: {your.bundle.id}
  ios26:
    slug: {your-app}-ios26
    app_id: {your.bundle.id}
    glossy: true
  android:
    slug: {your-app}-android      # matches simemu acquire slug
    app_id: {your.android.package}
  screens:
    - slug: welcome
      flow: flows/welcome.yaml
    - slug: sign-in
      flow: flows/sign-in.yaml
    - slug: home
      flow: flows/home.yaml
```

Commands:
```bash
sentinel catalog:capture       # capture all screens
sentinel catalog:validate      # verify all shots exist
sentinel catalog:index         # regenerate catalog/index.html
```

Rules:
1. Every screen added to the app must have an entry in `sentinel.yaml → screens`
2. `sentinel catalog:validate` must pass before marking any screen task done
3. Luke reviews completed screens from `catalog/index.html`
