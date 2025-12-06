package com.blockforge.data.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import androidx.compose.runtime.Immutable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CallLogRepository"

/**
 * Represents a call from the system call log
 * @Immutable tells Compose this class won't change, improving recomposition performance
 */
@Immutable
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

        Log.d(TAG, "getRecentCalls called with limit=$limit")

        // Check permission first
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "READ_CALL_LOG permission granted: $hasPermission")

        if (!hasPermission) {
            Log.w(TAG, "READ_CALL_LOG permission NOT granted, returning empty list")
            return@withContext emptyList()
        }

        var cursor: Cursor? = null
        try {
            Log.d(TAG, "Querying CallLog.Calls.CONTENT_URI...")

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
                "${CallLog.Calls.DATE} DESC"
            )

            Log.d(TAG, "Query returned cursor: ${cursor != null}, count: ${cursor?.count ?: 0}")

            cursor?.let {
                val idIndex = it.getColumnIndex(CallLog.Calls._ID)
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

                Log.d(TAG, "Column indices - id:$idIndex, number:$numberIndex, name:$nameIndex, date:$dateIndex, duration:$durationIndex, type:$typeIndex")

                var count = 0
                while (it.moveToNext() && count < limit) {
                    val callType = when (it.getInt(typeIndex)) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        else -> CallType.UNKNOWN
                    }

                    // Handle empty/null phone numbers (private/hidden callers)
                    val rawNumber = it.getString(numberIndex)
                    val number = when {
                        rawNumber.isNullOrBlank() -> "Private Number"
                        rawNumber == "-1" -> "Private Number"
                        rawNumber == "-2" -> "Payphone"
                        else -> rawNumber
                    }

                    val entry = SystemCallEntry(
                        id = it.getLong(idIndex),
                        number = number,
                        contactName = it.getString(nameIndex),
                        date = it.getLong(dateIndex),
                        duration = it.getLong(durationIndex),
                        type = callType
                    )
                    calls.add(entry)
                    count++

                    if (count <= 3) {
                        Log.d(TAG, "Call entry: ${entry.number}, type=${entry.type}, date=${entry.date}")
                    }
                }
            }

            Log.d(TAG, "Loaded ${calls.size} calls from system log")

        } catch (e: Exception) {
            Log.e(TAG, "Error reading call log", e)
        } finally {
            cursor?.close()
        }

        calls
    }
}
