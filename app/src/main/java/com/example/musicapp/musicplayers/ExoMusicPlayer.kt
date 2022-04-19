package com.example.musicapp.musicplayers

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface ExoMusicPlayer {
    var currentPlayback: Flow<Int>

    fun initialize(path: String)
    fun onPause()
    fun onResume()
    fun onStateChanged(action: (Int) -> (Unit))
}

class ExoMusicPlayerImpl @Inject constructor(@ApplicationContext private val context: Context) :
    ExoMusicPlayer {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    override var currentPlayback: Flow<Int> = flow {
        while (true) { //ugly while(true) but found in docs
            emit((exoPlayer.currentPosition / 1000).toInt())
            delay(1000)
        }
    }

    override fun initialize(path: String) {
        val media = MediaItem.fromUri(path)
        exoPlayer.setMediaItem(media)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun onPause() {
        exoPlayer.pause()
    }

    override fun onResume() {
        exoPlayer.play()
    }

    override fun onStateChanged(action: (Int) -> Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                action(state)
            }
        })
    }
}