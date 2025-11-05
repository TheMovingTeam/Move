@file:OptIn(ExperimentalSerializationApi::class)

package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
data class MetrobusValenciaResponse(
    val estimates: List<MetrobusValenciaEstimates>
)

@Serializable @JsonIgnoreUnknownKeys
data class MetrobusValenciaEstimates(
    @SerialName("line") val lineEmblem: String,
    @SerialName("route")  val destination: String,
    val estimations: List<MetrobusValenciaEstimation>
)

@Serializable @JsonIgnoreUnknownKeys
data class MetrobusValenciaEstimation(
    @SerialName("minutesToArrival") val min: Int,
)

fun parseMetrobusValencia(response: String, lines: List<LineItem>): List<LineTime> {
    val string = """{ "estimates":  $response }"""

    val estimates = Json.decodeFromString<MetrobusValenciaResponse>(string).estimates

    val response = estimates.mapNotNull { estimate ->
        val line = lines.find { it.emblem == estimate.lineEmblem }

        if (line != null) {
            if (estimate.estimations.count() > 1) {
                LineTime(
                    lineId =  line.id,
                    destination = estimate.destination,
                    nextTimeFirst = estimate.estimations[0].min,
                    nextTimeSecond = estimate.estimations[1].min
                )
            } else {
                LineTime(
                    lineId =  line.id,
                    destination = estimate.destination,
                    nextTimeFirst = estimate.estimations[0].min
                )
            }
        } else {
            null
        }
    }

    return response
}