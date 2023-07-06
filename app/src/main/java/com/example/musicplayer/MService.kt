package com.example.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder

class MService: Service() {

    companion object {
        lateinit var path: String
        var mp: MediaPlayer = MediaPlayer()
        lateinit var service: Service

        fun setpath(sPath: String) {
            path = sPath
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        service = this

        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("Music")
                .setContentText("Music is playing in background")
                .setSmallIcon(R.drawable.placeholder)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Music")
                .setContentText("Music is playing in background")
                .setSmallIcon(R.drawable.placeholder)
                .setContentIntent(pendingIntent)
                .build()
        }
        startForeground(1, notification)

        mp = MediaPlayer.create(this, Uri.parse(path))
        mp.start()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("ForegroundServiceChannel", "Foreground Service Channel", NotificationManager.IMPORTANCE_LOW)
            val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onDestroy() {
        mp.stop()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}