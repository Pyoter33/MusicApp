package com.example.musicapp.repository

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Track::class], version = 1)
abstract class TestDatabase: RoomDatabase() {
    abstract val trackDao: TrackDao

    companion object{
        const val DATABASE_NAME = "test_db"
    }
}