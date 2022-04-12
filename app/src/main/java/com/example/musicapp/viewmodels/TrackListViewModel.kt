package com.example.musicapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.models.UITrack
import com.example.musicapp.usecases.TrackListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(private val useCase: TrackListUseCase) : ViewModel() {

    private val _trackList = MutableLiveData<List<UITrack>>()
    val trackList: LiveData<List<UITrack>> = _trackList

    private val _currentTrack = MutableLiveData<UITrack>().also {
        getUITrackList()
    }
    val currentTrack: LiveData<UITrack> = _currentTrack

    private val _positionToNotify = MutableLiveData<Int>()
    val positionToNotify: LiveData<Int> = _positionToNotify

    private fun getUITrackList() {
        viewModelScope.launch {
            _trackList.value = useCase.getTrackList().map { track ->
                UITrack(track.id, track.name, track.artist, track.length, track.path)
            }
        }
    }

    fun updateTracks(currentUITrack: UITrack, position: Int) {
        _currentTrack.value?.apply {
            isPlaying = false
        }
        useCase.currentPosition?.let { currentPosition ->
            _positionToNotify.value = currentPosition
        }
        _positionToNotify.value = position
        currentUITrack.isPlaying = true
        _currentTrack.value = currentUITrack
        useCase.currentPosition = position
    }

    fun playNextTrack() { //remain here or put in use case? how?
        useCase.currentPosition?.let { currentPosition ->
            val newPosition = currentPosition + 1
            val list = trackList.value!!
            if (newPosition < list.size) {
                updateTracks(list[newPosition], newPosition)
            }
        }
    }

    fun playPreviousTrack() {
        useCase.currentPosition?.let { currentPosition ->
            val newPosition = currentPosition - 1
            val list = trackList.value!!
            if (newPosition >= 0) {
                updateTracks(list[newPosition], newPosition)
            }
        }
    }
}