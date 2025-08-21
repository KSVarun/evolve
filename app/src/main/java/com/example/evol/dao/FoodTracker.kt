package com.example.evol.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.evol.entity.FoodTracker

@Dao
interface FoodTrackerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodTracker: FoodTracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodTrackers: List<FoodTracker>)

    @Update
    suspend fun update(data: FoodTracker)

//    @Query("SELECT * FROM foodTracker")
//    suspend fun getAll(): List<FoodTracker>

    @Query("DELETE FROM foodTracker")
    suspend fun deleteAll()
}