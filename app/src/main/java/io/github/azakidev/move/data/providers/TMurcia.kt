package io.github.azakidev.move.data.providers

import androidx.compose.ui.util.fastCoerceAtLeast
import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlIgnoreWhitespace

@Serializable
@SerialName("Envelope")
data class TMurciaResponse(
    @XmlIgnoreWhitespace(true)
    val body: TMurciaBody
)

@Serializable
@SerialName("Body")
data class TMurciaBody(
    @XmlIgnoreWhitespace(true)
    val response: TMurciaRequestResponse
)

@Serializable
@SerialName("GetStopMonitoringResponse")
data class TMurciaRequestResponse(
    @XmlIgnoreWhitespace(true)
    val result: TMurciaRequestResult
)

@Serializable
@SerialName("GetStopMonitoringResult")
data class TMurciaRequestResult(
    @XmlIgnoreWhitespace(true)
    val answer: TMurciaAnswer
)

@Serializable
@SerialName("Answer")
data class TMurciaAnswer(
    @XmlIgnoreWhitespace(true)
    val delivery: TMurciaDelivery
)

@Serializable
@SerialName("StopMonitoringDelivery")
data class TMurciaDelivery(
    @XmlIgnoreWhitespace(true)
    val estimates: List<TMurciaEstimate>
)

@Serializable
@SerialName("MonitoredStopVisit")
data class TMurciaEstimate(
    @XmlIgnoreWhitespace(true)
    val lineInfo: TMurciaLineInfo,
    @XmlElement @SerialName("Delay") val minString: String,
)

@Serializable
@SerialName("MonitoredVehicleJourney")
data class TMurciaLineInfo(
    @XmlElement @SerialName("LineRef") val emblem: String,
    @XmlElement @SerialName("DirectionName") val direction: String,
)


fun parseTMurcia(response: String): List<LineTime> {
    val format = XML {
        defaultPolicy {
            pedantic = false
            ignoreUnknownChildren()
        }
    }

    println(response)
    val string = response.replace(
        "soap:",
        ""
    )

    val estimates = format.decodeFromString<TMurciaResponse>(string)
        .body
        .response
        .result
        .answer
        .delivery
        .estimates

    val response = estimates.groupBy { it.lineInfo.direction }.mapNotNull { (key, value) ->
        val id = value.first().lineInfo.emblem.replace("C", "").replace("R", "").toInt()
        if (value.count() >= 2) {
            LineTime(
                lineId = id,
                destination = key,
                nextTimeFirst = value[0].minString.split(" ").first().toInt().fastCoerceAtLeast(0),
                nextTimeSecond = value[1].minString.split(" ").first().toInt().fastCoerceAtLeast(0)
            )
        } else {
            LineTime(
                lineId = id,
                destination = key,
                nextTimeFirst = value.first().minString.split(" ").first().toInt()
                    .fastCoerceAtLeast(0)
            )
        }
    }

    return response
}