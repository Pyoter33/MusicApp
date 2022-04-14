package com.example.musicapp.modules

import com.example.musicapp.utils.MusicPlayer
import com.example.musicapp.utils.MusicPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class MusicPlayerModule {

    @Binds
    @Singleton
    abstract fun bindMusicPlayer(musicPlayerImpl: MusicPlayerImpl): MusicPlayer
}