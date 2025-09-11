package io.github.azakidev.move.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ProviderEntity::class,
        LineEntity::class,
        StopEntity::class
               ],
    version = 1,
    exportSchema = false)
@TypeConverters(Converters::class) // Add your TypeConverters here
abstract class MoveDatabase : RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun lineDao(): LineDao
    abstract fun stopDao(): StopDao

    companion object {
        @Volatile
        private var INSTANCE: MoveDatabase? = null

        fun getDatabase(context: Context): MoveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoveDatabase::class.java,
                    "move_database"
                )
                    // Add migrations if you change the schema later
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}