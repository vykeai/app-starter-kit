package com.onlystack.starterapp.core.sync

import java.util.Date

interface SyncableEntity {
    val id: String
    var updatedAt: Date
    var pendingSync: Boolean
    var syncAction: SyncAction?
    var lastSyncedAt: Date?
}

enum class SyncAction { CREATE, UPDATE, DELETE }
