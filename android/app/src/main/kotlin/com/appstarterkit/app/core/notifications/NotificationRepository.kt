package com.appstarterkit.app.core.notifications

import com.appstarterkit.app.BuildConfig
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: NotificationApiService,
) {
    suspend fun fetchPreferences(): Result<NotificationPreferences> = runCatching {
        apiService.fetchPreferences()
    }

    suspend fun updatePreferences(
        theme: String? = null,
        pushMarketingEnabled: Boolean? = null,
        pushActivityEnabled: Boolean? = null,
        pushTransactionalEnabled: Boolean? = null,
        pushSystemEnabled: Boolean? = null,
        emailNotificationsEnabled: Boolean? = null,
    ): Result<NotificationPreferences> = runCatching {
        apiService.updatePreferences(
            UpdateNotificationPreferencesBody(
                theme = theme,
                pushMarketingEnabled = pushMarketingEnabled,
                pushActivityEnabled = pushActivityEnabled,
                pushTransactionalEnabled = pushTransactionalEnabled,
                pushSystemEnabled = pushSystemEnabled,
                emailNotificationsEnabled = emailNotificationsEnabled,
            ),
        )
    }

    suspend fun registerCurrentDevice(pushToken: String): Result<RegisteredPushDevice> = runCatching {
        apiService.registerDevice(
            RegisterPushDeviceBody(
                platform = "android",
                token = pushToken,
                locale = Locale.getDefault().toLanguageTag(),
                appVersion = BuildConfig.VERSION_NAME,
            ),
        )
    }

    suspend fun revokeCurrentDevice(pushToken: String): Result<Unit> = runCatching {
        apiService.revokeDevice(pushToken)
        Unit
    }
}
