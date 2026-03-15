package com.onlystack.starterapp.core.analytics

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AnalyticsApiService {
    @POST("analytics/events")
    suspend fun track(
        @Body body: AnalyticsEvent,
    ): TrackAcceptedResponse

    @POST("analytics/events/authenticated")
    suspend fun trackAuthenticated(
        @Body body: AnalyticsEvent,
    ): TrackAcceptedResponse

    @GET("analytics/summary")
    suspend fun summary(): AnalyticsSummary
}
