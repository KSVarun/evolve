package com.example.evol.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.evol.entity.HabitTracker

@Dao
interface HabitTrackerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habitTracker: HabitTracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habitTrackers: List<HabitTracker>)

    @Update
    suspend fun update(data: HabitTracker)

//    @Query("SELECT * FROM habitTracker")
//    suspend fun getAll(): List<HabitTracker>

    @Query("DELETE FROM habitTracker")
    suspend fun deleteAll()
}