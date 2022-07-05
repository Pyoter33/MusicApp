package com.example.musicapp.di

import android.app.Application
import android.os.Environment
import androidx.room.Room
import com.example.musicapp.repository.TrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PathModule {

    @Provides
    fun providePath(): String {
        return "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}"
    }
}