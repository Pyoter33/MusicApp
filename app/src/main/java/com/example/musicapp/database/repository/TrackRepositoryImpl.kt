package com.example.musicapp.database.repository

import com.example.musicapp.database.Track
import com.example.musicapp.database.TrackDao
import com.example.musicapp.repository.TrackRepository
import kotlinx.coroutines.flow.Flow

class TrackRepositoryImpl(
    private val dao: TrackDao
): TrackRepository {
    override suspend fun getAll(): Flow<List<Track>> {
        return dao.getAll()
    }

    override suspend fun getTrackById(id: Int): Track? {
        return dao.getTrackById(id)
    }

    override suspend fun insertAll(track: Track) {
        dao.insertAll(track)
    }

    override suspend fun deleteTrack(track: Track) {
        dao.deleteTrack(track)
    }
}