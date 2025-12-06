package com.blockforge.domain

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.blockforge.data.database.AppDatabase
import com.blockforge.data.database.BlockedCall
import com.blockforge.data.database.CallerInfo
import com.blockforge.data.repository.CallerIdRepository
import com.blockforge.data.repository.SettingsRepository
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

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CallBlockingService CREATED")
    }

    override fun onBind(intent: Intent?) = super.onBind(intent).also {
        Log.d(TAG, "CallBlockingService BOUND")
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        Log.e(TAG, "=== CALL SCREENING STARTED ===")
        Log.e(TAG, "Phone number: $phoneNumber")
        Log.e(TAG, "Call details: $callDetails")

        serviceScope.launch {
            try {
                // Get current blocking settings
                val settings = settingsRepository.getSettingsOnce()
                Log.e(TAG, "Settings loaded: blockAll=${settings.blockAll}, blockUnknown=${settings.blockUnknown}, blockInternational=${settings.blockInternational}")

                // Check if number should be blocked based on settings and prefix list
                val matchedPrefix = findMatchingPrefix(phoneNumber)
                Log.e(TAG, "Matched prefix: $matchedPrefix")

                val isInContactsList = isInContacts(phoneNumber)
                Log.e(TAG, "Is in contacts: $isInContactsList")

                val shouldBlock = when {
                    // Block all calls setting takes priority
                    settings.blockAll -> {
                        Log.e(TAG, "Blocking: BLOCK ALL is enabled")
                        true
                    }
                    // Check if matches a blocked prefix
                    matchedPrefix != null -> {
                        Log.e(TAG, "Blocking: Matched prefix $matchedPrefix")
                        true
                    }
                    // Block unknown numbers (not in contacts)
                    settings.blockUnknown && !isInContactsList -> {
                        Log.e(TAG, "Blocking: Unknown number (not in contacts)")
                        true
                    }
                    // Block international numbers
                    settings.blockInternational && isInternationalNumber(phoneNumber, settings.userCountryCode) -> {
                        Log.e(TAG, "Blocking: International number")
                        true
                    }
                    // Otherwise allow
                    else -> {
                        Log.e(TAG, "Allowing call")
                        false
                    }
                }

                Log.e(TAG, "Final decision: shouldBlock = $shouldBlock")

                // IMPORTANT: Respond immediately to avoid Android timeout (5 second limit)
                val response = CallResponse.Builder()
                    .setDisallowCall(shouldBlock)
                    .setRejectCall(shouldBlock)
                    .setSkipCallLog(false)
                    .setSkipNotification(shouldBlock)
                    .build()

                Log.e(TAG, "Calling respondToCall with disallow=$shouldBlock, reject=$shouldBlock")
                respondToCall(callDetails, response)
                Log.e(TAG, "respondToCall completed")
            } catch (e: Exception) {
                Log.e(TAG, "ERROR in call screening", e)
                // On error, allow the call
                val response = CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .build()
                respondToCall(callDetails, response)
                Log.e(TAG, "=== CALL SCREENING COMPLETED ===")
            }
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

    /**
     * Check if phone number is in user's contacts
     */
    private fun isInContacts(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false

        var cursor: Cursor? = null
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            cursor = contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )
            return cursor?.count ?: 0 > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking contacts", e)
            return false
        } finally {
            cursor?.close()
        }
    }

    /**
     * Check if number is international (different country code than user's)
     */
    private fun isInternationalNumber(phoneNumber: String, userCountryCode: String): Boolean {
        if (phoneNumber.isBlank() || !phoneNumber.startsWith("+")) {
            return false
        }

        // If number starts with + but not with user's country code, it's international
        return !phoneNumber.startsWith(userCountryCode)
    }

    companion object {
        private const val TAG = "CallBlockingService"
    }
}
