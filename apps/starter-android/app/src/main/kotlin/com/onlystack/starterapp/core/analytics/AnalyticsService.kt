package com.onlystack.starterapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor(
    private val apiService: AnalyticsApiService,
) {
    suspend fun track(
        name: String,
        category: String? = null,
        properties: Map<String, String>? = null,
    ): Result<Unit> = runCatching {
        apiService.track(AnalyticsEvent(name = name, category = category, properties = properties))
        Unit
    }

    suspend fun trackAuthenticated(
        name: String,
        category: String? = null,
        properties: Map<String, String>? = null,
    ): Result<Unit> = runCatching {
        apiService.trackAuthenticated(
            AnalyticsEvent(name = name, category = category, properties = properties)
        )
        Unit
    }

    suspend fun fetchSummary(): Result<AnalyticsSummary> = runCatching {
        apiService.summary()
    }
}
