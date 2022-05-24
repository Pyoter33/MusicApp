package com.example.musicapp

import com.example.musicapp.repository.Track
import com.example.musicapp.repository.TrackDao
import com.example.musicapp.repository.TrackRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TrackRepositoryTest {

    @Mock
    private lateinit var dao: TrackDao

    @InjectMocks
    private lateinit var repository: TrackRepositoryImpl

    @Test
    fun shouldGetAllEmpty() = runBlocking {
        //given
        whenever(dao.getAll()).thenReturn(flowOf(listOf()))
        val expected = flowOf<List<Track>>(listOf()).firstOrNull()

        //when
        val actual = repository.getAll().firstOrNull()

        //then
        verify(dao).getAll()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun shouldGetAllNonEmpty() = runBlocking {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        val expectedList = listOf(testTrack1, testTrack2)
        whenever(dao.getAll()).thenReturn(flowOf(listOf(testTrack1, testTrack2)))

        //when
        val actual = repository.getAll().firstOrNull()

        //then
        verify(dao).getAll()
        Assert.assertEquals(expectedList, actual)
    }

    @Test
    fun shouldGetTrackById() = runBlocking {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        whenever(dao.getTrackById(any())).thenReturn(testTrack1)

        //when
        val actual = repository.getTrackById(1)

        //then
        verify(dao).getTrackById(any())
        Assert.assertEquals(testTrack1, actual)
    }

    @Test
    fun shouldInsertAndDeleteInTransaction() = runBlocking {
        //when
       repository.insertAndDeleteInTransaction(listOf(), listOf())

        //then
        verify(dao).insertAndDeleteInTransaction(any(), any())
    }

}