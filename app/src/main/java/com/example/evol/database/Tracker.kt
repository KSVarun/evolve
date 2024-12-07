package com.example.evol.database

import com.example.evol.dao.TrackerDao
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.evol.models.Tracker

@Database(entities = [Tracker::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
}