package com.blockforge.data.database

import androidx.room.*

/**
 * Data Access Object for CallerInfo cache
 */
@Dao
interface CallerInfoDao {

    /**
     * Get cached caller info by phone number
     */
    @Query("SELECT * FROM caller_info_cache WHERE phone_number = :phoneNumber")
    suspend fun getCallerInfo(phoneNumber: String): CallerInfo?

    /**
     * Insert or update caller info
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(callerInfo: CallerInfo)

    /**
     * Delete expired cache entries (older than 24 hours)
     */
    @Query("DELETE FROM caller_info_cache WHERE cached_at < :expiryTime")
    suspend fun deleteExpiredCache(expiryTime: Long)

    /**
     * Clear all cache
     */
    @Query("DELETE FROM caller_info_cache")
    suspend fun clearCache()

    /**
     * Get cache size
     */
    @Query("SELECT COUNT(*) FROM caller_info_cache")
    suspend fun getCacheSize(): Int
}
