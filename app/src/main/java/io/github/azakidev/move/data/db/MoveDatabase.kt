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
    version = 4,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate lines table to enable ON UPDATE CASCADE
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `lines_new` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `id` INTEGER NOT NULL, `providerId` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, `emblem` TEXT NOT NULL, 
                    `color` TEXT, `stops` TEXT NOT NULL, 
                    `path` TEXT, FOREIGN KEY(`providerId`) REFERENCES `providers`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )
                    """.trimIndent())
                db.execSQL("INSERT INTO `lines_new` (`localId`, `id`, `providerId`, `name`, `emblem`, `color`, `stops`, `path`) SELECT `localId`, `id`, `providerId`, `name`, `emblem`, `color`, `stops`, `path` FROM `lines`")
                db.execSQL("DROP TABLE `lines`")
                db.execSQL("ALTER TABLE `lines_new` RENAME TO `lines`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lines_providerId` ON `lines` (`providerId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_lines_id_providerId` ON `lines` (`id`, `providerId`)")

                // Recreate stops table to enable ON UPDATE CASCADE
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stops_new` (
                    `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `id` INTEGER NOT NULL, `comId` INTEGER, `providerId` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, `lines` TEXT NOT NULL, 
                    `geoX` REAL, 
                    `geoY` REAL, 
                    `notifications` TEXT NOT NULL, 
                    FOREIGN KEY(`providerId`) REFERENCES `providers`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )
                    """.trimIndent())
                db.execSQL("INSERT INTO `stops_new` (`localId`, `id`, `comId`, `providerId`, `name`, `lines`, `geoX`, `geoY`, `notifications`) SELECT `localId`, `id`, `comId`, `providerId`, `name`, `lines`, `geoX`, `geoY`, `notifications` FROM `stops`")
                db.execSQL("DROP TABLE `stops`")
                db.execSQL("ALTER TABLE `stops_new` RENAME TO `stops`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stops_providerId` ON `stops` (`providerId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_stops_id_providerId` ON `stops` (`id`, `providerId`)")
            }
        }

        fun getDatabase(context: Context): MoveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, MoveDatabase::class.java, "move_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}