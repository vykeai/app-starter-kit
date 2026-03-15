package com.onlystack.starterapp.core.sync

interface SyncRepository {
    suspend fun fetchPending(): List<SyncableEntity>
    suspend fun applyServerChanges(changes: List<SyncChange>)
    suspend fun resolveConflict(conflict: SyncConflict, strategy: ResolutionStrategy)
}
