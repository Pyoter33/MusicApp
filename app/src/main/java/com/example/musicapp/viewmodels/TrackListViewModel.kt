package com.example.musicapp.viewmodels

import androidx.lifecycle.*
import com.example.musicapp.models.UITrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.usecases.TrackUseCase
import com.google.android.exoplayer2.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val useCase: TrackUseCase,
    private val musicPlayer: ExoMusicPlayer
) : ViewModel() {

    private val _trackList = MutableLiveData<List<UITrack>>()
    val trackList: LiveData<List<UITrack>> = _trackList

    private val _currentTrack = MutableLiveData<UITrack>().also {
        getUITrackList()
    }
    val currentTrack: LiveData<UITrack> = _currentTrack

    private val _positionToNotify = MutableLiveData<Int>()
    val positionToNotify: LiveData<Int> = _positionToNotify

    private val _trackProgression = MutableLiveData<Int>()
    val trackProgression: LiveData<Int> = _trackProgression

    private var currentPosition: Int? = null
    private var isCurrentPaused = false

    private var trackProgressionJob: Job? = null

    init {
        musicPlayer.onStateChanged { state ->
            when (state) {
                Player.STATE_ENDED -> playNextTrack()
                //add more events later?
            }
        }
    }

    fun updateTracks(currentUITrack: UITrack, position: Int) {
        _currentTrack.value?.apply {
            isPlaying = false
        }
        currentPosition?.let { currentPosition ->
            _positionToNotify.value = currentPosition
        }
        trackProgressionJob?.run {
            cancel()
        }
        isCurrentPaused = false
        _positionToNotify.value = position
        currentUITrack.isPlaying = true
        _currentTrack.value = currentUITrack
        currentPosition = position
        musicPlayer.initialize(currentUITrack.path)
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

    fun resumePauseTrack() {
        isCurrentPaused = !isCurrentPaused
        if (isCurrentPaused) {
            musicPlayer.onPause()
            trackProgressionJob?.run {
                cancel()
            }
        } else {
            resumeTrackProgressionJob()
            musicPlayer.onResume()
        }
    }

    private fun getUITrackList() {
        viewModelScope.launch {
            _trackList.value = useCase.getTrackList().map { track ->
                UITrack(track.id, track.name, track.artist, track.length, track.path)
            }
        }
    }

    private fun resumeTrackProgressionJob() {
        trackProgressionJob = viewModelScope.launch {
            musicPlayer.currentPlayback.collect {
                _trackProgression.value = it
            }
        }
    }
}
