package com.example.musicapp.usecases

import com.example.musicapp.repository.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAll(): Flow<List<Track>>

    suspend fun getTrackById(id: Int): Track?

    suspend fun insertAndDeleteInTransaction(tracks: List<Track>, paths: List<String>)
}