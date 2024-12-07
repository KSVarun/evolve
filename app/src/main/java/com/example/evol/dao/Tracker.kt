package com.example.evol.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.evol.models.Tracker

@Dao
interface TrackerDao {
    @Insert
    suspend fun insert(data: Tracker)

    @Update
    suspend fun update(data: Tracker)

    @Query("SELECT * FROM tracker")
    suspend fun getAll(): List<Tracker>

    @Query("DELETE FROM tracker")
    suspend fun deleteAll()
}