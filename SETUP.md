# Setup Guide

Get from `git clone` to a running app in ~15 minutes.

---

## Prerequisites

Install the following before you begin:

| Tool | Version | Install |
|------|---------|---------|
| Xcode | 15+ | Mac App Store |
| Homebrew | Latest | [brew.sh](https://brew.sh) |
| Node.js | 20+ | `brew install node` or [nodejs.org](https://nodejs.org) |
| Java | 17 | `brew install openjdk@17` |
| Android Studio | Latest | [developer.android.com/studio](https://developer.android.com/studio) |
| Docker Desktop | Latest | [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop) |
| xcodegen | Latest | `brew install xcodegen` |

Verify Node and Java versions before continuing:

```bash
node --version   # must be v20+
java -version    # must be 17+
```

---

## Section 1: Clone and rename

```bash
git clone https://github.com/vykeai/onlystack.git my-app
cd my-app
bash scripts/rename.sh "MyApp" "com.mycompany.myapp"
```

The rename script performs a full find-and-replace of `StarterApp` and the bundle ID across all source files, renames iOS directories, moves the Android package tree, and regenerates the Xcode project. It prompts for confirmation before making changes.

After renaming, update brand colours in `apps/starter-ios/MyApp/DesignSystem/Tokens/AppTokens.swift` and `apps/starter-android/app/src/main/kotlin/.../design/tokens/AppTokens.kt`.

---

## Section 2: Backend setup

```bash
cp .env.example apps/starter-api/.env
# Edit apps/starter-api/.env — set DATABASE_URL and JWT_SECRET at minimum
```

The minimum required values in `apps/starter-api/.env`:

```
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/app_dev
JWT_SECRET=change-me-to-a-long-random-string
REDIS_URL=redis://localhost:6379
```

Then start the services and run migrations:

```bash
cd apps/starter-api && npm install
docker-compose up -d          # starts postgres:15 + redis:7
npx prisma migrate dev --name init
npm run start:dev
```

Verify the backend is running:

```bash
curl http://localhost:3000/health
# Expected: {"status":"ok"}
```

Swagger UI is available at `http://localhost:3000/api/docs`.

---

## Section 3: iOS setup

```bash
cd apps/starter-ios && xcodegen generate
open StarterApp.xcodeproj
```

In Xcode:
1. Select the **StarterApp-Dev** scheme from the scheme picker
2. Select an iPhone simulator as the run destination
3. Press Cmd+R to build and run

The Dev scheme points to `http://localhost:3000/api/v1` by default.

> Note: If you ran the rename script, the project file will be named after your app (e.g. `MyApp.xcodeproj`).

---

## Section 4: Android setup

1. Open **Android Studio**
2. Choose **Open** and select the `apps/starter-android/` folder
3. Wait for the Gradle sync to complete (this may take a few minutes on first run)
4. In the **Build Variants** panel (View → Tool Windows → Build Variants), select **devDebug**
5. Select an emulator (or connected device) and press the Run button

The `devDebug` variant points to `http://10.0.2.2:3000/api/v1` (the emulator's alias for localhost).

Alternatively, build from the command line:

```bash
cd apps/starter-android
./gradlew assembleDevDebug
```

---

## Section 5: Run tests

```bash
# Backend unit tests
npm run test:backend

# Backend E2E tests (requires running Postgres — docker-compose up -d)
npm run test:e2e

# Contract / fixture validation
npm run validate:fixtures
```

iOS tests (run in Xcode):
- Select the **StarterApp-Dev** scheme
- Press **Cmd+U** to run the full test suite

Android tests:

```bash
cd apps/starter-android
./gradlew testDevDebugUnitTest
```

---

## Common issues

### DB connection refused

**Symptom:** `Error: connect ECONNREFUSED 127.0.0.1:5432` when starting the backend.

**Fix:** Ensure Docker Desktop is running and the containers are up:

```bash
docker-compose up -d
docker-compose ps   # both postgres and redis should be "running"
```

### Port 3000 already in use

**Symptom:** `Error: listen EADDRINUSE :::3000` when running `npm run start:dev`.

**Fix:** Find and stop the process using port 3000:

```bash
lsof -ti:3000 | xargs kill -9
```

Or change the backend port by setting `PORT=3001` in `apps/starter-api/.env` and updating the API_BASE_URL in `apps/starter-ios/Configs/Dev.xcconfig`.

### Xcode code signing error

**Symptom:** Build fails with "No signing certificate" or "Provisioning profile not found".

**Fix:** For simulator builds, signing is not required. In Xcode:
1. Select the project in the navigator
2. Go to **Signing & Capabilities**
3. Uncheck **Automatically manage signing**, or set **Team** to your personal team

For CI builds, `CODE_SIGNING_ALLOWED=NO` is already set in the workflow.

### Gradle build failure on first sync

**Symptom:** Android Studio shows Gradle sync errors on first open, often related to SDK or JDK version.

**Fix:**
1. Ensure Java 17 is active: `java -version`
2. In Android Studio: **File → Project Structure → SDK Location** — confirm JDK path points to Java 17
3. If the Android SDK is missing, follow the in-IDE prompt to install it
4. Try **File → Invalidate Caches → Invalidate and Restart**
