package com.example.evol.utils

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.evol.service.AlarmWorker
import com.example.evol.service.NotificationWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

fun cancelAlarm(context: Context, reminderIdToCancel: Int) { // The unique ID of the reminder whose alarm you want to cancel
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, AlarmWorker::class.java).apply {
        action = "ALARM_REMAINDER"
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderIdToCancel,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("AlarmScheduler", "Alarm cancelled for reminder ID: $reminderIdToCancel")
    } else {
        Log.w("AlarmScheduler", "No alarm found to cancel for reminder ID: $reminderIdToCancel (PendingIntent was null)")
    }
}

fun scheduleNotificationUsingAlarmWorker(context: Context, duration: Long, title: String, message: String):Int{
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmWorker::class.java).apply {
        action = "ALARM_REMAINDER"
        putExtra("title", title)
        putExtra("message", message)
    }
    val requestCode = System.currentTimeMillis().toInt()
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, duration, pendingIntent)
        } else {
            // Guide user to settings or use inexact alarm
            // Or schedule a WorkManager request as a fallback
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, duration, pendingIntent)
    }

    return requestCode
}

fun scheduleNotification(context: Context, duration: Long, title: String, message: String): UUID {
    val data = workDataOf(
        "title" to title,
        "message" to message
    )
    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInputData(data)
        .setInitialDelay(duration, TimeUnit.MILLISECONDS)
        .addTag("reminder_notification")
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
    return workRequest.id
}

fun checkForNotificationPermission(context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return true
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        (context as? Activity)?.let { activity ->
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
    }
    return false
}