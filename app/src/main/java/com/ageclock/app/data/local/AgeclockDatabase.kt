package com.ageclock.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ageclock.app.data.model.Person

@Database(entities = [Person::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AgeclockDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AgeclockDatabase? = null

        fun getInstance(context: Context): AgeclockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgeclockDatabase::class.java,
                    "ageclock_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
