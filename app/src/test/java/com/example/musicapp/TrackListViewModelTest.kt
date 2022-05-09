package com.example.musicapp

import com.example.musicapp.database.Track
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.repository.TrackRepository
import com.example.musicapp.usecases.TrackUseCase
import com.example.musicapp.viewmodels.TrackListViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.assertj.core.api.Assertions.assertThat

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TrackListViewModelTest {

    @Mock
    private lateinit var useCase: TrackUseCase

    @Mock
    private lateinit var player: ExoMusicPlayer

    @InjectMocks
    private lateinit var viewModel: TrackListViewModel

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun shouldGetTrackListNonEmpty() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "",0 )
        val testViewTrack1 = ListViewTrack(1, "", "", 0,"" )
        val testViewTrack2 = ListViewTrack(2, "", "", 0,"" )
        val expectedSize = 2
        val expectedList = listOf(testViewTrack1, testViewTrack2)
        runBlocking { whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2))) }

        //when
        viewModel.trackList

        //then
        println(viewModel.trackList.value?.first().toString())

        assertEquals(expectedSize, viewModel.trackList.value?.size)
        assertThat(viewModel.trackList.value?.first()).isEqualToComparingFieldByFieldRecursively(expectedList.first())
    }

    @Test
    fun shouldGetTrackListEmpty() {
        //given
        val expectedSize = 0
        runBlocking { whenever(useCase.getTrackList()).thenReturn(flowOf(listOf())) }

        //when
        viewModel.trackList

        //then
        assertEquals(expectedSize, viewModel.trackList.value?.size)
    }

    @Test
    fun shouldResumeTrack() {
        //given
        val expectedFlowValue = 1
        whenever(player.trackPosition).thenReturn(flowOf(expectedFlowValue))

        //when
        viewModel.resumeTrack()

        //then
        verify(player).onResume()
        assertEquals(expectedFlowValue, viewModel.trackProgression.value)
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
        val testViewTrack = ListViewTrack(1, "", "", 0, "")
        val expectedTrack = ListViewTrack(1, "", "", 0, "", true)
        val expectedPosition = 0
        val expectedFlowValue = 1
        whenever(player.trackPosition).thenReturn(flowOf(expectedFlowValue))

        //when
        viewModel.updateTracks(testViewTrack, expectedPosition)

        //then
        verify(player).initialize(any())
        assertThat(viewModel.currentTrack.value).isEqualToComparingFieldByFieldRecursively(expectedTrack)
        assertEquals(expectedFlowValue, viewModel.trackProgression.value)
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }

    @Test
    fun shouldPlayNextTrackNonEnd() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "",0 )
        val testViewTrack1 = ListViewTrack(1, "", "", 0,"" )
        val testViewTrack2 = ListViewTrack(2, "", "", 0,"", true)

        val currentPosition = 0
        val expectedPosition = 1

        runBlocking { whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2))) }
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.updateTracks(testViewTrack1, currentPosition)

        //when
        viewModel.playNextTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualToComparingFieldByFieldRecursively(testViewTrack2)
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }

    @Test
    fun shouldPlayNextTrackEnd() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "",0 )
        val testViewTrack2 = ListViewTrack(2, "", "", 0,"", true)

        val currentPosition = 1
        val expectedPosition = 1

        runBlocking { whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2))) }
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.updateTracks(testViewTrack2, currentPosition)

        //when
        viewModel.playNextTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualToComparingFieldByFieldRecursively(testViewTrack2)
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }

    @Test
    fun shouldPlayPreviousTrackNonBegin() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "",0 )
        val testViewTrack1 = ListViewTrack(1, "", "", 0,"", true )
        val testViewTrack2 = ListViewTrack(2, "", "", 0,"")

        val currentPosition = 1
        val expectedPosition = 0

        runBlocking { whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2))) }
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.updateTracks(testViewTrack2, currentPosition)

        //when
        viewModel.playPreviousTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualToComparingFieldByFieldRecursively(testViewTrack1)
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }

    @Test
    fun shouldPlayPreviousTrackBegin() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "",0 )
        val testViewTrack1 = ListViewTrack(1, "", "", 0,"", true )

        val currentPosition = 0
        val expectedPosition = 0

        runBlocking { whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2))) }
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.updateTracks(testViewTrack1, currentPosition)

        //when
        viewModel.playPreviousTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualToComparingFieldByFieldRecursively(testViewTrack1)
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }


    @Test
    fun shouldSeekOnTrack() {
        //given
        val timeStamp = 0

        //when
        viewModel.seekOnTrack(timeStamp)

        //then
        verify(player).onSeek(any())
    }
}