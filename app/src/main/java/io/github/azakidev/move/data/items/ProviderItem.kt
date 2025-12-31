package io.github.azakidev.move.data.items

import kotlinx.serialization.Serializable


@Serializable
data class ProviderItem(
    val name: String = "DummyProvider",
    val description: String = "",

    val id: Int = -1,
    val versionMajor: Int = 0,
    val versionMinor: Int = 0,

    val lastUpdated: Int = 0,
    val capabilities: List<Capabilities> = emptyList(),

    val timeSource: String = "",

    val qrFormat: String = ""
)

enum class Capabilities {
    Time,
    DoubleTime,
    Notifications,

    ComId,
    QrScan,
    Geo,
    Unsafe
}

@Serializable
data class ProviderListResponse(
    val providers: List<String>
)