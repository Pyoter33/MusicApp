package com.example.musicapp

import com.example.musicapp.database.Track
import com.example.musicapp.repository.TrackRepository
import com.example.musicapp.usecases.TrackUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class TrackUseCaseTest {

    @Mock
    lateinit var repository: TrackRepository

    @InjectMocks
    lateinit var useCase: TrackUseCase

    @Test
    fun test_getTrackListEmpty() {
        runBlocking {
            val expected = listOf<Track>()
//            val repository: TrackRepository = mock()
//            val useCase = TrackUseCase(repository)

            whenever(repository.getAll()).thenReturn(flowOf(listOf()))

            val result = useCase.getTrackList().firstOrNull()

            Assert.assertEquals(expected, result)
        }
    }

    @Test
    fun test_getTrackListNonEmpty() {
        runBlocking {
            //given
            val track1 = Track(4, "Track4", "Artist4", "", 0)
            val track2 = Track(5, "Track5", "Artist5", "", 0)
            val track3 = Track(7, "Track7", "Artist1", "", 0)
            val expected = listOf(track1, track2, track3)
            val repository: TrackRepository = mock()
            val useCase = TrackUseCase(repository)

            whenever(repository.getAll()).thenReturn(flowOf(listOf(track1, track2, track3)))

            //when
            val result = useCase.getTrackList().firstOrNull()

            //then
            Assert.assertEquals(expected, result)
            //verify get all
            verify(repository).getAll()
        }
    }

}