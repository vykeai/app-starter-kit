package com.onlystack.starterapp.core.sync

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

data class SyncRecord(
    val timestamp: Long,
    val pushedCount: Int,
    val pulledCount: Int,
    val conflictCount: Int,
    val errorCount: Int,
    val durationMs: Long,
    val succeeded: Boolean,
)

@Singleton
class SyncMetrics @Inject constructor(context: Context) {
    private val prefs = context.getSharedPreferences("sync_metrics", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "sync_records"

    val records: MutableList<SyncRecord> = load().toMutableList()

    val successRate: Double
        get() = if (records.isEmpty()) 1.0
                else records.count { it.succeeded }.toDouble() / records.size

    fun record(entry: SyncRecord) {
        records.add(entry)
        if (records.size > 100) records.subList(0, records.size - 100).clear()
        save()
    }

    private fun load(): List<SyncRecord> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<SyncRecord>>(json, object : TypeToken<List<SyncRecord>>() {}.type)
        }.getOrDefault(emptyList())
    }

    private fun save() {
        prefs.edit().putString(key, gson.toJson(records)).apply()
    }
}
