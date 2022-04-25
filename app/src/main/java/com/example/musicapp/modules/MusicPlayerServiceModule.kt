package com.example.musicapp.modules

import com.example.musicapp.services.MusicPlayerService
import com.example.musicapp.services.MusicPlayerServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(ServiceComponent::class)
@Module
abstract class MusicPlayerServiceModule {

    @Binds
    @ServiceScoped
    abstract fun bindMusicPlayerService(musicPlayerServiceImpl: MusicPlayerServiceImpl): MusicPlayerService
}