package com.example.musicapp

import android.app.Application
import android.os.Environment
import androidx.room.Room
import com.example.musicapp.di.PathModule
import com.example.musicapp.repository.TrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PathModule::class]
)
class PathModuleTest {
    @Provides
    fun providePath(): String {
        return "${Environment.getExternalStorageDirectory()}/TracksTest"
    }
}