package com.onlystack.starterapp.core.media

data class PrepareMediaUploadBody(
    val kind: String,
    val mimeType: String,
    val fileName: String? = null,
    val sizeBytes: Int? = null,
    val visibility: String? = null,
)

data class CompleteMediaUploadBody(
    val assetId: String,
    val width: Int? = null,
    val height: Int? = null,
)

data class MediaUploadPreparation(
    val assetId: String,
    val storageKey: String,
    val uploadUrl: String,
    val publicUrl: String? = null,
    val headers: Map<String, String>,
    val expiresInSeconds: Int,
)

data class MediaAsset(
    val id: String,
    val ownerId: String,
    val kind: String,
    val status: String,
    val storageKey: String,
    val publicUrl: String? = null,
    val mimeType: String,
    val fileName: String? = null,
    val sizeBytes: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val visibility: String,
    val createdAt: String,
)
