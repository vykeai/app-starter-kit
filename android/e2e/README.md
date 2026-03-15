# Android E2E Tests

The canonical Android proof path is now:
- build the `mock` flavour
- run on an emulator managed by `simemu`
- drive the flow with Maestro or a thin harness
- capture screenshots with `simemu screenshot`

## Prerequisites

- Node.js 20+
- A `simemu` Android slug assigned to this project
- No backend required when using the `mock` flavour

## Build the app

```bash
cd android
./gradlew assembleMockDebug
```

## Run on a simemu device

```bash
export SIMEMU_AGENT=starter
simemu status
simemu boot starter-android
simemu install starter-android app/build/outputs/apk/mock/debug/app-mock-debug.apk
simemu launch starter-android com.appstarterkit.app.mock
```

## Screenshot proof loop

```bash
simemu screenshot starter-android -o ~/Desktop/screenshots/starter/android_auth.png --max-size 1000
```

The old Detox scaffolding is still present, but it is no longer the recommended
starter proof loop. New starter coverage should target `simemu` + Maestro so
the iOS and Android screenshot path stays aligned.
