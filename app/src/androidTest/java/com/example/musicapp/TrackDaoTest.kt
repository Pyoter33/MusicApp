package com.example.musicapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicapp.repository.Track
import com.example.musicapp.repository.TrackDao
import com.example.musicapp.repository.TrackDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class TrackDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var trackDatabase: TrackDatabase
    private lateinit var trackDao: TrackDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        trackDatabase = Room.inMemoryDatabaseBuilder(context, TrackDatabase::class.java)
            .allowMainThreadQueries()
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        trackDao = trackDatabase.trackDao
    }

    @After
    fun teardown() {
        trackDatabase.close()
    }

    @Test
    fun shouldInsertAll() = runBlocking {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        val expectedList = listOf(testTrack1, testTrack2)

        //when
        trackDao.insertAll(expectedList)

        //then
        Assert.assertEquals(expectedList, trackDao.getAll().firstOrNull())

    }

    @Test
    fun shouldDeleteTracks() = runBlocking {
        //given
        val testTrack1 = Track(1, "", "", "/.", 0)
        val testTrack2 = Track(2, "", "", "/", 0)
        trackDao.insertAll(listOf(testTrack1, testTrack2))
        val expectedList = listOf(testTrack1)

        //when
        trackDao.deleteTracks(listOf("/"))

        //then
        Assert.assertEquals(expectedList, trackDao.getAll().firstOrNull())
    }

    @Test
    fun shouldGetTrackById() = runBlocking {
        //given
        val testTrack1 = Track(1, "", "", "", 0)
        val testTrack2 = Track(2, "", "", "", 0)
        trackDao.insertAll(listOf(testTrack1, testTrack2))

        //when
        val actualTrack = trackDao.getTrackById(1)
        //then
        Assert.assertEquals(testTrack1, actualTrack)
    }

    @Test
    fun shouldInsertAndDeleteInTransaction() = runBlocking {
        //given
        val testTrack1 = Track(1, "", "", "/", 0)
        val testTrack2 = Track(2, "", "", "/.", 0)
        val testTrack3 = Track(3, "", "", "/..", 0)
        val expectedList = listOf(testTrack2, testTrack3)
        trackDao.insertAll(listOf(testTrack1, testTrack2))

        //when
        trackDao.insertAndDeleteInTransaction(listOf(testTrack3), listOf("/"))

        //then
        Assert.assertEquals(expectedList, trackDao.getAll().firstOrNull())
    }

}