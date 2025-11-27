package com.blockforge.data.repository

import com.blockforge.data.database.BlockedCall
import com.blockforge.data.database.BlockedCallDao
import com.blockforge.data.database.BlockedPrefix
import com.blockforge.data.database.BlockedPrefixDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing blocked prefixes and call logs
 */
@Singleton
class BlocklistRepository @Inject constructor(
    private val prefixDao: BlockedPrefixDao,
    private val callDao: BlockedCallDao
) {
    // ===== Prefix Operations =====

    fun getAllPrefixes(): Flow<List<BlockedPrefix>> = prefixDao.getAllPrefixes()

    suspend fun addPrefix(prefix: String, description: String? = null) {
        val blockedPrefix = BlockedPrefix(
            prefix = prefix,
            description = description
        )
        prefixDao.insert(blockedPrefix)
    }

    suspend fun deletePrefix(prefix: BlockedPrefix) {
        prefixDao.delete(prefix)
    }

    suspend fun prefixExists(prefix: String): Boolean {
        return prefixDao.exists(prefix)
    }

    // ===== Blocked Call Log Operations =====

    fun getRecentBlockedCalls(): Flow<List<BlockedCall>> = callDao.getRecentBlockedCalls()

    fun getBlockedCallsCount(): Flow<Int> = callDao.getBlockedCallsCount()

    fun getBlockedCallsCountToday(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return callDao.getBlockedCallsCountToday(calendar.timeInMillis)
    }

    suspend fun logBlockedCall(phoneNumber: String, matchedPrefix: String) {
        val blockedCall = BlockedCall(
            phoneNumber = phoneNumber,
            matchedPrefix = matchedPrefix
        )
        callDao.insert(blockedCall)
    }

    suspend fun clearBlockedCallLogs() {
        callDao.clearAll()
    }

    suspend fun deleteBlockedCall(call: BlockedCall) {
        callDao.delete(call)
    }
}
