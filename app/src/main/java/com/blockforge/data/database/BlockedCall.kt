package com.blockforge.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity representing a blocked call log entry
 */
@Entity(tableName = "blocked_calls")
data class BlockedCall(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "matched_prefix")
    val matchedPrefix: String,

    @ColumnInfo(name = "blocked_at")
    val blockedAt: Long = System.currentTimeMillis()
)
