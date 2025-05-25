package com.example.evol.ui.components

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.evol.service.Notification
import com.example.evol.utils.scheduleNotification
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class Step(
    val name: String,
    val time: Long
)

data class Task(
    val id: String,
    val name: String,
    val steps: List<Step>,
    val totalTime: Int
)


@Composable
fun Timer(context: Context) {
    val task = Task(
        id = "125",
        name = "test",
        steps = listOf(
            Step(name = "test1", time = 60000),
            Step(name = "test2", time = 60000)
        ),
        totalTime = 120000
    )


    var timeInSeconds by remember { mutableIntStateOf(0) }
    var job by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val notificationScope = rememberCoroutineScope()
//    val context = LocalContext.current  // Access the context for notifications

    // Create a notification channel (required for Android 8.0+)
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }

    Column {
        Text(text = "Timer: $timeInSeconds s", fontSize = 24.sp)
        Button(onClick = {
            if (job == null || job?.isActive == false) {
                job=scope.launch {
                    while (isActive) {
                        delay(1000)
                        timeInSeconds++

                        if (timeInSeconds == 10) {
                            showNotification(context, "Timer Alert", "10 seconds passed!")
                        }
                    }
                }
            }
        }) {
            Text(text = "Start")
        }
        Spacer(modifier = Modifier.width(8.dp))  // Add space between buttons

        Button(onClick = {

            val serviceIntent = Intent(context, Notification::class.java)
            context.startForegroundService(serviceIntent)

            notificationScope.launch {

                for(step in task.steps){
                    delay(step.time)
                    showNotification(context, "Timer Alert", step.name)
                } }
        }) {
            Text(text = "Notification trigger")
        }


        Spacer(modifier = Modifier.width(8.dp))  // Add space between buttons

        Button(onClick = {
            job?.cancel()  // Cancel the coroutine
            job = null     // Clear the job reference
        }) {
            Text(text = "Stop")
        }

        Spacer(modifier = Modifier.width(8.dp))  // Add space between buttons

        Button(onClick = {
            job?.cancel()  // Cancel the coroutine
            job = null     // Clear the job reference
            timeInSeconds=0
        }) {
            Text(text = "Reset")
        }

        Spacer(modifier = Modifier.width(8.dp))  // Add space between buttons

        Button(onClick = {
            scheduleNotification(context,10000, "hello", "world")
        }){
            Text(text="Worker")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = {

            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(context, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                // Now show Time Picker
                TimePickerDialog(context, { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    // Convert to milliseconds
                    val millis = calendar.timeInMillis
                    println("Selected Date-Time in Milliseconds: $millis")
                    scheduleNotification(context,millis-System.currentTimeMillis(),"scheduled","success")

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()

//            WorkManager.getInstance(context)
//                .getWorkInfosByTag("timer")
//                .addListener({
//                    val workInfos = WorkManager.getInstance(context)
//                        .getWorkInfosByTag("reminder_notification")
//                        .get() // Be careful: .get() is a blocking call
//                    if (workInfos != null) {
//                        for (workInfo in workInfos) {
//                            // You can inspect details such as id and state
//                            val workId = workInfo.id
//                            val state = workInfo.state
//                            val tags = workInfo.tags
//                            println(workInfo)
//                            // Print or log the details of each work request
//                            println("Work ID: $workId, State: $state, Tags: $tags")
//                        }
//                    }
//                }, Executors.newSingleThreadExecutor())

        }){
            Text(text="Date time picker")
        }
    }

}



fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "timer_channel",
        "Timer Notifications",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Channel for timer notifications"
    }
    val manager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}

// Function to show a notification
fun showNotification(context: Context, title: String, message: String) {
    // Check if notification permission is granted
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        // Permission is granted, show the notification
        val builder = NotificationCompat.Builder(context, "timer_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())  // Notification ID is 1
        }
    } else {
        // Request the permission if not granted (only for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (context as? Activity)?.let { activity ->
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}