package com.blockforge.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity representing a blocked phone number prefix
 *
 * Example: "+48" blocks all calls starting with +48
 */
@Entity(tableName = "blocked_prefixes")
data class BlockedPrefix(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "prefix")
    val prefix: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
