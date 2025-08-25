package io.github.azakidev.move.data

import androidx.lifecycle.ViewModel
import io.github.azakidev.move.R

class MoveModel: ViewModel() {
    var lines: List<LineItem> = listOf(
        LineItem(
            lineId = 1,
            lineName = "Line 1",
            lineEmblem = "L1",
            stops = listOf(1, 3, 5)
        ),
        LineItem(
            lineId = 2,
            lineName = "Line 2",
            lineEmblem = "6",
            stops = listOf(2, 3, 4)
        ),
        LineItem(
            lineId = 3,
            lineName = "Line 3",
            lineEmblem = "03",
            stops = listOf(1, 2, 4, 5)
        ),
    )

    var stops: List<StopItem> = listOf(
        StopItem(
            stopId = 1,
            stopName = "Them",
            image = R.drawable.nathofjoy,
            lineTimes = listOf(
                LineTime(
                    lineId = 3,
                    nextTime = 5
                ),
            )
        ),
        StopItem(
            stopId = 2,
            stopName = "Painting",
            image = R.drawable.heart_of_sea,
            lineTimes = listOf(
                LineTime(
                    lineId = 2,
                    nextTime = 4
                ),
                LineTime(
                    lineId = 1,
                    nextTime = 4
                ),
            )
        ),
        StopItem(
            stopId = 3,
            stopName = "They",
            image = R.drawable.nathofjoy,
            lineTimes = listOf(
                LineTime(
                    lineId = 1,
                    nextTime = 3
                ),
            )
        ),
        StopItem(
            stopId = 4,
            stopName = "Place",
            image = R.drawable.heart_of_sea,
            lineTimes = listOf(
                LineTime(
                    lineId = 2,
                    nextTime = 2
                ),
            )
        ),
        StopItem(
            stopId = 5,
            stopName = "Dalf station",
            image = R.drawable.nathofjoy,
            lineTimes = listOf(
                LineTime(
                    lineId = 3,
                    nextTime = 1
                ),
            )
        )
    )
    var favouriteStops: List<Int> = listOf()
}