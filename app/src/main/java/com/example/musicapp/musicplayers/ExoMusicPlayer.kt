package com.example.musicapp.musicplayers

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface ExoMusicPlayer {
    val trackPosition: Flow<Int>
    fun build(context: Context)
    fun onStateChanged(action: (Int) -> (Unit))
    fun initialize(path: String)
    fun onPause()
    fun onResume()
    fun onStop()
}

interface MusicPlayerStates {
    companion object {
        const val STATE_ENDED = Player.STATE_ENDED
        const val STATE_PAUSED = 5
        const val STATE_RESUMED = 6
    }
}

class ExoMusicPlayerImpl @Inject constructor() :
    ExoMusicPlayer {

    private lateinit var exoPlayer: ExoPlayer

    override fun build(context: Context) {
        exoPlayer = ExoPlayer.Builder(context).build()
    }

    override val trackPosition: Flow<Int> = flow {
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

    override fun onStop() {
        exoPlayer.stop()
    }

    override fun onStateChanged(action: (Int) -> Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        action(MusicPlayerStates.STATE_ENDED)
                    }
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                when (playWhenReady) {
                    true -> action(MusicPlayerStates.STATE_RESUMED)
                    false -> action(MusicPlayerStates.STATE_PAUSED) //combining two listeners into one
                }
            }
        })
    }
}