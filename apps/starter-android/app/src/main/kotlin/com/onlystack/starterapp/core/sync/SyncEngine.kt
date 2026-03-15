package com.onlystack.starterapp.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.onlystack.starterapp.core.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    private val context: Context,
    private val network: NetworkMonitor,
    private val metrics: SyncMetrics,
) {
    var repository: SyncRepository? = null
    var apiClient: SyncApiService? = null

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state

    var lastSyncAt: Date? = null
        private set

    private val _unresolvedConflicts = MutableStateFlow<List<SyncConflict>>(emptyList())
    val unresolvedConflicts: StateFlow<List<SyncConflict>> = _unresolvedConflicts

    private val queue = SyncQueue()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    private val deviceId: String by lazy {
        val prefs = context.getSharedPreferences("sync_engine", Context.MODE_PRIVATE)
        prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also { id ->
            prefs.edit().putString("device_id", id).apply()
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    suspend fun sync() {
        if (!isOnline()) {
            _state.value = SyncState.Offline
            return
        }
        if (_state.value is SyncState.Syncing) return

        val repo = repository ?: return
        val api = apiClient ?: return

        _state.value = SyncState.Syncing
        val start = System.currentTimeMillis()

        runCatching {
            val pending = repo.fetchPending()
            val changes = pending.mapNotNull { entity ->
                val action = entity.syncAction ?: return@mapNotNull null
                SyncChange(
                    id = entity.id,
                    collection = entity::class.simpleName ?: "unknown",
                    action = action,
                    payload = emptyMap(),
                    updatedAt = entity.updatedAt.toInstant().toString(),
                )
            }

            var pushedCount = 0
            var conflictCount = 0
            var errorCount = 0

            if (changes.isNotEmpty()) {
                val response = api.syncPush(SyncPushRequest(changes, deviceId))
                pushedCount = response.applied.size
                errorCount = response.errors.size
                val appliedIds = response.applied.map { it.id }.toSet()
                queue.remove(appliedIds)
                conflictCount = response.conflicts.size
                _unresolvedConflicts.value = _unresolvedConflicts.value + response.conflicts
            }

            val pullResponse = api.syncPull(
                since = lastSyncAt?.toInstant()?.toString(),
                collections = null,
            )
            repo.applyServerChanges(pullResponse.items)

            val duration = System.currentTimeMillis() - start
            metrics.record(SyncRecord(
                timestamp = start,
                pushedCount = pushedCount,
                pulledCount = pullResponse.items.size,
                conflictCount = conflictCount,
                errorCount = errorCount,
                durationMs = duration,
                succeeded = true,
            ))

            lastSyncAt = Date()
            _state.value = SyncState.Idle
        }.onFailure { e ->
            val duration = System.currentTimeMillis() - start
            metrics.record(SyncRecord(
                timestamp = start,
                pushedCount = 0,
                pulledCount = 0,
                conflictCount = 0,
                errorCount = 1,
                durationMs = duration,
                succeeded = false,
            ))
            _state.value = SyncState.Error(e.message ?: "Unknown error")
        }
    }

    fun startPolling(intervalMs: Long = 30_000L) {
        stopPolling()
        pollingJob = scope.launch {
            while (isActive) {
                sync()
                delay(intervalMs)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    suspend fun resolve(conflict: SyncConflict, strategy: ResolutionStrategy) {
        runCatching {
            repository?.resolveConflict(conflict, strategy)
            _unresolvedConflicts.value = _unresolvedConflicts.value.filterNot { it.id == conflict.id }
        }.onFailure { e ->
            _state.value = SyncState.Error(e.message ?: "Resolve failed")
        }
    }
}
