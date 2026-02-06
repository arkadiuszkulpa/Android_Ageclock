package com.ageclock.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ageclock.app.data.model.Person

@Database(entities = [Person::class], version = 2, exportSchema = false)
abstract class AgeclockDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AgeclockDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE people ADD COLUMN description TEXT")
                database.execSQL("ALTER TABLE people ADD COLUMN aiMessages TEXT")
                database.execSQL("ALTER TABLE people ADD COLUMN aiMessagesGeneratedAt INTEGER")
            }
        }

        fun getInstance(context: Context): AgeclockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgeclockDatabase::class.java,
                    "ageclock_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
