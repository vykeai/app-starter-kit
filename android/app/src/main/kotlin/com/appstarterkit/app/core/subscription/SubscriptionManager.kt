package com.appstarterkit.app.core.subscription

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// RevenueCat integration stub
// TODO: Add RevenueCat dependency to libs.versions.toml:
//   revenuecat = { group = "com.revenuecat.purchases", name = "purchases", version = "7.0.0" }
//   revenuecat-ui = { group = "com.revenuecat.purchases", name = "purchases-ui", version = "7.0.0" }
// Then add to build.gradle.kts: implementation(libs.revenuecat)

@Singleton
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // TODO: Replace with your RevenueCat public SDK key from dashboard
    private val sdkKey = "YOUR_REVENUECAT_PUBLIC_SDK_KEY"

    enum class SubscriptionTier { FREE, TRACKER }

    private val _currentTier = MutableStateFlow(SubscriptionTier.FREE)
    val currentTier: StateFlow<SubscriptionTier> = _currentTier.asStateFlow()

    fun configure(userId: String) {
        // TODO: Uncomment after adding SDK:
        // Purchases.logLevel = LogLevel.DEBUG
        // Purchases.configure(PurchasesConfiguration.Builder(context, sdkKey).appUserID(userId).build())
    }

    fun isFeatureAvailable(feature: TrackerFeature): Boolean = when (feature) {
        TrackerFeature.BASIC_LOGGING, TrackerFeature.EXERCISE_LIBRARY -> true
        TrackerFeature.UNLIMITED_TEMPLATES,
        TrackerFeature.ADVANCED_ANALYTICS,
        TrackerFeature.EXPORT_DATA,
        -> _currentTier.value == SubscriptionTier.TRACKER
    }

    enum class TrackerFeature {
        BASIC_LOGGING, EXERCISE_LIBRARY,           // Free
        UNLIMITED_TEMPLATES, ADVANCED_ANALYTICS, EXPORT_DATA, // Tracker+
    }
}
