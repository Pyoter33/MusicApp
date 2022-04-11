package com.example.musicapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapp.models.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.FieldPosition
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor() : ViewModel() {

    val list = listOf(
        Track(1, "Track1", "Artist1", "3:01", false),
        Track(2, "Track2", "Artist2", "3:01", false),
        Track(3, "Track3", "Artist3", "3:01", false),
        Track(4, "Track4", "Artist4", "3:01", false),
        Track(5, "Track5", "Artist5", "3:01", false),
        Track(7, "Track7", "Artist1", "3:01", false),
        Track(8, "Track8", "Artist2", "3:01", false),
        Track(9, "Track9", "Artist3", "3:01", false),
        Track(10, "Track10", "Artist4", "3:01", false),
        Track(11, "Track11", "Artist5", "3:01", false),
        Track(12, "Track12", "Artist1", "3:01", false),
        Track(13, "Track13", "Artist2", "3:01", false),
        Track(14, "Track14", "Artist3", "3:01", false),
        Track(15, "Track15", "Artist4", "3:01", false),
        Track(16, "Track16", "Artist5", "3:01", false),
        Track(17, "Track17", "Artist1", "3:01", false),
        Track(18, "Track18", "Artist2", "3:01", false),
        Track(19, "Track19", "Artist3", "3:01", false),
        Track(20, "Track20", "Artist4", "3:01", false),
        Track(21, "Track21", "Artist5", "3:01", false),
    )

    private val _currentTrack = MutableLiveData<Track?>(null)
    val currentTrack: LiveData<Track?> = _currentTrack

    private val _positionToNotify = MutableLiveData<Int>()
    val positionToNotify: LiveData<Int> = _positionToNotify

    var currentPosition: Int? = null

    fun updateTracks(currentTrack: Track, position: Int) {
        _currentTrack.value?.apply {
            playing = false
        }
        currentPosition?.let { currentPosition ->
            _positionToNotify.value = currentPosition
        }
        _positionToNotify.value = position
        currentTrack.playing = true
        _currentTrack.value = currentTrack
        currentPosition = position
    }

    fun playNextTrack() {
        currentPosition?.let { currentPosition ->
            val newPosition = currentPosition + 1
            if(newPosition < list.size) {
                updateTracks(list[newPosition], newPosition)
            }
        }
    }

    fun playPreviousTrack() {
        currentPosition?.let { currentPosition ->
            val newPosition = currentPosition - 1
            if(newPosition >= 0) {
                updateTracks(list[newPosition], newPosition)
            }
        }
    }
}