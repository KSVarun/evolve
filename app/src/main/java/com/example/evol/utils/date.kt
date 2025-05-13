package com.example.evol.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


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


fun getDefaultHourToShow():String{
    val currentHour = LocalTime.now().hour
    if(currentHour<23){
        return getValueInTwoDigits(currentHour + 1)
    }
    return getValueInTwoDigits(currentHour)
}


fun getPreviousNDate(inputDate:String,previous:Long):String{
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val currentDate = LocalDate.parse(inputDate, formatter)
    val updatedDate = currentDate.minusDays(previous)
    return updatedDate.format(formatter)
}

fun getNextNDate(inputDate:String,next:Long):String{
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val currentDate = LocalDate.parse(inputDate, formatter)
    val updatedDate = currentDate.plusDays(next)
    return updatedDate.format(formatter)
}


fun convertDateTimeToMillis(dateString: String, timeString: String): Long {
    val dateTimeString = "$dateString $timeString"
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
    return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}


fun convertMillisToDateTime(millis:Long, pattern:String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(millis))
}

fun isSelectedDateLessThanCurrentData(selectedDate:String):Boolean{
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val currentDate = LocalDate.now()
    val selectedLocalDate = LocalDate.parse(selectedDate, formatter)
    return selectedLocalDate.isBefore(currentDate)
}