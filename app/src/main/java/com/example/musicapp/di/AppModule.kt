package com.example.musicapp.di

import android.app.Application
import androidx.room.Room
import com.example.musicapp.repository.TrackDatabase
import com.example.musicapp.repository.TrackRepositoryImpl
import com.example.musicapp.usecases.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTrackDatabase(app: Application): TrackDatabase{
        return Room.databaseBuilder(
            app,
            TrackDatabase::class.java,
            TrackDatabase.DATABASE_NAME).build()
    }

    @Provides
    @Singleton //?
    fun provideTrackRepository(db: TrackDatabase): TrackRepository {
        return TrackRepositoryImpl(db.trackDao)
    }

}