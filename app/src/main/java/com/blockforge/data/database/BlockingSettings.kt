package com.blockforge.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Blocking settings/preferences
 */
@Entity(tableName = "blocking_settings")
data class BlockingSettings(
    @PrimaryKey
    val id: Int = 1, // Single row for settings

    @ColumnInfo(name = "block_unknown")
    val blockUnknown: Boolean = false,

    @ColumnInfo(name = "block_all")
    val blockAll: Boolean = false,

    @ColumnInfo(name = "block_international")
    val blockInternational: Boolean = false,

    @ColumnInfo(name = "user_country_code")
    val userCountryCode: String = "+1" // Default US, user can change
)
