package com.example.evol.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remainders")
data class Remainder(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val title: String,
    val description: String,
    val time: Long,
    val workerId: String
)


