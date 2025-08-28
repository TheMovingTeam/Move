package io.github.azakidev.move.data

import kotlinx.serialization.Serializable


@Serializable
data class ProviderItem(
    val name: String = "DummyProvider",
    val id: Int = 0,
    val versionMajor: Int = 0,
    val versionMinor: Int = 0,

    val lastUpdated: Int = 0,
    val capabilities: List<String> = listOf()
)

@Serializable
data class ProviderListResponse(
    val providers: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProviderListResponse

        return providers.contentEquals(other.providers)
    }

    override fun hashCode(): Int {
        return providers.contentHashCode()
    }
}