# notifications-core

Source of truth currently lives in:

- backend: `backend/src/notification/`
- iOS: `ios/AppStarterKit/Core/Notifications/`
- Android: `android/app/src/main/kotlin/com/appstarterkit/app/core/notifications/`

Responsibilities:

- notification preference model
- register / revoke device tokens
- push registration manager shape
- typed server-side send interface

Permission prompts and product-specific settings UI stay outside this boundary.
