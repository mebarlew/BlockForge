package com.blockforge.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for BlockedCall entity
 */
@Dao
interface BlockedCallDao {

    /**
     * Get all blocked calls as a Flow (most recent first)
     */
    @Query("SELECT * FROM blocked_calls ORDER BY blocked_at DESC")
    fun getAllBlockedCalls(): Flow<List<BlockedCall>>

    /**
     * Get recent blocked calls (last 50)
     */
    @Query("SELECT * FROM blocked_calls ORDER BY blocked_at DESC LIMIT 50")
    fun getRecentBlockedCalls(): Flow<List<BlockedCall>>

    /**
     * Get count of blocked calls
     */
    @Query("SELECT COUNT(*) FROM blocked_calls")
    fun getBlockedCallsCount(): Flow<Int>

    /**
     * Get count of calls blocked today
     */
    @Query("SELECT COUNT(*) FROM blocked_calls WHERE blocked_at >= :startOfDay")
    fun getBlockedCallsCountToday(startOfDay: Long): Flow<Int>

    /**
     * Insert a new blocked call log
     */
    @Insert
    suspend fun insert(blockedCall: BlockedCall)

    /**
     * Delete a blocked call log entry
     */
    @Delete
    suspend fun delete(blockedCall: BlockedCall)

    /**
     * Clear all blocked call logs
     */
    @Query("DELETE FROM blocked_calls")
    suspend fun clearAll()

    /**
     * Delete logs older than specified timestamp
     */
    @Query("DELETE FROM blocked_calls WHERE blocked_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
