package com.example.musicapp.modules

import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.musicplayers.ExoMusicPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class MusicPlayerModule {

    @Binds
    @Singleton
    abstract fun bindExoMusicPlayer(exoMusicPlayerImpl: ExoMusicPlayerImpl): ExoMusicPlayer
}