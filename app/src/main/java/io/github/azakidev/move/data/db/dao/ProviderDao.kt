package io.github.azakidev.move.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.azakidev.move.data.db.entities.ProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderEntity>)

    // Helper function to insert a single provider
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    // Helper function to delete a provider by ID
    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProviderById(id: Int)

    @Transaction // Ensures that all operations within this function are atomic
    suspend fun replaceProviderId(oldId: Int, newId: Int) {
        val oldProvider = getProviderById(oldId)
        oldProvider?.let { existingProvider ->
            val newProvider = existingProvider.copy(id = newId)
            deleteProviderById(oldId)
            insertProvider(newProvider)
        }
    }

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getProviderById(id: Int): ProviderEntity?

    @Query("SELECT * FROM providers")
    fun getAllProviders(): Flow<List<ProviderEntity>> // Observe changes

    @Query("DELETE FROM providers")
    fun clearAllProviders()
}