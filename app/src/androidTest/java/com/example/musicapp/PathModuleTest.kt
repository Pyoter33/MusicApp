package com.example.musicapp

import android.os.Environment
import com.example.musicapp.di.PathModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

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