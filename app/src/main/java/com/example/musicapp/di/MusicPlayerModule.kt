package com.example.musicapp.di

import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.musicplayers.ExoMusicPlayerImpl
import com.example.musicapp.musicplayers.ExoMusicPlayerService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class MusicPlayerModule {

    @Binds
    abstract fun bindExoMusicPlayer(exoMusicPlayerImpl: ExoMusicPlayerImpl): ExoMusicPlayer

    @Binds
    abstract fun bindExoMusicPlayerService(exoMusicPlayerImpl: ExoMusicPlayerImpl): ExoMusicPlayerService
}