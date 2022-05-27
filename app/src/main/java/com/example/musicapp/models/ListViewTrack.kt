package com.example.musicapp.models

data class ListViewTrack(val id: Long, val name: String, val artist: String, val length: Int, val path: String, val imageByteArray: ByteArray, var isPlaying: Boolean = false) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListViewTrack

        if (id != other.id) return false
        if (name != other.name) return false
        if (artist != other.artist) return false
        if (length != other.length) return false
        if (path != other.path) return false
        if (!imageByteArray.contentEquals(other.imageByteArray)) return false
        if (isPlaying != other.isPlaying) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + length
        result = 31 * result + path.hashCode()
        result = 31 * result + imageByteArray.contentHashCode()
        result = 31 * result + isPlaying.hashCode()
        return result
    }
}
