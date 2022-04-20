package com.example.musicapp.repository

import com.example.musicapp.database.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAll(): Flow<List<Track>>

    suspend fun getTrackById(id: Int): Track?

    fun insertAll(vararg tracks: Track)

    suspend fun deleteTrack(track: Track)

}