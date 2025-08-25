package io.github.azakidev.move.data

import androidx.lifecycle.ViewModel
import io.github.azakidev.move.R

class MoveModel: ViewModel() {
    var lines: List<LineItem> = listOf(
        LineItem(
            lineId = 1,
            lineName = "Line 1",
            lineEmblem = "01",
            stops = listOf(1, 3, 5)
        ),
        LineItem(
            lineId = 2,
            lineName = "Line 2",
            lineEmblem = "02",
            stops = listOf(2, 3, 4)
        ),
        LineItem(
            lineId = 3,
            lineName = "Line 3",
            lineEmblem = "03",
            stops = listOf(1, 2, 4, 5)
        ),
        LineItem(
            lineId = 4,
            lineName = "Line 4",
            lineEmblem = "04",
            stops = listOf(2, 4, 5)
        ),
        LineItem(
            lineId = 5,
            lineName = "Line 5",
            lineEmblem = "05",
            stops = listOf(1, 2, 3, 4, 5)
        ),
        LineItem(
            lineId = 6,
            lineName = "Line 6",
            lineEmblem = "06",
            stops = listOf(1, 4, 5)
        ),
        LineItem(
            lineId = 1,
            lineName = "Line 1",
            lineEmblem = "01",
            stops = listOf(1, 3, 5)
        ),
        LineItem(
            lineId = 2,
            lineName = "Line 2",
            lineEmblem = "02",
            stops = listOf(2, 3, 4)
        ),
        LineItem(
            lineId = 3,
            lineName = "Line 3",
            lineEmblem = "03",
            stops = listOf(1, 2, 4, 5)
        ),
        LineItem(
            lineId = 4,
            lineName = "Line 4",
            lineEmblem = "04",
            stops = listOf(2, 4, 5)
        ),
        LineItem(
            lineId = 5,
            lineName = "Line 5",
            lineEmblem = "05",
            stops = listOf(1, 2, 3, 4, 5)
        ),
        LineItem(
            lineId = 6,
            lineName = "Line 6",
            lineEmblem = "06",
            stops = listOf(1, 4, 5)
        ),
    )

    var stops: List<StopItem> = listOf(
        StopItem(
            stopId = 1,
            stopName = "First Station",
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
            stopName = "Second Station",
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
            stopName = "Third Station",
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
            stopName = "Fourth Station",
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