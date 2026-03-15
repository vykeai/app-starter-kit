package com.onlystack.starterapp.core.sync

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApiService {
    @POST("sync/push")
    suspend fun syncPush(@Body request: SyncPushRequest): SyncPushResponse

    @GET("sync/pull")
    suspend fun syncPull(
        @Query("since") since: String?,
        @Query("collections") collections: String?,
    ): SyncPullResponse
}
