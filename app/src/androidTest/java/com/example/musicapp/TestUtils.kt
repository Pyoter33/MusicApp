package com.example.musicapp

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}


object TestUtils {
    fun recyclerViewAtPositionCheckBackgroundDrawable(
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

    fun checkImageButtonDrawable(
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

}