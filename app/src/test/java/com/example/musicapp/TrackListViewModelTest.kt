package com.example.musicapp

import com.example.musicapp.repository.Track
import com.example.musicapp.models.ListViewTrack
import com.example.musicapp.musicplayers.ExoMusicPlayer
import com.example.musicapp.usecases.GetTracksUseCase
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.Assert.*
import org.assertj.core.api.Assertions.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TrackListViewModelTest {

    @Mock
    private lateinit var useCase: GetTracksUseCase

    @Mock
    private lateinit var player: ExoMusicPlayer

    @InjectMocks
    private lateinit var viewModel: TrackListViewModel

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    fun <T> LiveData<T>.getOrAwaitValue( //await data from flow with as live data
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data = o
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    @Test
    fun shouldGetTrackListNonEmpty() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        val testViewTrack1 = ListViewTrack(1, "", "", 0, "", byteArrayOf())
        val testViewTrack2 = ListViewTrack(2, "", "", 0, "", byteArrayOf())
        val expectedSize = 2
        val expectedList = listOf(testViewTrack1, testViewTrack2)
        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2)))

        //when
        viewModel.trackList

        //then
        assertEquals(expectedSize, viewModel.trackList.getOrAwaitValue().size)
        assertThat(viewModel.trackList.getOrAwaitValue().first()).isEqualTo(expectedList.first())
    }

    @Test
    fun shouldGetTrackListEmpty() {
        //given
        val expectedSize = 0
        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf()))

        //when
        viewModel.trackList

        //then
        assertEquals(expectedSize, viewModel.trackList.getOrAwaitValue().size)
    }

    @Test
    fun shouldResumeTrack() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val expectedPosition = 0
        val expectedFlowValue = 1
        whenever(player.trackPosition).thenReturn(flowOf(expectedFlowValue))
        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1)))
        viewModel.trackList.getOrAwaitValue()
        viewModel.updateTracks(expectedPosition)

        //when
        viewModel.resumeTrack()

        //then
        verify(player).onResume()
        assertEquals(expectedFlowValue, viewModel.trackProgression.value)
    }

    @Test
    fun shouldPauseTrack() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val expectedPosition = 0
        whenever(player.trackPosition).thenReturn(flowOf())
        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1)))
        viewModel.trackList.getOrAwaitValue()
        viewModel.updateTracks(expectedPosition)

        //when
        viewModel.pauseTrack()

        //then
        verify(player).onPause()
    }

    @Test
    fun shouldUpdateTracks() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val expectedTrack = ListViewTrack(1, "", "", 0, "", byteArrayOf(),true)
        val expectedPosition = 0
        val expectedFlowValue = 1
        whenever(player.trackPosition).thenReturn(flowOf(expectedFlowValue))
        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1)))
        viewModel.trackList.getOrAwaitValue()

        //when
        viewModel.updateTracks(expectedPosition)

        //then
        verify(player).initialize(any())
        assertThat(viewModel.currentTrack.value).isEqualTo(expectedTrack)
        assertEquals(expectedFlowValue, viewModel.trackProgression.value)
        assertEquals(expectedPosition, viewModel.positionToNotify.value)

    }

    @Test
    fun shouldPlayNextTrackNonEnd() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        val testViewTrack1 = ListViewTrack(1, "", "", 0, "", byteArrayOf())
        val testViewTrack2 = ListViewTrack(2, "", "", 0, "", byteArrayOf(),true)

        val currentPosition = 0
        val expectedPosition = 1

        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2)))
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.trackList.getOrAwaitValue()
        viewModel.updateTracks(currentPosition)

        //when
        viewModel.playNextTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualTo(testViewTrack2)
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }

    @Test
    fun shouldPlayNextTrackEnd() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        val testViewTrack2 = ListViewTrack(2, "", "", 0, "", byteArrayOf(), true)

        val currentPosition = 1
        val expectedPosition = 1

        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2)))
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.trackList.getOrAwaitValue()
        viewModel.updateTracks(currentPosition)

        //when
        viewModel.playNextTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualTo(
            testViewTrack2
        )
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }

    @Test
    fun shouldPlayPreviousTrackNonBegin() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        val testViewTrack1 = ListViewTrack(1, "", "", 0, "", byteArrayOf(),true)
        val testViewTrack2 = ListViewTrack(2, "", "", 0, "", byteArrayOf())

        val currentPosition = 1
        val expectedPosition = 0

        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2)))
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.trackList.getOrAwaitValue()
        viewModel.updateTracks(currentPosition)

        //when
        viewModel.playPreviousTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualToComparingFieldByFieldRecursively(
            testViewTrack1
        )
        assertEquals(expectedPosition, viewModel.positionToNotify.value)
    }

    @Test
    fun shouldPlayPreviousTrackBegin() {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        val testViewTrack1 = ListViewTrack(1, "", "", 0, "", byteArrayOf(),true)

        val currentPosition = 0
        val expectedPosition = 0

        whenever(useCase.getTrackList()).thenReturn(flowOf(listOf(testTrack1, testTrack2)))
        whenever(player.trackPosition).thenReturn(flowOf())
        viewModel.trackList.getOrAwaitValue()
        viewModel.updateTracks(currentPosition)

        //when
        viewModel.playPreviousTrack()

        //then
        assertThat(viewModel.currentTrack.value).isEqualToComparingFieldByFieldRecursively(
            testViewTrack1
        )
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