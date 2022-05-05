package com.example.musicapp.usecases

import android.os.Environment
import com.example.musicapp.database.Track
import com.example.musicapp.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackUseCase @Inject constructor(private val repository: TrackRepository) {

    suspend fun getTrackList(): Flow<List<Track>> {
        return repository.getAll()
    }

}