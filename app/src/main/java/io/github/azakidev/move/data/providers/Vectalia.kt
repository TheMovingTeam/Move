package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("result")
data class VectaliaResponse(
    @XmlElement val estimates: List<VectaliaEstimate>
)

@Serializable
@SerialName("estimation")
data class VectaliaEstimate(
    @XmlElement @SerialName("line") val lineEmblem: String,
    @XmlElement @SerialName("destino") val destination: String,
    @XmlElement @SerialName("seconds") val seconds: List<Int>,
)

fun parseVectaliaTimes(
    xmlResponse: String, lines: List<LineItem>
): List<LineTime> {
    val format = XML {
        defaultPolicy {
            pedantic = false
            ignoreUnknownChildren()
        }
    }
    val responseParsed = format.decodeFromString<VectaliaResponse>(xmlResponse)
    val response = mutableListOf<LineTime>()

    response += responseParsed.estimates.mapNotNull { estimate ->
        val matchingLine = lines.find { line ->
            (line.emblem == estimate.lineEmblem && line.name.contains(estimate.destination)) || line.emblem == estimate.lineEmblem
        }

        if (matchingLine != null) {
            if (estimate.seconds.count() >= 2) {
                LineTime(
                    lineId = matchingLine.id,
                    destination = estimate.destination,
                    nextTimeFirst = estimate.seconds[0].div(60),
                    nextTimeSecond = estimate.seconds[1].div(60)
                )
            } else {
                LineTime(
                    lineId = matchingLine.id,
                    nextTimeFirst = estimate.seconds.first().div(60)
                )
            }
        } else {
            null
        }
    }

    return response
}