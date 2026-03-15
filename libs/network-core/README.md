# network-core

Source of truth currently lives in:

- iOS: `ios/AppStarterKit/Core/Network/`
- Android: `android/app/src/main/kotlin/com/appstarterkit/app/core/network/`

Responsibilities:

- base URL configuration
- auth header injection
- transparent refresh behavior
- request ID headers
- runtime fixture-backed transport

This boundary should eventually own shared contract tests across platforms.
