package com.blockforge.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for NumVerify API (free tier)
 * https://numverify.com/documentation
 *
 * Free tier: 100 requests/month
 * For production, consider getting an API key
 */
interface NumberLookupApi {

    @GET("validate")
    suspend fun lookupNumber(
        @Query("access_key") apiKey: String,
        @Query("number") phoneNumber: String,
        @Query("country_code") countryCode: String? = null,
        @Query("format") format: Int = 1
    ): Response<NumVerifyResponse>
}

/**
 * Response from NumVerify API
 */
@Serializable
data class NumVerifyResponse(
    @SerialName("valid")
    val valid: Boolean = false,

    @SerialName("number")
    val number: String? = null,

    @SerialName("local_format")
    val localFormat: String? = null,

    @SerialName("international_format")
    val internationalFormat: String? = null,

    @SerialName("country_prefix")
    val countryPrefix: String? = null,

    @SerialName("country_code")
    val countryCode: String? = null,

    @SerialName("country_name")
    val countryName: String? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("carrier")
    val carrier: String? = null,

    @SerialName("line_type")
    val lineType: String? = null,

    // Error fields
    @SerialName("success")
    val success: Boolean? = null,

    @SerialName("error")
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    @SerialName("code")
    val code: Int? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("info")
    val info: String? = null
)
