package com.example.evol.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.evol.entity.Tracker

@Dao
interface TrackerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracker: Tracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trackers: List<Tracker>)

    @Update
    suspend fun update(data: Tracker)

    @Query("SELECT * FROM tracker")
    suspend fun getAll(): List<Tracker>

    @Query("DELETE FROM tracker")
    suspend fun deleteAll()
}