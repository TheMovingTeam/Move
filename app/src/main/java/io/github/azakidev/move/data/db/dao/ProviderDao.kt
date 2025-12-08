package io.github.azakidev.move.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.azakidev.move.data.db.entities.ProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderEntity>)

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getProviderById(id: Int): ProviderEntity?

    @Query("SELECT * FROM providers")
    fun getAllProviders(): Flow<List<ProviderEntity>> // Observe changes

    @Query("DELETE FROM providers")
    fun clearAllProviders()
}