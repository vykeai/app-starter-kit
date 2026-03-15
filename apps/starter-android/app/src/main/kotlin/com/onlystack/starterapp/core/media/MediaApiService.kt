package com.onlystack.starterapp.core.media

import com.onlystack.starterapp.features.auth.MessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MediaApiService {
    @POST("media/uploads/prepare")
    suspend fun prepareUpload(
        @Body body: PrepareMediaUploadBody,
    ): MediaUploadPreparation

    @POST("media/uploads/complete")
    suspend fun completeUpload(
        @Body body: CompleteMediaUploadBody,
    ): MediaAsset

    @GET("media/assets")
    suspend fun listAssets(): List<MediaAsset>

    @DELETE("media/assets/{assetId}")
    suspend fun deleteAsset(
        @Path("assetId") assetId: String,
    ): MessageResponse
}
