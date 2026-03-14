@file:OptIn(ExperimentalSerializationApi::class)

package io.github.azakidev.move.data.items

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
@JsonIgnoreUnknownKeys
data class ProviderRepo(
    val providers: List<String>,
    val groups: List<ProviderGroup> = emptyList()
)

@Serializable
@JsonIgnoreUnknownKeys
data class ProviderGroup(
    val name: String,
    val providers: List<String>
)
