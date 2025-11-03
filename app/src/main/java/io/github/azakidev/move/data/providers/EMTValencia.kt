package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlCData
import nl.adaptivity.xmlutil.serialization.XmlElement
import kotlin.collections.component1
import kotlin.collections.component2

@Serializable
@SerialName("estimacion")
data class EMTValenciaResponse (
    @XmlElement val data: EMTValenciaContent
)

@Serializable
@SerialName("solo_parada")
data class EMTValenciaContent (
    @XmlElement val estimates: List<EMTValenciaEstimate>
)

@Serializable
@SerialName("bus")
data class EMTValenciaEstimate (
    @XmlElement @SerialName("linea") val line: String,
    @XmlElement @XmlCData @SerialName("destino") val destination: String,
    @XmlElement @SerialName("minutos") val min: String
)

fun parseEMTValencia(
    xmlResponse: String,
): List<LineTime> {
    val format = XML {
        defaultPolicy {
            pedantic = false
            ignoreUnknownChildren()
        }
    }
    val parsedResponse = format.decodeFromString<EMTValenciaResponse>(xmlResponse)
    val estimatesByDestination = parsedResponse.data.estimates.groupBy { it.destination }

    val timeList = mutableListOf<LineTime>()

    estimatesByDestination.forEach { (destination, value) ->
        val estimates = value.groupBy { it.line }
        timeList += estimates.mapNotNull { (key, value) ->
            val matchedLine = key
                .replace("C1", "5")
                .replace("C2", "80")
                .replace("C3", "90")
                .toIntOrNull()
            if (matchedLine != null) {
                if (value.count() >= 2) {
                    LineTime(
                        lineId = matchedLine,
                        destination = destination,
                        nextTimeFirst = value[0].min.split(" ").first().toIntOrNull() ?: 0,
                        nextTimeSecond = value[1].min.split(" ").first().toIntOrNull() ?: 0
                    )
                } else {
                    LineTime(
                        lineId = matchedLine,
                        destination = destination,
                        nextTimeFirst = value[0].min.split(" ").first().toIntOrNull() ?: 0,
                    )
                }
            } else {
                null
            }
        }
    }
    return timeList
}