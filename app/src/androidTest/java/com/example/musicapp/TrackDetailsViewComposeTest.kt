package com.example.musicapp

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.musicapp.adapters.TracksListAdapter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TrackDetailsViewComposeTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun shouldFirstTrackName() {
        composeTestRule.onNodeWithText("Test 1").performClick()
        composeTestRule.onNodeWithTag("TrackController").performClick()
        composeTestRule.onNodeWithTag("TrackInfoName").assertTextEquals("Test 1")
    }

    @Test
    fun shouldFirstTrackTime() {
        composeTestRule.onNodeWithText("Test 1").performClick()
        composeTestRule.onNodeWithTag("TrackController").performClick()
        composeTestRule.onNodeWithTag("TrackInfoTime").assertTextEquals("0:04")
    }

    @Test
    fun shouldSecondTrackName() {
        composeTestRule.onNodeWithText("Test 2").performClick()
        composeTestRule.onNodeWithTag("TrackController").performClick()
        composeTestRule.onNodeWithTag("TrackInfoName").assertTextEquals("Test 2")
    }
    @Test
    fun shouldNextTrackName() {
        composeTestRule.onNodeWithText("Test 1").performClick()
        composeTestRule.onNodeWithTag("TrackController").performClick()
        composeTestRule.onNodeWithTag("ButtonNextTrack").performClick()
        composeTestRule.onNodeWithTag("TrackInfoName").assertTextEquals("Test 2")
    }

    @Test
    fun shouldPreviousTrackName() {
        composeTestRule.onNodeWithText("Test 2").performClick()
        composeTestRule.onNodeWithTag("TrackController").performClick()
        composeTestRule.onNodeWithTag("ButtonPreviousTrack").performClick()
        composeTestRule.onNodeWithTag("TrackInfoName").assertTextEquals("Test 1")
    }


}