package com.example.musicapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String?,
    val author: String?,
    val path: String?,
    val length: Int?
)