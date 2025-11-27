package com.blockforge.domain

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.blockforge.data.database.AppDatabase
import com.blockforge.data.database.BlockedCall
import com.blockforge.data.database.CallerInfo
import com.blockforge.data.repository.CallerIdRepository
import com.blockforge.ui.CallerIdActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CallScreeningService implementation
 * Intercepts incoming calls, performs caller ID lookup, and blocks based on prefix rules
 *
 * IMPORTANT: Must respond within 5 seconds or Android will timeout
 */
@AndroidEntryPoint
class CallBlockingService : CallScreeningService() {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var callerIdRepository: CallerIdRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        Log.d(TAG, "Screening call from: $phoneNumber")

        serviceScope.launch {
            // Check if number should be blocked
            val matchedPrefix = findMatchingPrefix(phoneNumber)
            val shouldBlock = matchedPrefix != null

            // Perform caller ID lookup (non-blocking, cached)
            val callerInfo = lookupCallerInfo(phoneNumber)

            if (shouldBlock && matchedPrefix != null) {
                // Log the blocked call
                logBlockedCall(phoneNumber, matchedPrefix)
                Log.d(TAG, "BLOCKED call from $phoneNumber (matched prefix: $matchedPrefix)")
            } else {
                // Show caller ID overlay for non-blocked calls
                if (callerInfo != null) {
                    showCallerIdOverlay(phoneNumber, callerInfo)
                }
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
     * Look up caller information
     */
    private suspend fun lookupCallerInfo(phoneNumber: String): CallerInfo? {
        return try {
            callerIdRepository.lookupNumber(phoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up caller info", e)
            null
        }
    }

    /**
     * Show caller ID overlay
     */
    private fun showCallerIdOverlay(phoneNumber: String, callerInfo: CallerInfo) {
        try {
            val intent = Intent(this, CallerIdActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(CallerIdActivity.EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(CallerIdActivity.EXTRA_COUNTRY_NAME, callerInfo.countryName)
                putExtra(CallerIdActivity.EXTRA_CARRIER, callerInfo.carrier)
                putExtra(CallerIdActivity.EXTRA_LINE_TYPE, callerInfo.getLineTypeDisplay())
                putExtra(CallerIdActivity.EXTRA_IS_SPAM, callerInfo.isSpam)
                putExtra(CallerIdActivity.EXTRA_SPAM_SCORE, callerInfo.spamScore)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show caller ID overlay", e)
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
