package com.example.musicapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackDao{
    @Insert
    fun insertAll(vararg tracks: Track)

    @Query("SELECT * FROM track")
    fun getAll(): List<Track>
}