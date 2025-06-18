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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.evol.service.AlarmWorker
import java.util.UUID

fun cancelAlarm(context: Context, reminderIdToCancel: Int) { // The unique ID of the reminder whose alarm you want to cancel
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, AlarmWorker::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderIdToCancel,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("AlarmScheduler", "Alarm cancelled for reminder ID: $reminderIdToCancel")
    } else {
        Log.w("AlarmScheduler", "No alarm found to cancel for reminder ID: $reminderIdToCancel (PendingIntent was null)")
    }
}


fun scheduleNotification(context: Context, duration: Long, title: String, message: String): Int {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmId = UUID.randomUUID().hashCode()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // For API 31 and above
        val testAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val res = testAlarmManager.canScheduleExactAlarms()
        Log.d("ExactAlarmScheduler", res.toString())
        if (!testAlarmManager.canScheduleExactAlarms()) {
            Toast.makeText(context, "Please grant 'Alarms & reminders' permission.", Toast.LENGTH_LONG).show()
            return -1
        }
    }

    val alarmReceiverIntent = Intent(context, AlarmWorker::class.java).apply {
        putExtra("title", title)
        putExtra("message", message)
        putExtra("alarmId", alarmId)
    }
    val alarmPendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId,
        alarmReceiverIntent,
        PendingIntent.FLAG_IMMUTABLE
    )
    // Optional: Intent to show when the user clicks the alarm icon in the status bar (if shown)
    // This usually opens the app's main screen or a specific alarm screen.
//    val showAppIntent = Intent(context, MainActivity::class.java).apply { // Or your specific alarm activity
//        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        // You could add extras here if needed to navigate to a specific part of your app
//        // putExtra("alarm_id_clicked", alarmId)
//    }
//    val showAppPendingIntent = PendingIntent.getActivity(
//        context,
//        alarmId + 1000, // Use a different request code to avoid collision with alarmPendingIntent
//        showAppIntent,
//        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//    )
//
//    val alarmClockInfo = AlarmManager.AlarmClockInfo(duration, showAppPendingIntent)

    try {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,duration, alarmPendingIntent)
        Log.d("ExactAlarmScheduler", "Exact alarm (setAlarmClock) scheduled for $duration with alarm ID: $alarmId")
        Toast.makeText(context, "Alarm set for exact time!", Toast.LENGTH_SHORT).show()
        return alarmId
    } catch (se: SecurityException) {
        // This might happen if canScheduleExactAlarms() was true but permission was revoked just before setting.
        // Or if on API 34+ and USE_EXACT_ALARM is needed but not granted/declared appropriately.
        Log.e("ExactAlarmScheduler", "SecurityException while setting alarm clock for alarm ID: $alarmId. $se")
        Toast.makeText(context, "Could not set exact alarm due to a security policy.", Toast.LENGTH_LONG).show()
        // Potentially guide to settings again or log more details.
        return -1
    } catch (e: Exception) {
        Log.e("ExactAlarmScheduler", "Exception while setting alarm clock for alarm ID: $alarmId. $e")
        Toast.makeText(context, "Could not set exact alarm.", Toast.LENGTH_LONG).show()
        return -1
    }
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