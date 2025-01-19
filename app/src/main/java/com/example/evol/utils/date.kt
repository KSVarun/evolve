package com.example.evol.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return currentDate.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getPreviousNDate(previous:Long):String{
    val currentDate = LocalDate.now()
    currentDate.minusDays(previous)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return currentDate.format(formatter)
}