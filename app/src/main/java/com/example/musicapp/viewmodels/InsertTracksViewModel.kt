package com.example.musicapp.viewmodels

import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.database.Track
import com.example.musicapp.usecases.InsertTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InsertTracksViewModel @Inject constructor(
    private val insertTracksUseCase: InsertTracksUseCase
): ViewModel() {
    fun insertTracks(){
        val path = "${Environment.getExternalStorageDirectory()}/Tracks"
        val file = File(path)
        val list = file.list()
        val mmr = MediaMetadataRetriever()
        val setTracks = mutableListOf<Track>().toMutableSet()

        for (elem in list!!) {
            mmr.setDataSource("$path/$elem")
            var title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            var artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            var path = "$path/$elem"
            var lengthString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            var length = Integer.parseInt(lengthString)
            if (!setTracks.contains(Track(0,title,artist,path,length))) {
                setTracks.add(Track(0, title, artist, path, length))
            }
        }

        viewModelScope.launch {
            insertTracksUseCase.insertTracks(setTracks)
        }
    }
}