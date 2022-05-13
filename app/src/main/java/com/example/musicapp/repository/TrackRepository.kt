package com.example.musicapp.repository

import com.example.musicapp.database.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    suspend fun getAll(): Flow<List<Track>>

    suspend fun getTrackById(id: Int): Track?

    suspend fun insertAll(tracks: Track)

    suspend fun deleteTrack(path: String)

}