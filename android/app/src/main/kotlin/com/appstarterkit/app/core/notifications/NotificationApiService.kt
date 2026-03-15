package com.appstarterkit.app.core.notifications

import com.appstarterkit.app.features.auth.MessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApiService {
    @GET("notifications/preferences")
    suspend fun fetchPreferences(): NotificationPreferences

    @PATCH("notifications/preferences")
    suspend fun updatePreferences(
        @Body body: UpdateNotificationPreferencesBody,
    ): NotificationPreferences

    @POST("notifications/devices")
    suspend fun registerDevice(
        @Body body: RegisterPushDeviceBody,
    ): RegisteredPushDevice

    @DELETE("notifications/devices/{token}")
    suspend fun revokeDevice(
        @Path("token") token: String,
    ): MessageResponse
}
