package com.example.musicapp

import com.example.musicapp.views.fragments.TracksListFragment
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TrackListViewComposeTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun hasDrawable(@DrawableRes id: Int): SemanticsMatcher {
        return SemanticsMatcher.expectValue(SemanticsPropertyKey("DrawableResId"), id) //????
    }

    @Test
   fun shouldLayoutTrackControllerInvisible(){
       composeTestRule.onNodeWithTag("TrackController").assertDoesNotExist()
   }

    @Test
    fun shouldLayoutTrackControllerVisible() {
        composeTestRule.onNodeWithText("Test 1").performClick()
        composeTestRule.onNodeWithTag("TrackController").assertExists()
    }

    @Test
    fun shouldFirstTrackName() {
        composeTestRule.onNodeWithText("Test 1").performClick()
        composeTestRule.onNodeWithTag("TrackControllerName", true).assertTextEquals("Test 1")
    }

    @Test
    fun shouldNextTrackName() {
        composeTestRule.onNodeWithText("Test 1").performClick()
        composeTestRule.onNodeWithTag("ButtonNextTrack").performClick()
        composeTestRule.onNodeWithTag("TrackControllerName", true).assertTextEquals("Test 2")
    }

    @Test
    fun shouldPreviousTrackName() {
        composeTestRule.onNodeWithText("Test 2").performClick()
        composeTestRule.onNodeWithTag("ButtonPreviousTrack").performClick()
        composeTestRule.onNodeWithTag("TrackControllerName", true).assertTextEquals("Test 1")
    }


    @Test
    fun shouldResumePauseButtonResume() {
        composeTestRule.onNodeWithText("Test 1").performClick()
        composeTestRule.onRoot().printToLog("test")
        composeTestRule.onNode(hasDrawable(R.drawable.icon_pause)).assertIsDisplayed() //????
        //composeTestRule.onNode(hasContentDescription("AAAA")).assertIsDisplayed() //????
    }

}