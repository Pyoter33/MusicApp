package com.example.musicapp.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow


@Dao
interface TrackDao{
    @Insert
    suspend fun insertAll(tracks: List<Track>)

    @Query("SELECT * FROM track")
    fun getAll(): Flow<List<Track>>

    @Query("SELECT * FROM track WHERE id=:id")
    suspend fun getTrackById(id: Int): Track?

    @Query("DELETE FROM track WHERE path in (:paths)")
    suspend fun deleteTracks(paths: List<String>)

    @Transaction
    suspend fun insertAndDeleteInTransaction(tracks: List<Track>, paths: List<String>) {
        insertAll(tracks)
        deleteTracks(paths)
    }

}