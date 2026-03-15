package com.appstarterkit.app.core.notifications

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRegistrationManager @Inject constructor(
    private val repository: NotificationRepository,
) {
    suspend fun registerCurrentDevice(pushToken: String): Result<RegisteredPushDevice> {
        return repository.registerCurrentDevice(pushToken)
    }

    suspend fun revokeCurrentDevice(pushToken: String): Result<Unit> {
        return repository.revokeCurrentDevice(pushToken)
    }

    // Permission prompts and token retrieval belong to product UI / Firebase setup.
    // Starter notification-core owns the registration contract once a token exists.
}
