package com.example.musicapp

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.musicapp.adapters.TracksListAdapter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TrackDetailsViewTest {
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

        onView(withId(R.id.layoutTrackController)).perform(click())

        onView(withId(R.id.textTrackName))
            .check(matches(withText("Test 1")))
    }

    @Test
    fun shouldFirstTrackTime() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.layoutTrackController)).perform(click())

        onView(withId(R.id.textTrackLength))
            .check(matches(withText("0:04")))
    }

    @Test
    fun shouldSecondTrackName() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    1,
                    click()
                )
            )

        onView(withId(R.id.layoutTrackController)).perform(click())

        onView(withId(R.id.textTrackName))
            .check(matches(withText("Test 2")))
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

        onView(withId(R.id.layoutTrackController)).perform(click())
        onView(withId(R.id.nextButton)).perform(click())

        onView(withId(R.id.textTrackName))
            .check(matches(withText("Test 2")))
    }

    @Test
    fun shouldPreviousTrackName() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    1,
                    click()
                )
            )

        onView(withId(R.id.layoutTrackController)).perform(click())
        onView(withId(R.id.previousButton)).perform(click())

        onView(withId(R.id.textTrackName))
            .check(matches(withText("Test 1")))
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

        onView(withId(R.id.layoutTrackController)).perform(click())
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
    fun shouldResumePauseButtonResume() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.layoutTrackController)).perform(click())

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
    fun shouldResumePauseButtonPauseFromList() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.resumePauseButton))
            .perform(click())
        onView(withId(R.id.layoutTrackController)).perform(click())

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
    fun shouldResumePauseButtonNextResume() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(
                    0,
                    click()
                )
            )

        onView(withId(R.id.layoutTrackController)).perform(click())
        onView(withId(R.id.resumePauseButton))
            .perform(click())
        onView(withId(R.id.nextButton)).perform(click())

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
}