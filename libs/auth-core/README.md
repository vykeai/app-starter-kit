# auth-core

Source of truth currently lives in:

- backend: `backend/src/auth/`
- iOS: `ios/AppStarterKit/Features/Auth/`
- Android: `android/app/src/main/kotlin/com/appstarterkit/app/features/auth/`

Responsibilities:

- email code + link-token auth
- Apple / Google sign-in contract
- pending deep-link auth payload
- client auth state machine

The UI remains native per platform. The shared part is the contract and flow.
