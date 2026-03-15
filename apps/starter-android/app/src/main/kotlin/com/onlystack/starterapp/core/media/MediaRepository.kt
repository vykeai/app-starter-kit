package com.onlystack.starterapp.core.media

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val apiService: MediaApiService,
) {
    suspend fun prepareUpload(
        kind: String,
        mimeType: String,
        fileName: String? = null,
        sizeBytes: Int? = null,
        visibility: String? = null,
    ): Result<MediaUploadPreparation> = runCatching {
        apiService.prepareUpload(
            PrepareMediaUploadBody(
                kind = kind,
                mimeType = mimeType,
                fileName = fileName,
                sizeBytes = sizeBytes,
                visibility = visibility,
            )
        )
    }

    suspend fun completeUpload(
        assetId: String,
        width: Int? = null,
        height: Int? = null,
    ): Result<MediaAsset> = runCatching {
        apiService.completeUpload(
            CompleteMediaUploadBody(assetId = assetId, width = width, height = height)
        )
    }

    suspend fun listAssets(): Result<List<MediaAsset>> = runCatching {
        apiService.listAssets()
    }

    suspend fun deleteAsset(assetId: String): Result<Unit> = runCatching {
        apiService.deleteAsset(assetId)
        Unit
    }
}
