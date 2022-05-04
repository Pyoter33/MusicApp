package com.example.musicapp.database.repository

import com.example.musicapp.database.Track
import com.example.musicapp.database.TrackDao
import com.example.musicapp.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val dao: TrackDao
): TrackRepository {
    override fun getAll(): Flow<List<Track>> {
        return dao.getAll()
    }

    override suspend fun getTrackById(id: Int): Track? {
        return dao.getTrackById(id)
    }

    override fun insertAll(vararg tracks: Track) {
        dao.insertAll(*tracks)
    }

    override suspend fun deleteTrack(track: Track) {
        dao.deleteTrack(track)
    }
}