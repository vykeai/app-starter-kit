# auth-core

Source of truth currently lives in:

- backend: `apps/starter-api/src/auth/`
- iOS: `apps/starter-ios/StarterApp/Features/Auth/`
- Android: `apps/starter-android/app/src/main/kotlin/com/onlystack/starterapp/features/auth/`

Responsibilities:

- email code + link-token auth
- Apple / Google sign-in contract
- pending deep-link auth payload
- client auth state machine

The UI remains native per platform. The shared part is the contract and flow.
