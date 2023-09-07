package com.dev.pari.gcp.service_utils.android_service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder


class SimpleBackgroundService : Service() {

    private lateinit var currentThread: Thread
    var stopSelf = false

    override fun onCreate() {
        super.onCreate()
        if (!stopSelf) {
            currentThread = Thread {
                while (!stopSelf) {
                    println("--->>>> Background Service Running....")
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
                println("--->>>> Background Service Stopped....")
            } else
                stopSelf = false
        }
        return START_STICKY
    }

    fun stopCurrentService() {
        stopSelf()
        println("--->>>> Background Service Stopped....")
    }
}