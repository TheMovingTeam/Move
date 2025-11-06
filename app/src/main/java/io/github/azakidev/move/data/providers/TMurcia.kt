package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlIgnoreWhitespace
import nl.adaptivity.xmlutil.serialization.XmlSerialName

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
    @XmlSerialName("GetStopMonitoringResponse", "http://tempuri.org/", "")
    val response: TMurciaRequestResponse
)

@Serializable
data class TMurciaRequestResponse(
    @XmlIgnoreWhitespace(true)
    @XmlSerialName("GetStopMonitoringResult", "http://tempuri.org/", "")
    val result: TMurciaRequestResult
)

@Serializable
data class TMurciaRequestResult(
    @XmlIgnoreWhitespace(true)
    @XmlSerialName("Answer", "", "")
    val answer: TMurciaAnswer
)

@Serializable
data class TMurciaAnswer(
    @XmlIgnoreWhitespace(true)
    @XmlSerialName("StopMonitoringDelivery", "http://www.siri.org.uk/siri", "")
    val delivery: TMurciaDelivery
)

@Serializable
data class TMurciaDelivery(
    @XmlIgnoreWhitespace(true)
    @XmlSerialName("MonitoredStopVisit", "http://www.siri.org.uk/siri", "")
    val estimates: List<TMurciaEstimate>
)

@Serializable
data class TMurciaEstimate(
    @XmlIgnoreWhitespace(true)
    @XmlSerialName("MonitoredVehicleJourney", "http://www.siri.org.uk/siri", "")
    val lineInfo: TMurciaLineInfo,
)

@Serializable
data class TMurciaLineInfo(
    @XmlElement @XmlSerialName("LineRef", "http://www.siri.org.uk/siri", "") val emblem: String,
    @XmlElement @XmlSerialName("DestinationName", "http://www.siri.org.uk/siri", "") val direction: String,
    @XmlElement @XmlSerialName("Delay", "http://www.siri.org.uk/siri", "") val minString: String,
)



fun parseTMurcia(response: String): List<LineTime> {
    val format = XML {
        defaultPolicy {
            pedantic = false
            ignoreUnknownChildren()
        }
    }

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

    println(estimates.groupBy { it.lineInfo.direction })

    val response = estimates.groupBy { it.lineInfo.direction }.mapNotNull { (destination, value) ->
        value.groupBy { it.lineInfo.emblem }.mapNotNull { (emblem, value) ->
            val id = emblem.replace("C", "").replace("R", "").toInt()
            if (value.count() >= 2) {
                LineTime(
                    lineId = id,
                    destination = destination,
                    nextTimeFirst = value[0].lineInfo.minString.timeFmt(),
                    nextTimeSecond = value[1].lineInfo.minString.timeFmt()
                )
            } else {
                LineTime(
                    lineId = id,
                    destination = destination,
                    nextTimeFirst = value.first().lineInfo.minString.timeFmt()
                )
            }
        }
    }.flatten()

    return response
}

private fun String.timeFmt(): Int {
    return this.split(" ")
        .first()
        .replace("-","")
        .toInt()
}
