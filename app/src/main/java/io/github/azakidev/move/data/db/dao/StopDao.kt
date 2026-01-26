package io.github.azakidev.move.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.azakidev.move.data.db.entities.StopEntity
import io.github.azakidev.move.data.items.StopKey
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Query("SELECT * FROM stops WHERE providerId = :providerId")
    fun getStopsForProvider(providerId: Int): Flow<List<StopEntity>>

    @Query("DELETE FROM stops WHERE providerId = :providerId")
    suspend fun deleteStopsForProvider(providerId: Int)

    @Query("SELECT * FROM stops WHERE id = :id")
    suspend fun getStopById(id: Int): StopEntity?

    @Query("SELECT * FROM stops WHERE id = :id AND providerId = :provider")
    suspend fun getStopByKey(id: Int, provider: Int): StopEntity?

    @Query("SELECT * FROM stops WHERE providerId IN (:providerIds)")
    fun getStopsForProviders(providerIds: List<Int>): Flow<List<StopEntity>>

    @Query("SELECT * FROM stops")
    fun getAllStops(): Flow<List<StopEntity>>

    @Query("DELETE FROM stops")
    fun clearAllStops()
}