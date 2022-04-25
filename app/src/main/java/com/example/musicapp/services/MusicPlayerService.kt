package com.example.musicapp.services

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface MusicPlayerService {
    val trackPosition: Flow<Int>
    fun onStateChanged(action: (Int) -> (Unit))
    fun initialize(track: ListViewTrack)
    fun onPause()
    fun onResume()

}

interface ServiceBinder {
    fun getService(): MusicPlayerService
}

@AndroidEntryPoint
class MusicPlayerServiceImpl @Inject constructor() : Service(), MusicPlayerService {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "1"
        const val NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var musicPlayer: ExoMusicPlayer

    override lateinit var trackPosition: Flow<Int>

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { //stop service on notification click
            stopForeground(true);
            stopSelf();
        }
    }

    override fun onCreate() {
        super.onCreate()
        musicPlayer.build(this)
        trackPosition = musicPlayer.trackPosition
        val filter = IntentFilter("android.intent.CLOSE_ACTIVITY")
        registerReceiver(broadcastReceiver, filter)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }

        val notification = createNotification("Title", "Artist")
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onBind(intent: Intent?): IBinder {
        return object: Binder(), ServiceBinder {
            override fun getService(): MusicPlayerService {
                return this@MusicPlayerServiceImpl
            }
        }
    }

    override fun onStateChanged(action: (Int) -> Unit) {
        musicPlayer.onStateChanged(action)
    }

    override fun initialize(track: ListViewTrack) {
        musicPlayer.initialize(track.path)
        updateNotification(track)
    }

    override fun onPause() {
        musicPlayer.onPause()
    }

    override fun onResume() {
        musicPlayer.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNotificationChannel(id: String) {
        val channel = NotificationChannel(
            id,
            "MusicPlayerService",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(title: String, artist: String): Notification {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
        }

        val closeIntent = Intent("android.intent.CLOSE_ACTIVITY")
        val pendingCloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, FLAG_IMMUTABLE)

       return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.icon_play_arrow)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_baseline_close_24, "STOP",
                pendingCloseIntent)
            .build()
    }

    private fun updateNotification(track: ListViewTrack) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(track.name, track.artist)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}