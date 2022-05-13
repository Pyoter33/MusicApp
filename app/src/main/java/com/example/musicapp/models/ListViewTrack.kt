package com.example.musicapp.models

data class ListViewTrack(val id: Long, val name: String, val artist: String, val length: Int, val path: String, var isPlaying: Boolean = false)
