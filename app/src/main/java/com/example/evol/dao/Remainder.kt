package com.example.evol.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.evol.entity.Remainder
import java.util.UUID

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

    @Query("DELETE FROM remainders where id= :id")
    suspend fun deleteById(id:UUID)

    @Query("UPDATE remainders SET title = :title, description = :description, time=:time, workerId=:workerId WHERE id = :id")
    suspend fun updateRemainderById(id: UUID, title: String, description: String, time:Long, workerId:UUID)
}