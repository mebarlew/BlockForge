package com.blockforge.data.repository

import com.blockforge.data.database.BlockedPrefix
import com.blockforge.data.database.BlockedPrefixDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing blocked prefixes
 * Provides a clean API for the ViewModel to interact with the database
 */
@Singleton
class BlocklistRepository @Inject constructor(
    private val dao: BlockedPrefixDao
) {

    /**
     * Get all blocked prefixes as a Flow
     */
    fun getAllPrefixes(): Flow<List<BlockedPrefix>> = dao.getAllPrefixes()

    /**
     * Add a new blocked prefix
     */
    suspend fun addPrefix(prefix: String, description: String? = null) {
        val blockedPrefix = BlockedPrefix(
            prefix = prefix,
            description = description
        )
        dao.insert(blockedPrefix)
    }

    /**
     * Delete a blocked prefix
     */
    suspend fun deletePrefix(prefix: BlockedPrefix) {
        dao.delete(prefix)
    }

    /**
     * Check if prefix already exists
     */
    suspend fun prefixExists(prefix: String): Boolean {
        return dao.exists(prefix)
    }
}
