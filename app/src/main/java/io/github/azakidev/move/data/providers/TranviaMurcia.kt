@file:OptIn(ExperimentalSerializationApi::class)

package io.github.azakidev.move.data.providers

import io.github.azakidev.move.data.LineTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlIgnoreWhitespace
import kotlin.collections.component1
import kotlin.collections.component2

@Serializable
@SerialName("ArrayOfEstimacionHoraria")
data class TranviaMurciaResponse(
    @XmlIgnoreWhitespace(true)
    val estimates: List<TranviaMurciaEstimates>
)

@Serializable
@SerialName("EstimacionHoraria")
data class TranviaMurciaEstimates(
    @XmlIgnoreWhitespace(true)
    @XmlElement @SerialName("IdParada") val stopId: Int,
    @XmlElement @SerialName("Linea") val lineEmblem: String,
    @XmlElement @SerialName("Destino") val direction: String,
    @XmlElement @SerialName("TiempoProximoTren") val min: Int,
    @XmlElement @SerialName("Via") val via: Int,
    @XmlElement @SerialName("Tranvia") val train: String,
)

@OptIn(ExperimentalXmlUtilApi::class)
fun parseTranviaMurcia(response: String): List<LineTime> {
    val responseParsed = XML.decodeFromString<TranviaMurciaResponse>(
            response.replace("entrando", "0")
        )
    val estimatesByDestination = responseParsed.estimates.groupBy { it.direction }

    val response = mutableListOf<LineTime>()

    println(estimatesByDestination)

    response += estimatesByDestination.map { (key, value) ->
        if (value.count() >= 2) {
            LineTime(
                lineId = 1,
                destination = key,
                nextTimeFirst = value[0].min,
                nextTimeSecond = value[1].min,
                emblemOverride = value[0].lineEmblem
            )
        } else {
            LineTime(
                lineId = 1,
                destination = key,
                nextTimeFirst = value[0].min,
                nextTimeSecond = null,
                emblemOverride = value[0].lineEmblem
            )
        }
    }

    return response
}