package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.LineTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
data class TMPMurciaResponse(
    val estimates: List<TMPMurciaEstimates>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable @JsonIgnoreUnknownKeys
data class TMPMurciaEstimates(
    @SerialName("line_id") val lineId: Int,
    @SerialName("synoptic") val subline: String,
    @SerialName("real_time") val timeString: String,
)

fun parseTMPMurcia(response: String, lines: List<LineItem>): List<LineTime> {
    val string = """{ "estimates":  $response }"""

    val estimates = Json.decodeFromString<TMPMurciaResponse>(string).estimates

    val response = estimates.mapNotNull { it ->
        val line = lines.find { lineItem -> lineItem.id == it.lineId }
        val min = it.timeString.split(" ").mapNotNull { it.toIntOrNull() }.toMutableList()
        min.ifEmpty { min += 0 }
        if (line != null) {
            LineTime(
                lineId = it.lineId,
                nextTimeFirst = min.first(),
                emblemOverride = line.emblem + it.subline
            )
        } else null
    }

    return response
}