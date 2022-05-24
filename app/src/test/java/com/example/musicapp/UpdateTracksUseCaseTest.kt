package com.example.musicapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.musicapp.repository.Track
import com.example.musicapp.usecases.TrackRepository
import com.example.musicapp.usecases.UpdateTracksUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class UpdateTracksUseCaseTest {
    @Mock
    lateinit var repository: TrackRepository

    @InjectMocks
    lateinit var useCase: UpdateTracksUseCase

    @Test
    fun shouldUpdateTracks() {
        runBlocking {
            //given
            val tracks = listOf<Track>()
            val paths = listOf<String>()

            //when
            useCase.updateTracks(tracks, paths)

            //then
            verify(repository).insertAndDeleteInTransaction(any(), any())
        }
    }
}