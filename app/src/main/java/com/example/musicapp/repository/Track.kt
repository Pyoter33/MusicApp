package com.example.musicapp.repository

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String?,
    val author: String?,
    val path: String,
    val length: Int?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Track

        if (id != other.id) return false
        if (title != other.title) return false
        if (author != other.author) return false
        if (path != other.path) return false
        if (length != other.length) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (author?.hashCode() ?: 0)
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (length ?: 0)
        return result
    }
}