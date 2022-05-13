package com.example.musicapp.usecases

import android.media.MediaMetadataRetriever
import android.os.Environment
import com.example.musicapp.database.Track
import com.example.musicapp.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class InsertTracksUseCase @Inject constructor(
    private val repository: TrackRepository
) {
    suspend fun insertTracks(tracksList: List<Track>){
        for (track in tracksList) {
            repository.insertAll(track)
        }
    }

}