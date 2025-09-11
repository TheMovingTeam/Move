package io.github.azakidev.move.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProviders(providers: List<ProviderEntity>)

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getProviderById(id: Int): ProviderEntity?

    @Query("SELECT * FROM providers")
    fun getAllProviders(): Flow<List<ProviderEntity>> // Observe changes

    @Query("SELECT * FROM providers WHERE id IN (:ids)")
    fun getProvidersByIds(ids: List<Int>): Flow<List<ProviderEntity>>
    @Query("DELETE FROM providers")
    fun clearAllProviders()
}