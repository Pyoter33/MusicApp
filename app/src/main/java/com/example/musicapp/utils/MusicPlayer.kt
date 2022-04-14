package com.example.musicapp.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.InvocationTargetException
import java.net.URI
import javax.inject.Inject

interface MusicPlayer {
    fun initialize(path: Uri)
    fun onResumePause()
}

class MusicPlayerImpl @Inject constructor(@ApplicationContext private val context: Context) : MusicPlayer {

    private val mediaPlayer: MediaPlayer =
        MediaPlayer().apply {
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            setOnPreparedListener {
                start()
            }
        }

    override fun onResumePause() {
        if (mediaPlayer.isPlaying) { //observe it somehow?
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
    }

    override fun initialize(path: Uri) {
        mediaPlayer.stop()
        Log.i("MusicPlayer", path.toString())
       try {
           mediaPlayer.setDataSource(context, path)
           mediaPlayer.prepareAsync()
       } catch (e: Exception) {
           e.printStackTrace()
           Log.i("MusicPlayer", "Wrong path provided!")
       }
    }

}