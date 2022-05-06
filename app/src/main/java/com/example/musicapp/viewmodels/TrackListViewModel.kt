package com.example.musicapp.viewmodels

import androidx.lifecycle.*
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.musicplayers.MusicPlayerStates
import com.example.musicapp.usecases.TrackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val trackUseCase: TrackUseCase,
    private val player: ExoMusicPlayer
) : ViewModel() {

    private val _trackList by lazy {
        val liveData = MutableLiveData<List<ListViewTrack>>()
        getTrackList(liveData)
        return@lazy liveData
    }
    val trackList: LiveData<List<ListViewTrack>> = _trackList

    private val _currentTrack = MutableLiveData<ListViewTrack>()
    val currentTrack: LiveData<ListViewTrack> = _currentTrack

    private val _positionToNotify = MutableLiveData<Int>()
    val positionToNotify: LiveData<Int> = _positionToNotify

    private val _trackProgression = MutableLiveData<Int>()
    val trackProgression: LiveData<Int> = _trackProgression

    private val _isCurrentPaused = MutableLiveData(false)
    val isCurrentPaused: LiveData<Boolean> = _isCurrentPaused

    private var currentPosition: Int? = null
    private var trackProgressionJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        trackProgressionJob?.cancel()
    }

    fun registerListener() {
        setOnStateChangeListener()
    }

    fun updateTracks(currentTrack: ListViewTrack, position: Int) {
        _currentTrack.value?.apply {
            isPlaying = false
        }
        currentPosition?.let { currentPosition ->
            _positionToNotify.value = currentPosition
        }
        trackProgressionJob?.run {
            cancel()
        }
        _positionToNotify.value = position
        currentTrack.isPlaying = true
        _currentTrack.value = currentTrack
        currentPosition = position
        player.initialize(currentTrack)
        resumeTrackProgressionJob()
    }

    fun playNextTrack() {
        currentPosition?.let { currentPosition ->
            val newPosition = currentPosition + 1
            val list = trackList.value!!
            if (newPosition < list.size) {
                updateTracks(list[newPosition], newPosition)
            }
        }
    }

    fun playPreviousTrack() {
        currentPosition?.let { currentPosition ->
            val newPosition = currentPosition - 1
            val list = trackList.value!!
            if (newPosition >= 0) {
                updateTracks(list[newPosition], newPosition)
            }
        }
    }

    fun resumeTrack() {
        player.onResume()
        resumeTrackProgressionJob()
    }

    fun pauseTrack() {
        player.onPause()
        trackProgressionJob?.run {
            cancel()
        }
    }

    fun seekOnTrack(timeStamp: Int) {
        player.onSeek(timeStamp)
    }

    private fun getTrackList(liveData: MutableLiveData<List<ListViewTrack>>) {
        viewModelScope.launch {
            trackUseCase.getTrackList().collect { list ->
                liveData.value = list.map { track ->
                    ListViewTrack(
                        track.id,
                        track.title ?: "Unknown",
                        track.author?: "Unknown",
                        track.length?: 0,
                        track.path!!
                    )
                }
            }
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
}
