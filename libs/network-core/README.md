# network-core

Source of truth currently lives in:

- iOS: `apps/starter-ios/StarterApp/Core/Network/`
- Android: `apps/starter-android/app/src/main/kotlin/com/onlystack/starterapp/core/network/`

Responsibilities:

- base URL configuration
- auth header injection
- transparent refresh behavior
- request ID headers
- runtime fixture-backed transport

This boundary should eventually own shared contract tests across platforms.
