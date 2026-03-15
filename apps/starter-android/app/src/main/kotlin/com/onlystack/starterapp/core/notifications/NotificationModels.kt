package com.onlystack.starterapp.core.notifications

data class NotificationPreferences(
    val theme: String,
    val pushMarketingEnabled: Boolean,
    val pushActivityEnabled: Boolean,
    val pushTransactionalEnabled: Boolean,
    val pushSystemEnabled: Boolean,
    val emailNotificationsEnabled: Boolean,
    val pushEnabled: Boolean? = null,
    val emailEnabled: Boolean? = null,
    val enabledCategories: List<String>? = null,
    val quietHoursEnabled: Boolean? = null,
    val quietHoursStart: String? = null,
    val quietHoursEnd: String? = null,
    val urgentBreaksQuietHours: Boolean? = null,
    val batchSoonNotifications: Boolean? = null,
    val updatedAt: String? = null,
)

data class UpdateNotificationPreferencesBody(
    val theme: String? = null,
    val pushMarketingEnabled: Boolean? = null,
    val pushActivityEnabled: Boolean? = null,
    val pushTransactionalEnabled: Boolean? = null,
    val pushSystemEnabled: Boolean? = null,
    val emailNotificationsEnabled: Boolean? = null,
    val pushEnabled: Boolean? = null,
    val emailEnabled: Boolean? = null,
    val enabledCategories: List<String>? = null,
    val quietHoursEnabled: Boolean? = null,
    val quietHoursStart: String? = null,
    val quietHoursEnd: String? = null,
    val urgentBreaksQuietHours: Boolean? = null,
    val batchSoonNotifications: Boolean? = null,
)

data class RegisterPushDeviceBody(
    val platform: String,
    val token: String,
    val locale: String? = null,
    val appVersion: String? = null,
)

data class RegisteredPushDevice(
    val id: String,
    val platform: String,
    val token: String,
    val locale: String? = null,
    val appVersion: String? = null,
    val lastSeenAt: String? = null,
    val revokedAt: String? = null,
)
