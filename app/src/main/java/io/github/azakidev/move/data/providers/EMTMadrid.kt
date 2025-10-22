@file:OptIn(ExperimentalSerializationApi::class)

package io.github.azakidev.move.data.providers

import android.util.Log
import com.google.gson.annotations.SerializedName
import io.github.azakidev.move.LogTags
import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.collections.plusAssign

@Serializable
@JsonIgnoreUnknownKeys
data class TokenResponse(
    val data: List<TokenData>
)

@Serializable
@JsonIgnoreUnknownKeys
data class TokenData(
    val idUser: String
)

const val TOKEN_URL = "https://api.mpass.mobi/v1/core/identity/login/integrator"
const val CLIENT_ID = "428b01e6-693c-4f7f-a11e-3bb923420587"

@Serializable
data class Content(
    val passKey: String = "504fea88211f2f90633f964189b7696037d65cc3a5f47b8fa1d5ea5e34db0239ad2e068851e72be0cec125779224749e3bc236c1b7af39d8a3d398e99223f058",
    @SerializedName("X-ClientId") val id: String = CLIENT_ID
)

fun fetchEMTMadridToken(client: OkHttpClient): String {
    val requestBody = Content()
    val body = Json.encodeToString(Content.serializer(), requestBody)
    val request = Request.Builder()
        .header("X-ClientId", CLIENT_ID)
//        .header("Content-Type", "application/json")
        .header("debug", "1")
        .post(
           body.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
        .url(TOKEN_URL)
        .build()

    try {
        val response = client.newCall(request).execute()
        println(response.body!!.string())
        val data = Json.decodeFromString<TokenResponse>(response.body!!.string()).data
        return data.first().idUser
    } catch (e: Exception) {
        Log.e(
            LogTags.Networking.name,
            "Failed to fetch authentication token for EMT Madrid",
            e
        )
    }
    return ""
}

@Serializable @JsonIgnoreUnknownKeys
data class EMTMadridEstimateResponse(
    val data: List<EMTMadridEstimateData>
)

@Serializable @JsonIgnoreUnknownKeys
data class EMTMadridEstimateData(
    @SerializedName("Arrive") val estimates: List<EMTMadridEstimates>
)

@Serializable @JsonIgnoreUnknownKeys
data class EMTMadridEstimates(
    val line: String,
    val destination: String,
    val estimateArrive: Int
)

fun parseEMTMadrid(response: String): List<LineTime> {
    val responseJson = Json.decodeFromString<EMTMadridEstimateResponse>(response)
    val estimatesByDestination = responseJson.data.first().estimates.groupBy { it.destination }

    val response = mutableListOf<LineTime>()

    estimatesByDestination.forEach { (_, value) ->
        val estimates = value.groupBy { it.line }
        response += estimates.map { (key, value) ->
            if (value.count() >= 2) {
                LineTime(
                    lineId = key.toInt(),
                    destination = value[0].destination,
                    nextTimeFirst = value[0].estimateArrive / 60,
                    nextTimeSecond = value[1].estimateArrive / 60
                )
            } else {
                LineTime(
                    lineId = key.toInt(),
                    destination = value[0].destination,
                    nextTimeFirst = value[0].estimateArrive / 60,
                    nextTimeSecond = null
                )
            }
        }
    }

    return response
}