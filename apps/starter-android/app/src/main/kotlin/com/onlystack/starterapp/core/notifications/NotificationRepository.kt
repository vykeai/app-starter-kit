package com.onlystack.starterapp.core.notifications

import com.onlystack.starterapp.BuildConfig
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
        pushEnabled: Boolean? = null,
        emailEnabled: Boolean? = null,
        enabledCategories: List<String>? = null,
        quietHoursEnabled: Boolean? = null,
        quietHoursStart: String? = null,
        quietHoursEnd: String? = null,
        urgentBreaksQuietHours: Boolean? = null,
        batchSoonNotifications: Boolean? = null,
    ): Result<NotificationPreferences> = runCatching {
        apiService.updatePreferences(
            UpdateNotificationPreferencesBody(
                theme = theme,
                pushMarketingEnabled = pushMarketingEnabled,
                pushActivityEnabled = pushActivityEnabled,
                pushTransactionalEnabled = pushTransactionalEnabled,
                pushSystemEnabled = pushSystemEnabled,
                emailNotificationsEnabled = emailNotificationsEnabled,
                pushEnabled = pushEnabled,
                emailEnabled = emailEnabled,
                enabledCategories = enabledCategories,
                quietHoursEnabled = quietHoursEnabled,
                quietHoursStart = quietHoursStart,
                quietHoursEnd = quietHoursEnd,
                urgentBreaksQuietHours = urgentBreaksQuietHours,
                batchSoonNotifications = batchSoonNotifications,
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
