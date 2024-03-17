package com.marianpekar.teapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class NotificationService : Service() {

    private val notificationChannelId: String = "teapp_notification_channel_id"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val position = intent?.getIntExtra("position", -1) ?: -1
        showNotification(position)
        return START_STICKY
    }

    private fun showNotification(position: Int) {
        val notificationIntent = Intent(this, RecordActivity::class.java)

        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        notificationIntent.putExtra("position", position)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.teacup)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.your_tea_is_ready))
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableLights(true)
                lightColor = R.color.green_tea_darker

                // Enable custom notification sound
                setSound(
                    Uri.parse("android.resource://${packageName}/${R.raw.flute_shot}"),
                    null
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(1, notification)
    }
}