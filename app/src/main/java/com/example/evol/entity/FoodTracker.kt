package com.example.evol.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foodTracker")
data class FoodTracker(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val item: String,
    val value: Double
)
