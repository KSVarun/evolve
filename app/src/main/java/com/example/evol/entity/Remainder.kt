package com.example.evol.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "remainders")
data class Remainder(
    @PrimaryKey val id: UUID,
    val title: String,
    val description: String,
    val time: Long,
    val workerId: String
)


