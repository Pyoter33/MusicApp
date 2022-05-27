package com.example.musicapp

import android.os.Environment
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicapp.adapters.TracksListAdapter
import com.example.musicapp.repository.Track
import com.example.musicapp.repository.TrackDao
import com.example.musicapp.repository.TrackDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot.not
import org.junit.*
import java.io.FileOutputStream
import java.io.InputStream


class TrackListViewTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private lateinit var db: TrackDatabase
    private lateinit var dao: TrackDao

    @Before
    fun setDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.databaseBuilder(context, TrackDatabase::class.java, TrackDatabase.DATABASE_NAME).allowMainThreadQueries().build()
        dao = db.trackDao

        runBlocking {
            dao.insertAll(
                listOf(
                    Track(0, "Title 1", "Artist 1", "test", 2000),
                    Track(0, "Title 2", "Artist 2", "test", 2000),
                    Track(0, "Title 3", "Artist 3", "test", 2000)
                )
            )
        }


    }

    @After
    fun closeDatabase() {
        db.clearAllTables()
        db.close()
    }


    @Test
    fun shouldFirstTrackName() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.textCurrentTrackName))
            .check(matches(withText("Title 1")))
    }

    @Test
    fun shouldLayoutTrackControllerInvisible() {
        onView(withId(R.id.layoutTrackController))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun shouldLayoutTrackControllerVisible() {

        Thread.sleep(2000)
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.layoutTrackController))
            .check(matches(isDisplayed()))
    }

    @Test
    fun shouldTrackItemBackgroundBase() {
        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track, null)))
    }

    @Test
    fun shouldTrackItemBackgroundPlaying() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }

    @Test
    fun shouldResumePauseButtonResume() { //error when running with other tests
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.layoutTrackController))
            .check(matches(isDisplayed()))

        onView(withId(R.id.resumePauseButton))
            .check(matches(TestUtils.checkImageButtonDrawable(R.drawable.icon_pause, R.color.track_playing_background)))
    }

    @Test
    fun shouldResumePauseButtonPause() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.resumePauseButton))
            .perform(click())

        onView(withId(R.id.resumePauseButton))
            .check(matches(TestUtils.checkImageButtonDrawable(R.drawable.icon_play_arrow, R.color.track_playing_background)))
    }

    @Test
    fun shouldItemBackgroundNextTrackChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.nextButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track, null)))

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(1, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }

    @Test
    fun shouldItemBackgroundPreviousTrackChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(1, click()))

        onView(withId(R.id.previousButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(1, R.id.itemTrackLayout, R.drawable.background_track, null)))

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }

    @Test
    fun shouldItemBackgroundPreviousTrackFirstNotChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.previousButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }
}