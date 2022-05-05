package com.example.musicapp.database.repository

import android.os.Environment
import com.example.musicapp.database.Track
import com.example.musicapp.database.TrackDao
import com.example.musicapp.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TrackRepositoryImpl(
    private val dao: TrackDao
): TrackRepository {
    override fun getAll(): Flow<List<Track>> {
        val flow = flowOf(listOf(
            Track(2, "Track2", "Artist2", "${Environment.getExternalStorageDirectory()}/Tracks/Gwizd.ogg", 1),
            Track(1, "Track1", "Artist1", "${Environment.getExternalStorageDirectory()}/Tracks/01 Intro.m4a", 4),
            Track(3, "Track3", "Artist3", "${Environment.getExternalStorageDirectory()}/Tracks/Over_the_Horizon.mp3", 205),
            Track(4, "Track4", "Artist4", "", 0),
            Track(5, "Track5", "Artist5", "", 0),
            Track(7, "Track7", "Artist1", "", 0),
            Track(8, "Track8", "Artist2", "", 0),
            Track(9, "Track9", "Artist3", "", 0),
            Track(10, "Track10", "Artist4", "", 0),
            Track(11, "Track11", "Artist5", "", 0),
            Track(12, "Track12", "Artist1", "", 0),
            Track(13, "Track13", "Artist2", "", 0),
            Track(14, "Track14", "Artist3", "", 0),
            Track(15, "Track15", "Artist4", "", 0),
            Track(16, "Track16", "Artist5", "", 0),
            Track(17, "Track17", "Artist1", "", 0),
            Track(18, "Track18", "Artist2", "", 0),
            Track(19, "Track19", "Artist3", "", 0),
            Track(20, "Track20", "Artist4", "", 0),
            Track(21, "Track21", "Artist5", "${Environment.getExternalStorageDirectory()}/Tracks/Skarb.ogg", 1),
        ))

        //return dao.getAll()
        return flow
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