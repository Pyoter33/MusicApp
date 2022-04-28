package com.example.musicapp.musicplayers

import android.content.Context
import com.example.musicapp.models.ListViewTrack
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface ExoMusicPlayer {
    val trackPosition: Flow<Int>
    fun onStateChanged(action: (Int) -> (Unit))
    fun initialize(track: ListViewTrack)
    fun onPause()
    fun onResume()
}

interface ExoMusicPlayerService {
    fun build(context: Context)
    fun setOnTrackChangedListener(action: (ListViewTrack) -> Unit)
    fun onStop()
}

interface MusicPlayerStates {
    companion object {
        const val STATE_ENDED = Player.STATE_ENDED
        const val STATE_PAUSED = 5
        const val STATE_RESUMED = 6
    }
}

@Singleton
class ExoMusicPlayerImpl @Inject constructor() :
    ExoMusicPlayer, ExoMusicPlayerService {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var currentTrack: ListViewTrack

    override fun build(context: Context) {
        exoPlayer = ExoPlayer.Builder(context).build()
    }

    override val trackPosition: Flow<Int> = flow {
        while (true) { //ugly while(true) but found in docs
            emit((exoPlayer.currentPosition / 1000).toInt())
            delay(1000)
        }
    }

    override fun initialize(track: ListViewTrack) {
        currentTrack = track
        val media = MediaItem.fromUri(track.path)
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

    override fun setOnTrackChangedListener(action: (ListViewTrack) -> Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
                action(currentTrack)
            }
        })
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