package com.example.musicapp

import com.example.musicapp.database.Track
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.repository.TrackRepository
import com.example.musicapp.usecases.TrackUseCase
import com.example.musicapp.viewmodels.TrackListViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.mockito.kotlin.verifyBlocking

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TrackListViewModelTest {

    @Mock
    private lateinit var useCase: TrackUseCase

    @Mock
    lateinit var repository: TrackRepository

    @Mock
    private lateinit var player: ExoMusicPlayer

    @InjectMocks
    private lateinit var viewModel: TrackListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldGetTrackListEmpty()  {
        //given
        val expected = listOf<ListViewTrack>()
        runBlocking {
           whenever(useCase.getTrackList()).thenReturn(flowOf(listOf()))

           //when
           viewModel.getTrackList()
       }

        //then
        assertEquals(expected, viewModel.trackList.value) //?
    }

    @Test
    fun shouldResumeTrack() {
        //when
        viewModel.resumeTrack()

        //then
        verify(player).onResume()
    }

    @Test
    fun shouldPauseTrack() {
        //when
        viewModel.pauseTrack()

        //then
        verify(player).onPause()
    }

    @Test
    fun shouldUpdateTracks() {
        //given
        val track1 = ListViewTrack(4, "Track4", "Artist4", 0, "")

        //when
        viewModel.updateTracks(track1, 0)

        //then
        verify(player).initialize(any())
    }

}