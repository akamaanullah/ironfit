package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProfileEntity::class,
        MealLogEntity::class,
        GymLogEntity::class,
        WaterLogEntity::class,
        SupplementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IronDatabase : RoomDatabase() {
    abstract fun ironDao(): IronDao

    companion object {
        @Volatile
        private var INSTANCE: IronDatabase? = null

        fun getDatabase(context: Context): IronDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IronDatabase::class.java,
                    "ironfit_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
