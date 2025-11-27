package com.blockforge.domain

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.blockforge.data.database.AppDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CallScreeningService implementation
 * This service intercepts incoming calls and blocks them based on prefix rules
 *
 * IMPORTANT: This service must respond within 5 seconds or Android will timeout
 */
@AndroidEntryPoint
class CallBlockingService : CallScreeningService() {

    @Inject
    lateinit fun database: AppDatabase

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        // Extract phone number from call details
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        Log.d(TAG, "Screening call from: $phoneNumber")

        // Check if number should be blocked (async but must respond quickly)
        serviceScope.launch {
            val shouldBlock = checkIfBlocked(phoneNumber)

            // Build response
            val response = CallResponse.Builder()
                .setDisallowCall(shouldBlock)
                .setRejectCall(shouldBlock)
                .setSkipCallLog(false)  // Still log blocked calls
                .setSkipNotification(false)  // Show notification for blocked calls
                .build()

            // Respond to call
            respondToCall(callDetails, response)

            Log.d(TAG, "Call ${if (shouldBlock) "BLOCKED" else "ALLOWED"}: $phoneNumber")
        }
    }

    /**
     * Check if phone number starts with any blocked prefix
     * Must execute quickly (< 5 seconds)
     */
    private suspend fun checkIfBlocked(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false

        // Get all blocked prefixes from database
        val blockedPrefixes = database.blockedPrefixDao().getAllPrefixStrings()

        // Check if number starts with any blocked prefix
        return blockedPrefixes.any { prefix ->
            phoneNumber.startsWith(prefix)
        }
    }

    companion object {
        private const val TAG = "CallBlockingService"
    }
}
