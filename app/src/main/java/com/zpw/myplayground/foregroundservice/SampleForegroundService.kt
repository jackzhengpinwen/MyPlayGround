package com.zpw.myplayground.foregroundservice

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zpw.myplayground.R
import com.zpw.myplayground.foregroundservice.ForegroundServiceActivity.Companion.ACTION_STOP
import com.zpw.myplayground.foregroundservice.ForegroundServiceActivity.Companion.ACTION_STOP_FOREGROUND

class SampleForegroundService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.action != null && intent.action.equals(
                ACTION_STOP_FOREGROUND, ignoreCase = false
        )) {
            stopForeground(true)
            stopSelf()
        }
        generateForegroundNotification()
        return START_STICKY
    }

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, ForegroundServiceActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            val iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannelGroup(NotificationChannelGroup("chats_group", "Chats"))
                val notificationChannel = NotificationChannel("service_channel", "Service Notifications", NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")
            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" service is running").toString())
                .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running").toString())
                .setContentText("Touch to open") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification, 128, 128, false))
            }
            builder.color = resources.getColor(R.color.purple_200)
            val notification = builder.build()
            startForeground(123, notification)
        }
    }


}