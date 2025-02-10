package com.example.evol.database

import com.example.evol.dao.TrackerDAO
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.evol.dao.RemainderDAO
import com.example.evol.entity.Remainder
import com.example.evol.entity.Tracker

@Database(entities = [Tracker::class, Remainder::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackerDAO(): TrackerDAO
    abstract fun remainderDAO(): RemainderDAO
}