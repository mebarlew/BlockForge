package com.blockforge.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached caller information from number lookup
 */
@Entity(tableName = "caller_info_cache")
data class CallerInfo(
    @PrimaryKey
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "country_code")
    val countryCode: String? = null,

    @ColumnInfo(name = "country_name")
    val countryName: String? = null,

    @ColumnInfo(name = "carrier")
    val carrier: String? = null,

    @ColumnInfo(name = "line_type")
    val lineType: String? = null, // mobile, landline, voip, etc.

    @ColumnInfo(name = "is_valid")
    val isValid: Boolean = true,

    @ColumnInfo(name = "is_spam")
    val isSpam: Boolean = false,

    @ColumnInfo(name = "spam_score")
    val spamScore: Int = 0, // 0-100

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if cache is still valid (24 hours)
     */
    fun isCacheValid(): Boolean {
        val cacheLifetime = 24 * 60 * 60 * 1000L // 24 hours
        return System.currentTimeMillis() - cachedAt < cacheLifetime
    }

    /**
     * Get display-friendly line type
     */
    fun getLineTypeDisplay(): String {
        return when (lineType?.lowercase()) {
            "mobile" -> "Mobile"
            "landline", "fixed_line" -> "Landline"
            "voip" -> "VoIP"
            "toll_free" -> "Toll-Free"
            "premium_rate" -> "Premium"
            "shared_cost" -> "Shared Cost"
            else -> lineType?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        }
    }
}
