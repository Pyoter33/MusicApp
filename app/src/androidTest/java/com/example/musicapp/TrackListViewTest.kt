package com.example.musicapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.musicapp.adapters.TracksListAdapter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TrackListViewTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val scenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun shouldFirstTrackName() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.textCurrentTrackName))
            .check(matches(withText("Test 1")))
    }

    @Test
    fun shouldNextTrackName() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.nextButton))
            .perform(click())

        onView(withId(R.id.textCurrentTrackName))
            .check(matches(withText("Test 2")))
    }

    @Test
    fun shouldLayoutTrackControllerInvisible() {
        onView(withId(R.id.layoutTrackController))
            .check(matches(not(isDisplayed())))
    }


    @Test
    fun shouldLayoutTrackControllerVisible() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )
        onView(withId(R.id.layoutTrackController))
            .check(matches(isDisplayed()))
    }

    @Test
    fun shouldTrackItemBackgroundBase() {
        onView(withId(R.id.viewRecyclerTracks))
            .check(
                matches(
                    TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(
                        0,
                        R.id.itemTrackLayout,
                        R.drawable.background_track,
                        null
                    )
                )
            )
    }

    @Test
    fun shouldTrackItemBackgroundPlaying() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.viewRecyclerTracks))
            .check(
                matches(
                    TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(
                        0,
                        R.id.itemTrackLayout,
                        R.drawable.background_track_playing,
                        null
                    )
                )
            )
    }

    @Test
    fun shouldResumePauseButtonResume() { //error when running with other tests
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.layoutTrackController))
            .check(matches(isDisplayed()))

        onView(withId(R.id.resumePauseButton))
            .check(
                matches(
                    TestUtils.checkImageButtonDrawable(
                        R.drawable.icon_pause,
                        R.color.track_playing_background
                    )
                )
            )
    }

    @Test
    fun shouldResumePauseButtonPause() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.resumePauseButton))
            .perform(click())

        onView(withId(R.id.resumePauseButton))
            .check(
                matches(
                    TestUtils.checkImageButtonDrawable(
                        R.drawable.icon_play_arrow,
                        R.color.track_playing_background
                    )
                )
            )
    }

    @Test
    fun shouldItemBackgroundNextTrackChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.nextButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(
                matches(
                    TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(
                        0,
                        R.id.itemTrackLayout,
                        R.drawable.background_track,
                        null
                    )
                )
            )

        onView(withId(R.id.viewRecyclerTracks))
            .check(
                matches(
                    TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(
                        1,
                        R.id.itemTrackLayout,
                        R.drawable.background_track_playing,
                        null
                    )
                )
            )
    }

    @Test
    fun shouldItemBackgroundPreviousTrackChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    1,
                    click()
                )
            )

        onView(withId(R.id.previousButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(
                matches(
                    TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(
                        1,
                        R.id.itemTrackLayout,
                        R.drawable.background_track,
                        null
                    )
                )
            )

        onView(withId(R.id.viewRecyclerTracks))
            .check(
                matches(
                    TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(
                        0,
                        R.id.itemTrackLayout,
                        R.drawable.background_track_playing,
                        null
                    )
                )
            )
    }

    @Test
    fun shouldItemBackgroundPreviousTrackFirstNotChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.previousButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(
                matches(
                    TestUtils.recyclerViewAtPositionCheckBackgroundDrawable(
                        0,
                        R.id.itemTrackLayout,
                        R.drawable.background_track_playing,
                        null
                    )
                )
            )
    }
}