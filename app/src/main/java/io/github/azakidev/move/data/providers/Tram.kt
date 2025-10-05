package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable @JsonIgnoreUnknownKeys
data class TramResponse(
    val previsiones: List<TramTimeEstimate>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable @JsonIgnoreUnknownKeys
data class TramTimeEstimate(
    @SerialName("line_id") val id: Int,
    val trains: List<TramTrain>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable @JsonIgnoreUnknownKeys
data class TramTrain(
    val destino: String,
    val seconds: Int,
    @SerialName("line_id") val id: Int
)

fun parseTramResponse(response: String): List<LineTime> {
    val responseJson = Json.decodeFromString<TramResponse>(response)
    val estimatesByDestination = responseJson.previsiones.map { it.trains }.flatten().groupBy { it.destino }

    val response = mutableListOf<LineTime>()

    estimatesByDestination.forEach { (_, value) ->
        val estimates = value.groupBy { it.id }
        println(estimates)
        response += estimates.map { (key, value) ->
            if (value.count() >= 2) {
                LineTime(
                    lineId = key,
                    destination = value[0].destino,
                    nextTimeFirst = value[0].seconds / 60,
                    nextTimeSecond = value[1].seconds / 60
                )
            } else {
                LineTime(
                    lineId = key,
                    destination = value[0].destino,
                    nextTimeFirst = value[0].seconds / 60,
                    nextTimeSecond = null
                )
            }
        }
    }
    return response
}