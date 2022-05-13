package com.example.musicapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao{
    @Insert
    suspend fun insertAll(track: Track)

    @Query("SELECT * FROM track")
    fun getAll(): Flow<List<Track>>

    @Query("SELECT * FROM track WHERE id=:id")
    suspend fun getTrackById(id: Int): Track?

    @Query("DELETE FROM track WHERE path=:path")
    suspend fun deleteTrack(path: String)
}