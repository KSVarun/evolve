package com.example.evol.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return currentDate.format(formatter)
}

fun getValueInTwoDigits(input:Int):String{
    if(input.toString().length==1) {
        return "0$input"
    }
    return input.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDefaultHourToShow():String{
    val currentHour = LocalTime.now().hour
    if(currentHour<23){
        return getValueInTwoDigits(currentHour + 1)
    }
    return getValueInTwoDigits(currentHour)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getPreviousNDate(previous:Long):String{
    val currentDate = LocalDate.now()
    currentDate.minusDays(previous)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return currentDate.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertDateTimeToMillis(dateString: String, timeString: String): Long {
    val dateTimeString = "$dateString $timeString"
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
    return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertMillisToDateTime(millis:Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(millis))
}