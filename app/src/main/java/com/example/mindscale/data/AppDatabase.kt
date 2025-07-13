package com.example.mindscale.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// UPDATED: Version incremented to 2
@Database(entities = [WellnessEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wellnessDao(): WellnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wellness_database"
                )
                    // UPDATED: Added this line to handle future schema changes automatically
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}