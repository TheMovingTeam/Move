package io.github.azakidev.move.data.providers

import android.util.Log
import android.util.Xml
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

data class VectaliaResponse(
    val lineEmblem: String,
    val destination: String,
    val imageUrl: String,
    val firstEstimateSeconds: Int?,
    val secondEstimateSeconds: Int?
)

@Throws(XmlPullParserException::class, IOException::class)
fun parseVectaliaResponse(xmlString: String): List<VectaliaResponse> {
    val estimations = mutableListOf<VectaliaResponse>()
    val parser: XmlPullParser = Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
    parser.setInput(StringReader(xmlString))

    var eventType = parser.eventType
    var currentEstimation: MutableMap<String, String?> = mutableMapOf()
    var currentTag: String? = null
    val secondsList = mutableListOf<Int>()

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                currentTag = parser.name
                if (currentTag == "estimation") {
                    currentEstimation = mutableMapOf()
                    secondsList.clear()
                }
            }
            XmlPullParser.TEXT -> {
                val text = parser.text.trim()
                if (text.isNotEmpty()) {
                    when (currentTag) {
                        "line" -> currentEstimation["lineEmblem"] = text
                        "destino" -> currentEstimation["destination"] = text
                        "imageIcon" -> currentEstimation["imageUrl"] = text
                        "seconds" -> {
                            try {
                                secondsList.add(text.toInt())
                            } catch (e: NumberFormatException) {
                                // Handle cases where 'seconds' is not a valid integer
                                Log.w("XmlParser", "Invalid number format for seconds: $text")
                            }
                        }
                    }
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "estimation") {
                    val firstEstimate = if (secondsList.isNotEmpty()) secondsList[0] else null
                    val secondEstimate = if (secondsList.size > 1) secondsList[1] else null

                    val estimation = VectaliaResponse(
                        lineEmblem = currentEstimation["lineEmblem"] ?: "",
                        destination = currentEstimation["destination"] ?: "",
                        imageUrl = currentEstimation["imageUrl"] ?: "",
                        firstEstimateSeconds = firstEstimate,
                        secondEstimateSeconds = secondEstimate
                    )
                    estimations.add(estimation)
                }
                currentTag = null // Reset current tag
            }
        }
        eventType = parser.next()
    }
    return estimations
}

fun parseVectaliaTimes(
    response:String,
    lines: List<LineItem>
): List<LineTime> {
    println(response)
    val estimations = parseVectaliaResponse(response)
    val timeList: List<LineTime> = estimations.mapNotNull { estimation ->
        val matchingLine = lines.find { line ->
            line.name.equals(estimation.destination, ignoreCase = true) ||
                    line.emblem == estimation.lineEmblem // Fallback or additional check using emblem
        }
        val time =
            if ((matchingLine != null) && (estimation.firstEstimateSeconds != null)) {
                LineTime(
                    lineId = matchingLine.id,
                    nextTimeFirst = estimation.firstEstimateSeconds.div(60),
                    nextTimeSecond = estimation.secondEstimateSeconds?.div(60)
                )
            } else {
                null
            }
        time
    }
    return timeList
}