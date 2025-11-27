package com.blockforge.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for BlockedPrefix entity
 * Provides methods to interact with the database
 */
@Dao
interface BlockedPrefixDao {

    /**
     * Get all blocked prefixes as a Flow (reactive stream)
     * UI will automatically update when data changes
     */
    @Query("SELECT * FROM blocked_prefixes ORDER BY created_at DESC")
    fun getAllPrefixes(): Flow<List<BlockedPrefix>>

    /**
     * Get all prefixes as a simple list (for call screening service)
     */
    @Query("SELECT prefix FROM blocked_prefixes")
    suspend fun getAllPrefixStrings(): List<String>

    /**
     * Insert a new blocked prefix
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prefix: BlockedPrefix)

    /**
     * Delete a blocked prefix
     */
    @Delete
    suspend fun delete(prefix: BlockedPrefix)

    /**
     * Delete prefix by ID
     */
    @Query("DELETE FROM blocked_prefixes WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Check if a prefix exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM blocked_prefixes WHERE prefix = :prefix)")
    suspend fun exists(prefix: String): Boolean
}
