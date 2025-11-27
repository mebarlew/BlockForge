package com.blockforge.domain

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.blockforge.data.database.AppDatabase
import com.blockforge.data.database.BlockedCall
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CallScreeningService implementation
 * Intercepts incoming calls and blocks them based on prefix rules
 *
 * IMPORTANT: Must respond within 5 seconds or Android will timeout
 */
@AndroidEntryPoint
class CallBlockingService : CallScreeningService() {

    @Inject
    lateinit var database: AppDatabase

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        Log.d(TAG, "Screening call from: $phoneNumber")

        serviceScope.launch {
            val matchedPrefix = findMatchingPrefix(phoneNumber)
            val shouldBlock = matchedPrefix != null

            if (shouldBlock && matchedPrefix != null) {
                // Log the blocked call
                logBlockedCall(phoneNumber, matchedPrefix)
                Log.d(TAG, "BLOCKED call from $phoneNumber (matched prefix: $matchedPrefix)")
            } else {
                Log.d(TAG, "ALLOWED call from $phoneNumber")
            }

            val response = CallResponse.Builder()
                .setDisallowCall(shouldBlock)
                .setRejectCall(shouldBlock)
                .setSkipCallLog(false)
                .setSkipNotification(shouldBlock)
                .build()

            respondToCall(callDetails, response)
        }
    }

    /**
     * Find which prefix matches the phone number, if any
     */
    private suspend fun findMatchingPrefix(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null

        val blockedPrefixes = database.blockedPrefixDao().getAllPrefixStrings()
        return blockedPrefixes.find { prefix ->
            phoneNumber.startsWith(prefix)
        }
    }

    /**
     * Log a blocked call to the database
     */
    private suspend fun logBlockedCall(phoneNumber: String, matchedPrefix: String) {
        try {
            val blockedCall = BlockedCall(
                phoneNumber = phoneNumber,
                matchedPrefix = matchedPrefix
            )
            database.blockedCallDao().insert(blockedCall)
            Log.d(TAG, "Logged blocked call: $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log blocked call", e)
        }
    }

    companion object {
        private const val TAG = "CallBlockingService"
    }
}
