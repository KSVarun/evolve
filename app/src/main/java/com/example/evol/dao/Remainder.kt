package com.example.evol.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.evol.entity.Remainder

@Dao
interface RemainderDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remainder: Remainder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remainder: List<Remainder>)

    @Update
    suspend fun update(data: Remainder)

    @Query("SELECT * FROM remainders")
    suspend fun getAll(): List<Remainder>

    @Query("DELETE FROM remainders")
    suspend fun deleteAll()
}