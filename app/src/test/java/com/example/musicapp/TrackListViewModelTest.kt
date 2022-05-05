package com.example.musicapp

import com.example.musicapp.database.Track
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.usecases.TrackUseCase
import com.example.musicapp.viewmodels.TrackListViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TrackListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_getTrackListEmpty() {
            val expected = 0
            val useCase: TrackUseCase = mock()
            val player: ExoMusicPlayer = mock()
            val viewModel = TrackListViewModel(useCase, player)

            whenever(useCase.getTrackList()).thenReturn(flowOf(listOf()))

            runBlocking { viewModel.getTrackList() }
            assertEquals(expected, viewModel.trackList.value!!.size)
    }
}