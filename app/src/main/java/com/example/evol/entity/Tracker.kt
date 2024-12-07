package com.example.evol.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracker")
data class Tracker (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val item: String?,
    val value: Int?
)