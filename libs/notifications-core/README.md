# notifications-core

Source of truth currently lives in:

- backend: `apps/starter-api/src/notification/`
- iOS: `apps/starter-ios/StarterApp/Core/Notifications/`
- Android: `apps/starter-android/app/src/main/kotlin/com/onlystack/starterapp/core/notifications/`

Responsibilities:

- notification preference model
- register / revoke device tokens
- push registration manager shape
- typed server-side send interface

Permission prompts and product-specific settings UI stay outside this boundary.
