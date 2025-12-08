package io.github.azakidev.move.worker

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.azakidev.move.data.db.MoveDatabase
import io.github.azakidev.move.data.db.entities.toLineItem
import io.github.azakidev.move.data.db.entities.toProviderItem
import io.github.azakidev.move.data.db.entities.toStopItem
import io.github.azakidev.move.widget.FavStopWidget
import io.github.azakidev.move.utils.LogTags
import io.github.azakidev.move.utils.fetchStopTime
import io.github.azakidev.move.data.items.ProviderItem

/**
 * A worker to periodically update the FavStopWidget with new stop times.
 */
class FavStopUpdaterWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val KEY_APP_WIDGET_ID = "app_widget_id"
        const val KEY_STOP_ID = "stop_id"
    }

    override suspend fun doWork(): Result {
        val appWidgetId = inputData.getInt(KEY_APP_WIDGET_ID, -1)
        val stopId = inputData.getInt(KEY_STOP_ID, -1)

        if (appWidgetId == -1 || stopId == -1) {
            Log.e(LogTags.Worker.name, "Invalid appWidgetId or stopId for FavStopUpdaterWorker")
            return Result.failure()
        }

        try {
            // Fetch the StopItem from the database
            val stopItem =
                MoveDatabase.getDatabase(context).stopDao().getStopById(stopId)?.toStopItem()

            if (stopItem == null) {
                Log.e(LogTags.Worker.name, "StopItem not found for id: $stopId")
                return Result.failure()
            }

            // Fetch the lines for the provider
            val lineItems = MoveDatabase
                .getDatabase(context)
                .lineDao()
                .getLinesForProvider(stopItem.provider)
                .map { it.toLineItem() }

            val providerItem =
                MoveDatabase.getDatabase(context).providerDao().getProviderById(stopItem.provider)
                    ?.toProviderItem() ?: ProviderItem()

            fetchStopTime(providerItem, stopItem, lineItems)

            GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId).let { glanceId ->
                FavStopWidget().update(context, glanceId)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(LogTags.Worker.name, "Error updating favorite stop widget: ${e.message}", e)
            return Result.failure()
        }
    }
}
