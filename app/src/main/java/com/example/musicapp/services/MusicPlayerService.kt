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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayerService
import com.example.musicapp.musicplayers.MusicPlayerStates
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

interface ServiceBinder {
    fun getService(): MusicPlayerService
}

@AndroidEntryPoint
class MusicPlayerService @Inject constructor() : Service() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "1"
        const val NOTIFICATION_ID = 1
        const val ARTIST_PLACEHOLDER = "Artist"
        const val TITLE_PLACEHOLDER = "Title"
    }

    private lateinit var artist: String
    private lateinit var title: String

    private val pendingIntent by lazy {
        Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
        }
    }

    private val closeIntent by lazy { Intent("android.intent.CLOSE_ACTIVITY") }
    private val pendingCloseIntent by lazy { PendingIntent.getBroadcast(this, 0, closeIntent, FLAG_IMMUTABLE) }

    private val resumePauseIntent by lazy { Intent("android.intent.RESUME_PAUSE_TRACK") }
    private val pendingResumePauseIntent by lazy { PendingIntent.getBroadcast(this, 0, resumePauseIntent, FLAG_IMMUTABLE) }

    private val nextTrackIntent by lazy { Intent("android.intent.PLAY_NEXT_TRACK") }
    private val pendingNextTrackIntent by lazy { PendingIntent.getBroadcast(this, 0, nextTrackIntent, FLAG_IMMUTABLE) }

    private val previousTrackIntent by lazy { Intent("android.intent.PLAY_PREVIOUS_TRACK") }
    private val pendingPreviousTrackIntent by lazy { PendingIntent.getBroadcast(this, 0, previousTrackIntent, FLAG_IMMUTABLE) }

    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    @Inject
    lateinit var musicPlayer: ExoMusicPlayerService

    private val broadcastReceiver = object : BroadcastReceiver() { //stop service on notification click
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        musicPlayer.build(this)
        val filter = IntentFilter("android.intent.CLOSE_ACTIVITY")
        registerReceiver(broadcastReceiver, filter)
        artist = ARTIST_PLACEHOLDER
        title = TITLE_PLACEHOLDER
        setOnTrackChangedListener()
        setOnStateChangeListener()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }

        val notification = createNotification(false)
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onBind(intent: Intent?): IBinder {
        return object : Binder(), ServiceBinder {
            override fun getService(): MusicPlayerService {
                return this@MusicPlayerService
            }
        }
    }

    private fun setOnTrackChangedListener() {
        musicPlayer.setOnTrackChangedListener { track ->
            updateNotification(track)
        }
    }

    private fun setOnStateChangeListener() {
        musicPlayer.onStateChanged { state ->
            when (state) {
                MusicPlayerStates.STATE_PAUSED -> {
                    val notification = createNotification(false)
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
                MusicPlayerStates.STATE_RESUMED -> {
                    val notification = createNotification(true)
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
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

    private fun createNotification(isPlaying: Boolean): Notification {
        val resumePauseActionIconId = if (isPlaying) {
            R.drawable.icon_pause
        } else {
            R.drawable.icon_play_arrow
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.icon_play_arrow)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(MediaStyle())
            .addAction(
                R.drawable.ic_baseline_close_24, "STOP",
                pendingCloseIntent
            )
            .addAction(
                R.drawable.icon_previous_arrow, "Previous",
                pendingPreviousTrackIntent
            )
            .addAction(
                resumePauseActionIconId, "PlayPause",
                pendingResumePauseIntent
            )
            .addAction(
                R.drawable.icon_next_arrow, "Next",
                pendingNextTrackIntent
            )
            .build()
    }

    private fun updateNotification(track: ListViewTrack) {
        title = track.name
        artist = track.artist
        val notification = createNotification(true)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}