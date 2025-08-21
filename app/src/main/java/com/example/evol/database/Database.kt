package com.example.evol.database

import com.example.evol.dao.HabitTrackerDAO
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.evol.dao.FoodTrackerDAO
import com.example.evol.dao.RemainderDAO
import com.example.evol.entity.Remainder
import com.example.evol.entity.HabitTracker
import com.example.evol.entity.FoodTracker

@Database(entities = [HabitTracker::class, Remainder::class, FoodTracker::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitTrackerDAO(): HabitTrackerDAO
    abstract fun remainderDAO(): RemainderDAO
    abstract fun foodTrackerDAO(): FoodTrackerDAO
}