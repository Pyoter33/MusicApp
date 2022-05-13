package com.example.musicapp.viewmodels

import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.database.Track
import com.example.musicapp.usecases.DeleteTracksUseCase
import com.example.musicapp.usecases.InsertTracksUseCase
import com.example.musicapp.usecases.TrackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InsertTracksViewModel @Inject constructor(
    private val insertTracksUseCase: InsertTracksUseCase,
    private val deleteTracksUseCase: DeleteTracksUseCase,
    private val trackUseCase: TrackUseCase
) : ViewModel(){

    fun insertTracks() {
        val path = "${Environment.getExternalStorageDirectory()}/Tracks"
        val file = File(path)
        val list = file.list()
        val mmr = MediaMetadataRetriever()
        val listTracks = mutableListOf<Track>()
        val listTracksToDb = mutableListOf<Track>()
        val listPaths = mutableListOf<String>()
        val listPathsToDb = mutableListOf<String>()

        for (elem in list!!) {
            mmr.setDataSource("$path/$elem")
            var title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            var artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            var path = "$path/$elem"
            var lengthString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            var length = Integer.parseInt(lengthString)
            listPaths.add(path)
            if (!listTracks.contains(Track(0, title, artist, path, length))) {
                listTracks.add(Track(0, title, artist, path, length))
                listTracksToDb.add(Track(0, title, artist, path, length))
            }
        }

        viewModelScope.launch {
            trackUseCase.getTrackList().collect { list ->
                for (track in list) {
                    for (trackSd in listTracks) {
                        if (trackSd.path == track.path) {
                            listTracksToDb.remove(trackSd)
                        }
                    }
                    if (!listPaths.contains(track.path)) {
                        Log.i("Test","${track.path}")
                        listPathsToDb.add(track.path!!)
                    }
                }
                insertTracksUseCase.insertTracks(listTracksToDb)
                deleteTracksUseCase.deleteTracks(listPathsToDb)
            }
        }
    }
}


