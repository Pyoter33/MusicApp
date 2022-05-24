package com.example.musicapp

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicapp.adapters.TracksListAdapter
import com.example.musicapp.repository.Track
import com.example.musicapp.repository.TrackDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsNot.not
import org.junit.*



class TrackListViewTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private lateinit var db: TrackDatabase
    companion object {

    }

    @Before
    fun setDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, TrackDatabase::class.java).allowMainThreadQueries().build()
        val dao = db.trackDao

        runBlocking {
            dao.insertAll(
                listOf(
                    Track(0, "Title 1", "Artist 1", "", 2000),
                    Track(0, "Title 2", "Artist 2", "", 2000),
                    Track(0, "Title 3", "Artist 3", "", 2000)
                )
            )
        }
    }

    @After
    fun closeDatabase() {
        db.close()
    }


    private fun recyclerViewAtPositionCheckBackgroundDrawable(
        position: Int,
        targetViewId: Int,
        drawableId: Int,
        tintId: Int?
    ): BoundedMatcher<View?, RecyclerView> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has view id $targetViewId at position $position with drawable id $drawableId")
            }

            override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                val targetView = viewHolder!!.itemView.findViewById<View>(targetViewId)
                val resources: Resources = recyclerView.context.resources
                val expectedDrawable: Drawable = resources.getDrawable(drawableId)

                return areDrawablesIdentical(targetView.background, expectedDrawable, tintId)
            }
        }
    }

    private fun checkImageButtonDrawable(
        drawableId: Int,
        tintId: Int?
    ): TypeSafeMatcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("view with drawable id $drawableId")
            }

            override fun matchesSafely(view: View): Boolean {
                val resources: Resources = view.context.resources
                val expectedDrawable: Drawable = resources.getDrawable(drawableId)
                val imageButton = view as ImageButton

                return areDrawablesIdentical(imageButton.drawable, expectedDrawable, tintId)
            }
        }
    }


    private fun areDrawablesIdentical(drawableA: Drawable?, drawableB: Drawable?, usedTint: Int?): Boolean {
        if (drawableA == null && drawableB == null) {
            return true
        } else if (drawableA == null || drawableB == null) {
            return false
        }
        val stateA = drawableA.constantState
        val stateB = drawableB.constantState
        // If the constant state is identical, they are using the same drawable resource.
        // However, the opposite is not necessarily true.
        return (stateA != null && stateA == stateB
                || getBitmap(drawableA, usedTint).sameAs(getBitmap(drawableB, usedTint)))
    }


    private fun getBitmap(drawable: Drawable, usedTint: Int?): Bitmap {
        val result: Bitmap
        if (drawable is BitmapDrawable) {
            result = drawable.bitmap
        } else {
            var width = drawable.intrinsicWidth
            var height = drawable.intrinsicHeight
            if (width <= 0) {
                width = 1
            }
            if (height <= 0) {
                height = 1
            }
            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            usedTint?.let {
                drawable.setTint(it)
            }
            drawable.draw(canvas)
        }
        return result
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
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.layoutTrackController))
            .check(matches(isDisplayed()))
    }

    @Test
    fun shouldTrackItemBackgroundBase() {
        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track, null)))
    }

    @Test
    fun shouldTrackItemBackgroundPlaying() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }

    @Test
    fun shouldResumePauseButtonResume() { //error when running with other tests
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.layoutTrackController))
            .check(matches(isDisplayed()))

        onView(withId(R.id.resumePauseButton))
            .check(matches(checkImageButtonDrawable(R.drawable.icon_pause, R.color.track_playing_background)))
    }

    @Test
    fun shouldResumePauseButtonPause() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.resumePauseButton))
            .perform(click())

        onView(withId(R.id.resumePauseButton))
            .check(matches(checkImageButtonDrawable(R.drawable.icon_play_arrow, R.color.track_playing_background)))
    }

    @Test
    fun shouldItemBackgroundNextTrackChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.nextButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track, null)))

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(recyclerViewAtPositionCheckBackgroundDrawable(1, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }

    @Test
    fun shouldItemBackgroundPreviousTrackChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(1, click()))

        onView(withId(R.id.previousButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(recyclerViewAtPositionCheckBackgroundDrawable(1, R.id.itemTrackLayout, R.drawable.background_track, null)))

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }

    @Test
    fun shouldItemBackgroundPreviousTrackFirstNotChange() {
        onView(withId(R.id.viewRecyclerTracks))
            .perform(RecyclerViewActions.actionOnItemAtPosition<TracksListAdapter.TrackViewHolder>(0, click()))

        onView(withId(R.id.previousButton))
            .perform(click())

        onView(withId(R.id.viewRecyclerTracks))
            .check(matches(recyclerViewAtPositionCheckBackgroundDrawable(0, R.id.itemTrackLayout, R.drawable.background_track_playing, null)))
    }
}