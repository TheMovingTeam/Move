package io.github.azakidev.move.data

import kotlinx.serialization.Serializable


@Serializable
data class ProviderItem(
    val name: String = "DummyProvider",
    val description: String = "",

    val id: Int = 0,
    val versionMajor: Int = 0,
    val versionMinor: Int = 0,

    val lastUpdated: Int = 0,
    val capabilities: List<Capabilities> = listOf(),

    val timeSource: String = "",
    val timeFormat: TimeFormat = TimeFormat(),

    val qrFormat: String = ""
)

enum class Capabilities {
    Time,
    DoubleTime,
    Notifications,
    QrScan,
//    Geo,
}

@Serializable
data class TimeFormat(
    val type: TimeType = TimeType.IntArray,
    val regex: String? = null,
)

enum class TimeType {
    IntArray,
    XML,
    JSON,
    String,
}

@Serializable
data class ProviderListResponse(
    val providers: List<String>
)