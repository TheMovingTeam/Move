package io.github.azakidev.move.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Query("SELECT * FROM stops WHERE providerId = :providerId")
    fun getStopsForProvider(providerId: Int): Flow<List<StopEntity>>

    @Query("DELETE FROM stops WHERE providerId = :providerId")
    suspend fun deleteStopsForProvider(providerId: Int)

    @Query("SELECT * FROM stops WHERE providerId IN (:providerIds)")
    fun getStopsForProviders(providerIds: List<Int>): Flow<List<StopEntity>>
    @Query("DELETE FROM stops")
    fun clearAllStops()
}