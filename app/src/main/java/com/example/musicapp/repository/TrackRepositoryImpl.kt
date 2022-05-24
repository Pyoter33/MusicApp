package com.example.musicapp.repository

import com.example.musicapp.usecases.TrackRepository
import kotlinx.coroutines.flow.Flow

class TrackRepositoryImpl(
    private val dao: TrackDao
): TrackRepository {
    override fun getAll(): Flow<List<Track>> = dao.getAll()

    override suspend fun getTrackById(id: Int): Track? {
        return dao.getTrackById(id)
    }

    override suspend fun insertAndDeleteInTransaction(tracks: List<Track>, paths: List<String>) {
        dao.insertAndDeleteInTransaction(tracks, paths)
    }

}