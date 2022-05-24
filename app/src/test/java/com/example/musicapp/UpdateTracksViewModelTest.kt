package com.example.musicapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.musicapp.usecases.GetTracksUseCase
import com.example.musicapp.usecases.UpdateTracksUseCase
import com.example.musicapp.viewmodels.UpdateTracksViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


@RunWith(MockitoJUnitRunner::class)
class UpdateTracksViewModelTest {

    @Mock
    private lateinit var updateTracksUseCase: UpdateTracksUseCase

    @Mock
    private lateinit var getTracksUseCase: GetTracksUseCase

    @InjectMocks
    private lateinit var viewModel: UpdateTracksViewModel

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @get:Rule
    var tempFolder: TemporaryFolder = TemporaryFolder()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun shouldUpdateTracks() {
        //given
        whenever(getTracksUseCase.getTrackList()).thenReturn(flowOf(listOf()))

        //when
        viewModel.updateTracks(tempFolder.newFolder().path)

        //then
        runBlocking { verify(updateTracksUseCase).updateTracks(any(), any()) }
    }
}