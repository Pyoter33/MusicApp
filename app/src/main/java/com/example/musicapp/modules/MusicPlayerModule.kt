package com.example.musicapp.modules

import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.musicplayers.ExoMusicPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent

@InstallIn(ServiceComponent::class)
@Module
abstract class MusicPlayerModule {

    @ServiceScoped
    @Binds
    abstract fun bindExoMusicPlayer(exoMusicPlayerImpl: ExoMusicPlayerImpl): ExoMusicPlayer
}