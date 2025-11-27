package com.blockforge.data.repository

import android.util.Log
import com.blockforge.data.api.NumberLookupApi
import com.blockforge.data.database.CallerInfo
import com.blockforge.data.database.CallerInfoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for caller ID lookups with caching
 */
@Singleton
class CallerIdRepository @Inject constructor(
    private val callerInfoDao: CallerInfoDao,
    private val numberLookupApi: NumberLookupApi
) {
    companion object {
        private const val TAG = "CallerIdRepository"

        // NumVerify free API key - replace with your own for production
        // Get one at: https://numverify.com/
        private const val API_KEY = "YOUR_API_KEY_HERE"

        // Cache expiry time (24 hours)
        private const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L
    }

    /**
     * Look up caller information for a phone number
     * Returns cached data if available and valid, otherwise fetches from API
     */
    suspend fun lookupNumber(phoneNumber: String): CallerInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache first
                val cached = callerInfoDao.getCallerInfo(phoneNumber)
                if (cached != null && cached.isCacheValid()) {
                    Log.d(TAG, "Returning cached info for $phoneNumber")
                    return@withContext cached
                }

                // If no API key configured, return basic info
                if (API_KEY == "YOUR_API_KEY_HERE") {
                    Log.w(TAG, "No API key configured, returning basic info")
                    return@withContext createBasicCallerInfo(phoneNumber)
                }

                // Fetch from API
                Log.d(TAG, "Fetching info from API for $phoneNumber")
                val response = numberLookupApi.lookupNumber(
                    apiKey = API_KEY,
                    phoneNumber = phoneNumber
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.valid) {
                        val callerInfo = CallerInfo(
                            phoneNumber = phoneNumber,
                            countryCode = body.countryCode,
                            countryName = body.countryName,
                            carrier = body.carrier,
                            lineType = body.lineType,
                            isValid = true,
                            isSpam = isLikelySpam(body.lineType, body.carrier),
                            spamScore = calculateSpamScore(body.lineType, body.carrier)
                        )

                        // Cache the result
                        callerInfoDao.insertOrUpdate(callerInfo)
                        return@withContext callerInfo
                    }
                }

                // API call failed or number invalid
                Log.w(TAG, "API lookup failed for $phoneNumber")
                createBasicCallerInfo(phoneNumber)

            } catch (e: Exception) {
                Log.e(TAG, "Error looking up number", e)
                createBasicCallerInfo(phoneNumber)
            }
        }
    }

    /**
     * Create basic caller info from phone number analysis
     */
    private fun createBasicCallerInfo(phoneNumber: String): CallerInfo {
        val countryCode = extractCountryCode(phoneNumber)
        val lineType = guessLineType(phoneNumber)

        return CallerInfo(
            phoneNumber = phoneNumber,
            countryCode = countryCode,
            countryName = getCountryName(countryCode),
            lineType = lineType,
            isSpam = isLikelySpam(lineType, null),
            spamScore = calculateSpamScore(lineType, null)
        )
    }

    /**
     * Extract country code from phone number
     */
    private fun extractCountryCode(phoneNumber: String): String? {
        val cleaned = phoneNumber.replace(Regex("[^0-9+]"), "")

        return when {
            cleaned.startsWith("+1") -> "US"
            cleaned.startsWith("+44") -> "GB"
            cleaned.startsWith("+49") -> "DE"
            cleaned.startsWith("+33") -> "FR"
            cleaned.startsWith("+48") -> "PL"
            cleaned.startsWith("+91") -> "IN"
            cleaned.startsWith("+86") -> "CN"
            cleaned.startsWith("+81") -> "JP"
            cleaned.startsWith("+82") -> "KR"
            cleaned.startsWith("+61") -> "AU"
            cleaned.startsWith("+55") -> "BR"
            cleaned.startsWith("+52") -> "MX"
            cleaned.startsWith("+7") -> "RU"
            cleaned.startsWith("+34") -> "ES"
            cleaned.startsWith("+39") -> "IT"
            else -> null
        }
    }

    /**
     * Get country name from code
     */
    private fun getCountryName(code: String?): String? {
        return when (code) {
            "US" -> "United States"
            "GB" -> "United Kingdom"
            "DE" -> "Germany"
            "FR" -> "France"
            "PL" -> "Poland"
            "IN" -> "India"
            "CN" -> "China"
            "JP" -> "Japan"
            "KR" -> "South Korea"
            "AU" -> "Australia"
            "BR" -> "Brazil"
            "MX" -> "Mexico"
            "RU" -> "Russia"
            "ES" -> "Spain"
            "IT" -> "Italy"
            else -> null
        }
    }

    /**
     * Guess line type from phone number patterns
     */
    private fun guessLineType(phoneNumber: String): String? {
        val cleaned = phoneNumber.replace(Regex("[^0-9]"), "")

        return when {
            // US toll-free
            cleaned.startsWith("1800") || cleaned.startsWith("1888") ||
            cleaned.startsWith("1877") || cleaned.startsWith("1866") ||
            cleaned.startsWith("1855") || cleaned.startsWith("1844") -> "toll_free"

            // US premium
            cleaned.startsWith("1900") -> "premium_rate"

            else -> null
        }
    }

    /**
     * Determine if number is likely spam based on characteristics
     */
    private fun isLikelySpam(lineType: String?, carrier: String?): Boolean {
        // VoIP numbers are more likely to be spam
        if (lineType?.lowercase() == "voip") return true

        // Toll-free numbers calling you are often spam
        if (lineType?.lowercase() == "toll_free") return true

        return false
    }

    /**
     * Calculate spam score (0-100)
     */
    private fun calculateSpamScore(lineType: String?, carrier: String?): Int {
        var score = 0

        when (lineType?.lowercase()) {
            "voip" -> score += 40
            "toll_free" -> score += 30
            "premium_rate" -> score += 50
        }

        // Unknown carrier is suspicious
        if (carrier.isNullOrBlank()) {
            score += 10
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Clean up expired cache entries
     */
    suspend fun cleanupExpiredCache() {
        withContext(Dispatchers.IO) {
            val expiryTime = System.currentTimeMillis() - CACHE_EXPIRY_MS
            callerInfoDao.deleteExpiredCache(expiryTime)
        }
    }

    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            callerInfoDao.clearCache()
        }
    }
}
