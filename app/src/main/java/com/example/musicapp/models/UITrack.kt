package com.example.musicapp.models

data class UITrack(val id: Long, val name: String, val artist: String, val length: String, val path: String, var isPlaying: Boolean = false)
