package com.example.evol.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracker")
data class Tracker (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val item: String?,
    val value: Int?
)