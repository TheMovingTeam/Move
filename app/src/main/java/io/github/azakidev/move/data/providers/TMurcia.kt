package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.items.LineTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlIgnoreWhitespace
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.until
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
    @XmlElement @XmlSerialName(
        "DestinationName",
        "http://www.siri.org.uk/siri",
        ""
    ) val direction: String,
    @XmlElement @XmlSerialName("Delay", "http://www.siri.org.uk/siri", "") val minString: String,
    @XmlElement @XmlSerialName(
        "MonitoredCall",
        "http://www.siri.org.uk/siri",
        ""
    ) val mc: TMurciaMonitoredCall,
)

@Serializable
data class TMurciaMonitoredCall(
    @XmlElement @XmlSerialName(
        "ExpectedArrivalTime",
        "http://www.siri.org.uk/siri",
        ""
    ) val eta: String,
    @XmlElement @XmlSerialName(
        "ExpectedDepartureTime",
        "http://www.siri.org.uk/siri",
        ""
    ) val etd: String,
)

@OptIn(ExperimentalTime::class)
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

    val now = Clock.System.now()

    val response = estimates.groupBy { it.lineInfo.direction }.mapNotNull { (destination, value) ->
        value
            .filterNot {
                now.until(
                    LocalDateTime.parse(it.lineInfo.mc.eta.take(19))
                        .toInstant(TimeZone.of("Europe/Madrid")),
                    DateTimeUnit.MINUTE
                ) < 0
            }
            .groupBy { it.lineInfo.emblem }
            .mapNotNull { (emblem, value) ->
                val id = emblem.replace("C", "").replace("R", "").toInt()
                if (value.count() >= 2) {
                    val date1 = LocalDateTime.parse(value[0].lineInfo.mc.eta.take(19))
                    val min1 = now.until(
                        date1.toInstant(TimeZone.of("Europe/Madrid")),
                        DateTimeUnit.MINUTE
                    ).toInt()
                    val date2 = LocalDateTime.parse(value[1].lineInfo.mc.eta.take(19))
                    var min2: Int? = now.until(
                        date2.toInstant(TimeZone.of("Europe/Madrid")),
                        DateTimeUnit.MINUTE
                    ).toInt()
                    if (min1 == min2) {
                        min2 = null
                    }
                    LineTime(
                        lineId = id,
                        destination = destination,
                        nextTimeFirst = min1,
                        nextTimeSecond = min2
                    )
                } else {
                    val date1 = LocalDateTime.parse(value[0].lineInfo.mc.eta.take(19))
                    val min1 = now.until(
                        date1.toInstant(TimeZone.of("Europe/Madrid")),
                        DateTimeUnit.MINUTE
                    ).toInt()
                    LineTime(
                        lineId = id,
                        destination = destination,
                        nextTimeFirst = min1
                    )
                }
            }
    }.flatten()

    return response
}
