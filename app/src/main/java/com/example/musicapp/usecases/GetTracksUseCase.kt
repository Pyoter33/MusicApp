package com.example.musicapp.usecases

import android.util.Log
import com.example.musicapp.repository.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(private val repository: TrackRepository) {

    fun getTrackList(): Flow<List<Track>> {
        return repository.getAll()
    }
}