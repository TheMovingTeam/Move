@file:OptIn(ExperimentalSerializationApi::class)

package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlin.collections.component1
import kotlin.collections.component2

@Serializable @JsonIgnoreUnknownKeys
data class TranviaMurciaResponse(
    val data: TranviaMurciaData
)

@Serializable @JsonIgnoreUnknownKeys
data class TranviaMurciaData(
    @SerialName("tiempo_real") val estimates: List<TranviaMurciaEstimates>
)

@Serializable @JsonIgnoreUnknownKeys
data class TranviaMurciaEstimates (
    @SerialName("error_code") val error: Int,
    @SerialName("tiempo_real") val min: Int,
    @SerialName("direccion") val direction: String,
)

fun parseTranviaMurcia(response: String): List<LineTime> {
    val responseJson = Json.decodeFromString<TranviaMurciaResponse>(
        response.replace("entrando", "0")
    )
    val estimatesByDestination = responseJson.data.estimates.filter{ it.error == 0 }.groupBy { it.direction }

    val response = mutableListOf<LineTime>()

    println(estimatesByDestination)

    response += estimatesByDestination.map { (key, value) ->
        if (value.count() >= 2) {
            LineTime(
                lineId = 1,
                destination = key,
                nextTimeFirst = value[0].min,
                nextTimeSecond = value[1].min
            )
        } else {
            LineTime(
                lineId = 1,
                destination = key,
                nextTimeFirst = value[0].min,
                nextTimeSecond = null
            )
        }
    }

    return response
}