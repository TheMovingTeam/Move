package io.github.azakidev.move.data.items

data class StopKey (
    val stopId: Int,
    val providerId: Int,
) {
    constructor(stopItem: StopItem) : this(stopItem.id, stopItem.provider)
}

fun StopItem.toKey() : StopKey {
    return StopKey(this)
}