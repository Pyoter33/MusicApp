package com.example.musicapp.viewmodels

import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.repository.Track
import com.example.musicapp.usecases.UpdateTracksUseCase
import com.example.musicapp.usecases.GetTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateTracksViewModel @Inject constructor(
    private val updateTracksUseCase: UpdateTracksUseCase,
    private val getTracksUseCase: GetTracksUseCase
) : ViewModel() {

    fun updateTracks(path: String) {
        val file = File(path)
        val list = file.list()
        val mmr = MediaMetadataRetriever()
        val listTracks = mutableListOf<Track>()
        val listPaths = mutableListOf<String>()
        for (elem in list!!) {
            mmr.setDataSource("$path/$elem")
            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val fullPath = "$path/$elem"
            val lengthString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val a = mmr.embeddedPicture
            val length = Integer.parseInt(lengthString)
            listTracks.add(Track(0, title, artist, fullPath, length))
            listPaths.add(fullPath)
        }

        viewModelScope.launch {
            getTracksUseCase.getTrackList().collect { list ->

                val paths = list.map { it.path }.toSet()
                val tracksToAdd = listTracks.filter { it.path !in paths }
                val tracksToDelete = list.filter { it.path !in listPaths.toSet() }
                updateTracksUseCase.updateTracks(tracksToAdd, tracksToDelete.map { it.path!! })

                coroutineContext.job.cancel()
            }
        }
    }
}


