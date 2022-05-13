package com.example.musicapp.usecases

import com.example.musicapp.database.Track
import com.example.musicapp.repository.TrackRepository
import javax.inject.Inject

class DeleteTracksUseCase @Inject constructor(
    private val repository: TrackRepository
) {
    suspend fun deleteTracks(pathsList: List<String>){
        for (path in pathsList) {
            repository.deleteTrack(path)
        }
    }
}