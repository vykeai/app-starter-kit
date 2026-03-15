package com.onlystack.starterapp.core.user

data class UserPreferencesSummary(
    val theme: String? = null,
    val pushMarketingEnabled: Boolean? = null,
    val pushActivityEnabled: Boolean? = null,
    val pushTransactionalEnabled: Boolean? = null,
    val pushSystemEnabled: Boolean? = null,
    val emailNotificationsEnabled: Boolean? = null,
)

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String?,
    val preferences: UserPreferencesSummary? = null,
)

data class UpdateUserProfileBody(
    val displayName: String? = null,
)
