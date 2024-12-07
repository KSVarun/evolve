package com.example.evol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class Notification : Service() {

    companion object {
        const val CHANNEL_ID = "timer_notification_foreground"
        const val CHANNEL_NAME = "Timer Notifications"
        const val NOTIFICATION_ID = 100
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()  // Ensure the channel exists
    }




    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Service Running", "Processing timer...")
        val foregroundServiceType = getForegroundServiceTypeName()
        startForeground(NOTIFICATION_ID, notification, foregroundServiceType)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun getForegroundServiceTypeName(): Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        }
        return 0
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for timer notifications foreground"
            }
            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Replace with your own icon
            .build()
    }
}
