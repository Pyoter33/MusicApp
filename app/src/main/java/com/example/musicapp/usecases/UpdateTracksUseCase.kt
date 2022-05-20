package com.example.musicapp.usecases

import com.example.musicapp.repository.Track
import javax.inject.Inject

class UpdateTracksUseCase @Inject constructor(
    private val repository: TrackRepository
) {
    suspend fun updateTracks(tracks: List<Track>, paths: List<String>){
        repository.insertAndDeleteInTransaction(tracks, paths)
    }

}