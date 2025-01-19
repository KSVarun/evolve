package com.example.evol.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracker")
data class Tracker(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val item: String?,
    val value: Double
)

val MaxThresholdValue = "max-threshold-value"
val ThresholdIncrement = "threshold-increment"
