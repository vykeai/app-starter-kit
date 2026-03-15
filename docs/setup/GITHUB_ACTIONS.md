# GitHub Actions Setup Guide

Workflow templates are in `.github/workflows/`. They are configured with `on: workflow_dispatch` only — meaning they will **not** run automatically until you opt in by adding triggers.

---

## Step 1: Activate workflows

To enable automatic runs on every push to `main` and on pull requests, edit each workflow file and add triggers:

```yaml
on:
  workflow_dispatch:       # keep manual trigger
  push:
    branches: [main]
  pull_request:
    branches: [main]
```

---

## Step 2: Backend workflow (`backend.yml`)

### What it does
- Installs Node.js 20
- Runs `npm install`
- Runs `npm run lint`
- Runs `npx prisma validate`
- Runs `npx tsc --noEmit`
- Runs `npm test`

### Required secrets
None for lint/type-check. For integration tests against a real DB, add:

| Secret | Value |
|--------|-------|
| `DATABASE_URL` | `postgresql://postgres:postgres@localhost:5432/app_test` |

The workflow already uses a `postgres` service container — no external DB needed.

### Postgres service (add to job)
```yaml
services:
  postgres:
    image: postgres:15-alpine
    env:
      POSTGRES_DB: app_test
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - 5432:5432
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

---

## Step 3: iOS workflow (`ios.yml`)

### What it does
- Runs on `macos-14` (Apple Silicon, Xcode 15)
- Installs XcodeGen via Homebrew
- Runs `xcodegen generate`
- Builds `StarterApp-Dev` scheme for iOS Simulator
- Runs unit tests

### Required secrets
None for simulator builds. For TestFlight distribution add:

| Secret | Description |
|--------|-------------|
| `APP_STORE_CONNECT_API_KEY_ID` | App Store Connect API key ID |
| `APP_STORE_CONNECT_API_ISSUER_ID` | Issuer ID |
| `APP_STORE_CONNECT_API_KEY_BASE64` | P8 key content, base64-encoded |
| `DISTRIBUTION_CERTIFICATE_BASE64` | Distribution cert, base64-encoded |
| `DISTRIBUTION_CERTIFICATE_PASSWORD` | Cert password |
| `PROVISIONING_PROFILE_BASE64` | Provisioning profile, base64-encoded |

### Recommended runner
`macos-14` — Apple Silicon, Xcode 15.x pre-installed.

### Cache key
```yaml
- uses: actions/cache@v4
  with:
    path: ~/Library/Developer/Xcode/DerivedData
    key: ${{ runner.os }}-xcode-${{ hashFiles('ios/project.yml') }}
```

---

## Step 4: Android workflow (`android.yml`)

### What it does
- Runs on `ubuntu-latest`
- Sets up JDK 17
- Runs `./gradlew assembleDevDebug assembleStagingDebug`
- Runs `./gradlew test`

### Required secrets
None for debug builds. For Play Store distribution add:

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Release keystore, base64-encoded |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

### Cache key
```yaml
- uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('android/**/*.gradle.kts', 'android/gradle/libs.versions.toml') }}
```

---

## Step 5: Branch protection (recommended)

In GitHub → Settings → Branches → Add rule for `main`:

- [x] Require a pull request before merging
- [x] Require status checks to pass before merging
  - Add: `backend`, `ios`, `android` (the job names from each workflow)
- [x] Require branches to be up to date before merging
- [x] Do not allow bypassing the above settings

---

## Workflow file locations

```
.github/workflows/
├── backend.yml    # NestJS: lint + typecheck + test
├── ios.yml        # iOS: xcodegen + build + test
└── android.yml    # Android: assemble + test
```

All three are **manual-trigger only** by default. Follow Step 1 above to activate.
