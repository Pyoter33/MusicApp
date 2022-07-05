package com.example.musicapp.viewmodels

import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
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

    companion object{
        val ACCEPTED_EXTENSIONS = listOf("mp3", "oog", "m4a", "mp4")
    }

    fun updateTracks(path: String) {
        val file = File(path)
        val list = file.list() ?: arrayOf()
        val mmr = MediaMetadataRetriever()
        val listTracks = mutableListOf<Track>()
        val listPaths = mutableListOf<String>()
        for (elem in list) {
            val fullPath = "$path/$elem"
            if(!File(fullPath).exists()) continue
            if(elem.substringAfter('.') !in ACCEPTED_EXTENSIONS) continue
            mmr.setDataSource("$path/$elem")
            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: elem.substringBefore('.')
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val lengthString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val length = Integer.parseInt(lengthString)
            listTracks.add(Track(0, title, artist, fullPath, length))
            listPaths.add(fullPath)
        }

        viewModelScope.launch {
            getTracksUseCase.getTrackList().collect { list ->
                val paths = list.map { it.path }.toSet()
                val tracksToAdd = listTracks.filter { it.path !in paths }
                val tracksToDelete = list.filter { it.path !in listPaths.toSet() }
                updateTracksUseCase.updateTracks(tracksToAdd, tracksToDelete.map { it.path })

                coroutineContext.job.cancel()
            }
        }
    }

    fun deleteTracks(path: String) {
        val file = File(path)
        val list = file.list() ?: arrayOf()
        val listPaths = mutableListOf<String>()
        for (elem in list) {
            val fullPath = "$path/$elem"
            listPaths.add(fullPath)
        }
        viewModelScope.launch {
            getTracksUseCase.getTrackList().collect { list ->
                val tracksToDelete = list.filter { it.path !in listPaths.toSet() }
                updateTracksUseCase.updateTracks(listOf(), tracksToDelete.map { it.path })
                coroutineContext.job.cancel()
            }
        }
    }
}


