package com.blockforge.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database for BlockForge
 */
@Database(
    entities = [BlockedPrefix::class, BlockedCall::class, CallerInfo::class, BlockingSettings::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedPrefixDao(): BlockedPrefixDao
    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun callerInfoDao(): CallerInfoDao
    abstract fun blockingSettingsDao(): BlockingSettingsDao
}
