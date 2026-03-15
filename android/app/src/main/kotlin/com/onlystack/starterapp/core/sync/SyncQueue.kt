package com.onlystack.starterapp.core.sync

import java.util.Date
import kotlin.math.min
import kotlin.math.pow

data class SyncQueueItem(
    val change: SyncChange,
    val retryCount: Int = 0,
    val nextRetryAt: Date = Date(),
) {
    val isEligible: Boolean get() = retryCount < 5 && nextRetryAt <= Date()

    fun scheduleRetry(): SyncQueueItem {
        val delayMs = min(2.0.pow(retryCount.toDouble()) * 2_000, 300_000.0).toLong()
        return copy(
            retryCount = retryCount + 1,
            nextRetryAt = Date(System.currentTimeMillis() + delayMs),
        )
    }
}

class SyncQueue {
    private val items = mutableListOf<SyncQueueItem>()

    fun enqueue(change: SyncChange) { items.add(SyncQueueItem(change)) }

    fun dequeueEligible(): List<SyncQueueItem> = items.filter { it.isEligible }

    fun markFailed(ids: Set<String>) {
        val updated = items.map { if (it.change.id in ids) it.scheduleRetry() else it }
        items.clear()
        items.addAll(updated.filter { it.retryCount < 5 })
    }

    fun remove(ids: Set<String>) { items.removeAll { it.change.id in ids } }

    val isEmpty: Boolean get() = items.isEmpty()
}
