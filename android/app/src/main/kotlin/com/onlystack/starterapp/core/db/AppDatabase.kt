package com.onlystack.starterapp.core.db

import androidx.room.Database
import androidx.room.Entity
import androidx.room.migration.Migration
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String?,
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val collection: String,
    val action: String,
    val payload: String, // JSON
    val updatedAt: String,
    val retryCount: Int = 0,
    val nextRetryAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sync_queue (
                id TEXT NOT NULL PRIMARY KEY,
                collection TEXT NOT NULL,
                action TEXT NOT NULL,
                payload TEXT NOT NULL,
                updatedAt TEXT NOT NULL,
                retryCount INTEGER NOT NULL DEFAULT 0,
                nextRetryAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sync_metadata (
                `key` TEXT NOT NULL PRIMARY KEY,
                value TEXT NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [UserEntity::class, SyncQueueEntity::class, SyncMetadataEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase()
