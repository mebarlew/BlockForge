package com.blockforge.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database for BlockForge
 * Currently contains only BlockedPrefix table
 */
@Database(
    entities = [BlockedPrefix::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedPrefixDao(): BlockedPrefixDao
}
