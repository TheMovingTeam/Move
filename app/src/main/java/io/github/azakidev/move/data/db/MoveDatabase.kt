package io.github.azakidev.move.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.azakidev.move.data.db.dao.LineDao
import io.github.azakidev.move.data.db.dao.ProviderDao
import io.github.azakidev.move.data.db.dao.StopDao
import io.github.azakidev.move.data.db.entities.LineEntity
import io.github.azakidev.move.data.db.entities.ProviderEntity
import io.github.azakidev.move.data.db.entities.StopEntity

@Database(
    entities = [ProviderEntity::class, LineEntity::class, StopEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MoveDatabase : RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun lineDao(): LineDao
    abstract fun stopDao(): StopDao

    companion object {
        @Volatile
        private var INSTANCE: MoveDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lines ADD COLUMN path TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE providers DROP COLUMN versionMajor")
                db.execSQL("ALTER TABLE providers DROP COLUMN versionMinor")
            }
        }

        fun getDatabase(context: Context): MoveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, MoveDatabase::class.java, "move_database"
                )
                    // Add migrations if you change the schema later
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}