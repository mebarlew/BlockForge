package com.blockforge.data.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents a call from the system call log
 */
data class SystemCallEntry(
    val id: Long,
    val number: String,
    val contactName: String?,
    val date: Long,
    val duration: Long,
    val type: CallType
)

/**
 * Type of call from the system log
 */
enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    UNKNOWN
}

/**
 * Repository for reading the system call log
 * Uses Android's CallLog content provider
 */
@Singleton
class CallLogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Get recent calls from the system call log
     * @param limit Maximum number of calls to return
     */
    suspend fun getRecentCalls(limit: Int = 100): List<SystemCallEntry> = withContext(Dispatchers.IO) {
        val calls = mutableListOf<SystemCallEntry>()

        // Check permission first
        if (context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
                ),
                null,
                null,
                "${CallLog.Calls.DATE} DESC LIMIT $limit"
            )

            cursor?.let {
                val idIndex = it.getColumnIndex(CallLog.Calls._ID)
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

                while (it.moveToNext()) {
                    val callType = when (it.getInt(typeIndex)) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        else -> CallType.UNKNOWN
                    }

                    calls.add(
                        SystemCallEntry(
                            id = it.getLong(idIndex),
                            number = it.getString(numberIndex) ?: "Unknown",
                            contactName = it.getString(nameIndex),
                            date = it.getLong(dateIndex),
                            duration = it.getLong(durationIndex),
                            type = callType
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("CallLogRepository", "Error reading call log", e)
        } finally {
            cursor?.close()
        }

        calls
    }
}
