package com.example.evol.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.evol.service.NotificationWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
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