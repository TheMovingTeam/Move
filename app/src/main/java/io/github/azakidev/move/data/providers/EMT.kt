package io.github.azakidev.move.data.providers

import android.util.Xml
import io.github.azakidev.move.data.LineTime
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader
import kotlin.collections.component1
import kotlin.collections.component2

data class EMTResponse (
    val linea: Int,
    val destino: String?,
    val min: Int
)

@Throws(XmlPullParserException::class, IOException::class)
fun parseEMTResponse(xmlString: String): List<EMTResponse> {
    val estimations = mutableListOf<EMTResponse>()
    val parser: XmlPullParser = Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
    parser.setInput(StringReader(xmlString))

    var eventType = parser.eventType
    var currentTag: String? = null

    var currentLine: Int? = null
    var currentDestino: String? = null
    var min: Int? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                currentTag = parser.name
                if (currentTag == "bus") {
                    // Reset variables for new tag
                    currentLine = null
                    currentDestino = null
                    min = null
                }
            }
            XmlPullParser.TEXT -> {
                val text = parser.text?.trim()
                if (!text.isNullOrEmpty()) {
                    when (currentTag) {
                        "linea" -> {
                            currentLine = text
                                .replace("C1", "5")
                                .replace("C2", "80")
                                .replace("C3", "90")
                                .toIntOrNull()
                        }
                        "destino" -> currentDestino = text
                        "minutos" -> {
                            // API returns "Next" o "X min."
                            min = text.split(" ").first().toIntOrNull() ?: 0
                        }
                    }
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "bus") {
                    if (currentLine != null && min != null) {
                        estimations.add(
                            EMTResponse(
                                linea = currentLine,
                                destino = currentDestino,
                                min = min
                            )
                        )
                    }
                }
                currentTag = null // Clean current tag
            }
        }
        eventType = parser.next()
    }
    return estimations
}

fun parseEMTTimes(
    response: String,
): List<LineTime> {
    val estimations = parseEMTResponse(response)

    val estimatesByDestination = estimations.groupBy { it.destino }

    val timeList = mutableListOf<LineTime>()

    estimatesByDestination.forEach { (_, value) ->
        val estimates = value.groupBy { it.linea }
        timeList += estimates.map { (key, value) ->
            if (value.count() >= 2) {
                LineTime(
                    lineId = key,
                    destination = value[0].destino,
                    nextTimeFirst = value[0].min,
                    nextTimeSecond = value[1].min
                )
            } else {
                LineTime(
                    lineId = key,
                    destination = value[0].destino,
                    nextTimeFirst = value[0].min,
                    nextTimeSecond = null
                )
            }
        }
    }
    return timeList
}