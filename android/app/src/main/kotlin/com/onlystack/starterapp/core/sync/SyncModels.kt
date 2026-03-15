package com.onlystack.starterapp.core.sync

data class SyncChange(
    val id: String,
    val collection: String,
    val action: SyncAction,
    val payload: Map<String, Any>,
    val updatedAt: String,
)

data class SyncResult(
    val id: String,
    val collection: String,
    val success: Boolean,
)

data class SyncConflict(
    val id: String,
    val collection: String,
    val clientVersion: Map<String, Any>,
    val serverVersion: Map<String, Any>,
    val serverUpdatedAt: String,
)

data class SyncError(
    val id: String,
    val collection: String,
    val message: String,
)

data class SyncPushRequest(
    val changes: List<SyncChange>,
    val deviceId: String,
)

data class SyncPushResponse(
    val applied: List<SyncResult>,
    val conflicts: List<SyncConflict>,
    val errors: List<SyncError>,
    val timestamp: String,
)

data class SyncPullResponse(
    val items: List<SyncChange>,
    val hasMore: Boolean,
    val nextSyncAt: String,
)
