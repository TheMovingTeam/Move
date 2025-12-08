package io.github.azakidev.move.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.azakidev.move.data.db.entities.LineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<LineEntity>)

    @Query("DELETE FROM lines WHERE providerId = :providerId")
    suspend fun deleteLinesForProvider(providerId: Int)

    @Query("SELECT * FROM lines WHERE providerId IN (:providerIds)")
    fun getLinesForProviders(providerIds: List<Int>): Flow<List<LineEntity>>

    @Query("SELECT * FROM lines WHERE providerId IN (:providerId)")
    fun getLinesForProvider(providerId: Int): List<LineEntity>

    @Query("SELECT * FROM lines")
    fun getAllLines(): Flow<List<LineEntity>>

    @Query("DELETE FROM lines")
    fun clearAllLines()
}