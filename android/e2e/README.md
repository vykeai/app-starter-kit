# Android E2E Tests (Detox)

Gray-box end-to-end testing for the Android app using
[Detox](https://wix.github.io/Detox/). Tests run against a debug APK on an
Android emulator.

## Prerequisites

- Node.js 20+
- Android SDK with an AVD named `Pixel_6_API_34` (or update `.detoxrc.js`)
- The backend running locally or pointed at staging

## Install Detox CLI

```bash
npm install -g detox-cli
```

Install project dev dependencies (run from the `android/` directory):

```bash
cd android
npm install --save-dev detox jest jest-circus @types/detox
```

## Setup

The Detox config lives in `android/e2e/.detoxrc.js`. It references the
emulator AVD name and the APK build path. Update `avdName` to match your
local Android Virtual Device:

```bash
# List your local AVDs
emulator -list-avds
```

## Build the app

```bash
detox build --configuration android.emu.debug
```

This runs `./gradlew assembleDevDebug assembleDevDebugAndroidTest` and places
the APK at `app/build/outputs/apk/dev/debug/app-dev-debug.apk`.

## Run tests

```bash
detox test --configuration android.emu.debug
```

Run a single test file:

```bash
detox test --configuration android.emu.debug e2e/auth.test.js
```

## Port forwarding

The `.detoxrc.js` config includes `reversePorts: [3000]` which runs
`adb reverse tcp:3000 tcp:3000` automatically so the emulator can reach
your local backend at `http://localhost:3000`.

## CI usage (GitHub Actions)

```yaml
- name: Run Detox E2E
  run: |
    detox build --configuration android.emu.debug
    detox test --configuration android.emu.debug --headless
```

## Test inventory

| File | What it covers |
|------|----------------|
| `auth.test.js` | Welcome screen, navigation to email input, empty-email validation |
