package com.onlystack.starterapp.core.analytics

data class AnalyticsEvent(
    val name: String,
    val category: String? = null,
    val properties: Map<String, String>? = null,
)

data class AnalyticsSummary(
    val totalEvents: Int,
    val uniqueEvents: Int,
    val topEvents: List<TopEvent>,
    val lastEventAt: String? = null,
) {
    data class TopEvent(
        val name: String,
        val count: Int,
    )
}

data class TrackAcceptedResponse(
    val accepted: Boolean,
)
