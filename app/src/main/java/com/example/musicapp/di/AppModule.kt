package com.example.musicapp.di

import android.app.Application
import androidx.room.Room
import com.example.musicapp.database.TrackDatabase
import com.example.musicapp.database.repository.TrackRepositoryImpl
import com.example.musicapp.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideTrackDatabase(app: Application): TrackDatabase{
        return Room.databaseBuilder(
            app,
            TrackDatabase::class.java,
            TrackDatabase.DATABASE_NAME).build()
    }

    @Provides
    fun provideTrackRepository(db: TrackDatabase): TrackRepository{
        return TrackRepositoryImpl(db.trackDao)
    }
}