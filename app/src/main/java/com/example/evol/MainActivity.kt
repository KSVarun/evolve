package com.example.evol

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.evol.ui.theme.EvolTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.*


class MainActivity : ComponentActivity() {
    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!checkBatteryLifePermission(this)){
            val intent = Intent()
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.data = Uri.parse("package:$packageName")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                // Handle the error gracefully, perhaps show a message to the user
            }
        }

        setContent {
            EvolTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    Greeting("Android")
                    Timer(this)
                }
            }
        }
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hola $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EvolTheme {
        Greeting("Android!")
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Timer(context: Context) {
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

            val serviceIntent = Intent(context, NotificationService::class.java)
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
    }
}

fun checkBatteryLifePermission(context: Context): Boolean {
    try {
        val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)

    } catch (e: Throwable) {
        println(e)
    }
    return false
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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