package com.example.musicapp.other

import android.os.FileObserver
import android.util.Log

class DirectoryObserver(path: String): FileObserver(path) {
    override fun onEvent(event: Int, path: String?) {
        Log.i("Track added","to $path added!!!!!")
    }
}