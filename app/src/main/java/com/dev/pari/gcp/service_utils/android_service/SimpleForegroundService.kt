package com.dev.pari.gcp.service_utils.android_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dev.pari.gcp.R
import com.dev.pari.gcp.activity.MainActivity
import com.dev.pari.gcp.common.Constants.NOTIFICATION_CHANNEL_NAME
import com.dev.pari.gcp.common.Constants.NOTIFICATION_ID


class SimpleForegroundService : Service() {

    private lateinit var currentThread: Thread
    var stopSelf = false

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createNotificationChanel()
        else
            startForeground(1, Notification())
        if (!stopSelf) {
            currentThread = Thread {
                while (!stopSelf) {
                    println("--->>>> Foreground Service Running....")
                    try {
                        Thread.sleep(2000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
            currentThread.start()
        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val shouldClose = intent!!.getBooleanExtra("close", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (shouldClose) {
                stopSelf = true
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                println("--->>>> Foreground Service Stopped....")
            } else
                stopSelf = false
        }
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val chan = NotificationChannel(
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("close", true)
        val signOffIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentText("Test Foreground Service Running")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
//            .setContentIntent(signOffIntent)
//            .addAction(NotificationCompat.Action.Builder(0, "Cancel", cancelIntent).build())
//            .addAction(NotificationCompat.Action.Builder(0, "Sign-Off", signOffIntent).build())
            .build()
//        manager.notify(2, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(2, notification)
        } else startForeground(2, notification)
    }
}