package com.example.musicapp.usecases

import com.example.musicapp.models.Track
import javax.inject.Inject

class TrackListUseCase @Inject constructor() {

    var currentPosition: Int? = null

    suspend fun getTrackList(): List<Track> {
        return listOf(
            Track(1, "Track1", "Artist1", "3:01", ""),
            Track(2, "Track2", "Artist2", "3:01", ""),
            Track(3, "Track3", "Artist3", "3:01", ""),
            Track(4, "Track4", "Artist4", "3:01", ""),
            Track(5, "Track5", "Artist5", "3:01", ""),
            Track(7, "Track7", "Artist1", "3:01", ""),
            Track(8, "Track8", "Artist2", "3:01", ""),
            Track(9, "Track9", "Artist3", "3:01", ""),
            Track(10, "Track10", "Artist4", "3:01", ""),
            Track(11, "Track11", "Artist5", "3:01", ""),
            Track(12, "Track12", "Artist1", "3:01", ""),
            Track(13, "Track13", "Artist2", "3:01", ""),
            Track(14, "Track14", "Artist3", "3:01", ""),
            Track(15, "Track15", "Artist4", "3:01", ""),
            Track(16, "Track16", "Artist5", "3:01", ""),
            Track(17, "Track17", "Artist1", "3:01", ""),
            Track(18, "Track18", "Artist2", "3:01", ""),
            Track(19, "Track19", "Artist3", "3:01", ""),
            Track(20, "Track20", "Artist4", "3:01", ""),
            Track(21, "Track21", "Artist5", "3:01", ""),
        )
    }

}