package com.example.evol.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.evol.service.NotificationWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
fun scheduleNotification(context: Context, duration: Long, title: String, message: String): UUID {
    val data = workDataOf(
        "title" to title ,
        "message" to message
    )
    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInputData(data)
        .setInitialDelay(duration, TimeUnit.MILLISECONDS)  // Change the delay as needed
        .addTag("reminder_notification")
        .build()

//            WorkManager.getInstance(context).cancelAllWorkByTag("reminder_notification")
//            WorkManager.getInstance(context).enqueue(workRequest)

    WorkManager.getInstance(context).enqueue(workRequest)
    return workRequest.id
}