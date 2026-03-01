package com.example.evol.entity

data class Consistency(
    val consistentSince:Int,
    val brokenSince:Int,
    val longestConsistentSince:Int = 0,
    val longestBrokenSince:Int = 0
)
