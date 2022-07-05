package com.example.musicapp.viewmodels

import android.bluetooth.BluetoothDevice
import android.media.MediaMetadataRetriever
import androidx.lifecycle.*
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.musicplayers.MusicPlayerStates
import com.example.musicapp.repository.Track
import com.example.musicapp.usecases.GetTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val player: ExoMusicPlayer
) : ViewModel() {

    val trackList: LiveData<List<ListViewTrack>> by lazy {
        return@lazy getTrackListFromUseCase()
    }

    private val _currentTrack = MutableLiveData<ListViewTrack?>()
    val currentTrack: LiveData<ListViewTrack?> = _currentTrack

    private val _positionToNotify = MutableLiveData<Int>()
    val positionToNotify: LiveData<Int> = _positionToNotify

    private val _trackProgression = MutableLiveData<Int>()
    val trackProgression: LiveData<Int> = _trackProgression

    private val _isCurrentPaused = MutableLiveData<Boolean>()
    val isCurrentPaused: LiveData<Boolean> = _isCurrentPaused

    private val _availableDevices = MutableLiveData(listOf<BluetoothDevice>())
    val availableDevices: LiveData<List<BluetoothDevice>> = _availableDevices

    private var currentPosition: Int? = null
    private var trackProgressionJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        trackProgressionJob?.cancel()
    }

    fun registerListener() {
        setOnStateChangeListener()
    }

    fun updateTracks(position: Int) {
        currentPosition?.let { currentPosition ->
            trackList.value!![currentPosition].apply {
                isPlaying = false
                isPlayingState = false
            }
            _positionToNotify.value = currentPosition
        }
        trackProgressionJob?.run {
            cancel()
        }
        val newCurrentTrack = trackList.value!![position]
        newCurrentTrack.apply {
            isPlaying = true
            newCurrentTrack.isPlayingState = true
        }
        _positionToNotify.value = position
        _currentTrack.value = newCurrentTrack
        currentPosition = position
        player.initialize(newCurrentTrack)
        resumeTrackProgressionJob()
    }

    fun playNextTrack() {
        currentPosition?.let { currentPosition ->
            val newPosition = currentPosition + 1
            val list = trackList.value!!
            if (newPosition < list.size) {
                updateTracks(newPosition)
            }
        }
    }

    fun playPreviousTrack() {
        currentPosition?.let { currentPosition ->
            val newPosition = currentPosition - 1
            if (newPosition >= 0) {
                updateTracks(newPosition)
            }
        }
    }

    fun resumeTrack() {
        if(currentPosition != null) {
            player.onResume()
            resumeTrackProgressionJob()
        }
    }

    fun pauseTrack() {
        if(currentPosition != null) {
            player.onPause()
            trackProgressionJob?.run {
                cancel()
            }
        }
    }

    fun seekOnTrack(timeStamp: Int) {
        player.onSeek(timeStamp)
    }

    private fun getTrackListFromUseCase(): LiveData<List<ListViewTrack>> {
        return getTracksUseCase.getTrackList().map { list ->
            currentPosition = null
            val newList = mutableListOf<ListViewTrack>()
            for (i in list.indices) { //loop instead of map to update current position after list reload
                val track = list[i]
                val isPlaying = track.id == currentTrack.value?.id
                if (isPlaying) {
                    currentPosition = i
                }
                newList.add(
                    ListViewTrack(
                        track.id,
                        track.title ?: "Unknown",
                        track.author ?: "Unknown",
                        track.length ?: 0,
                        track.path,
                        getTrackImage(track),
                        isPlaying
                    )
                )
            }
            checkNullPosition()
            return@map newList
        }.asLiveData()
    }

    private fun getTrackImage(track: Track): ByteArray {
        val image = if(File(track.path).exists()) {
            val mmr =MediaMetadataRetriever().apply {
                setDataSource(track.path)
            }
            mmr.embeddedPicture
        } else {
            byteArrayOf()
        }
        return image ?: byteArrayOf()
    }

    private fun checkNullPosition() {
        if(currentPosition == null && currentTrack.value != null) {
            _currentTrack.value = null
            player.onStop()
        }
    }

    private fun resumeTrackProgressionJob() {
        trackProgressionJob = viewModelScope.launch {
            player.let { player ->
                player.trackPosition.collect {
                    _trackProgression.value = it
                }
            }
        }
    }

    fun setTrackProgression(progression: Int) {
        _trackProgression.value = progression
    }

    private fun setOnStateChangeListener() {
        player.onStateChanged { state ->
            when (state) {
                MusicPlayerStates.STATE_ENDED -> {
                    playNextTrack()
                }
                MusicPlayerStates.STATE_PAUSED -> {
                    _isCurrentPaused.value = true
                }
                MusicPlayerStates.STATE_RESUMED -> {
                    _isCurrentPaused.value = false
                }
            }
        }
    }

    fun addBluetoothDevice(device: BluetoothDevice) {
        _availableDevices.value = availableDevices.value!! + listOf(device)
    }

    fun resetBluetoothDevices() {
        _availableDevices.value = listOf()
    }
}
