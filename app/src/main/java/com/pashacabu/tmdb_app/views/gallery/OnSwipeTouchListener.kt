package com.pashacabu.tmdb_app.views.gallery

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class OnSwipeTouchListener(context: Context, private val swipeInterface: SwipeInterface) :
    View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY: Float = e1?.y?.let { e2?.y?.minus(it) } ?: 0f
                val diffX: Float = e1?.x?.let { e2?.x?.minus(it) } ?: 0f
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_MIN_DIST && abs(velocityX) > SWIPE_MIN_VELOCITY) {
                        when {
                            diffX > 0 -> {
                                swipeInterface.onSwipeLeft()
                            }
                            diffX < 0 -> {
                                swipeInterface.onSwipeRight()
                            }
                            else -> {
                            }
                        }
                        result = true
                    }
                } else {
                    if (abs(diffY) > SWIPE_MIN_DIST && abs(velocityY) > SWIPE_MIN_VELOCITY) {
                        when {
                            diffY > 0 -> {
                                swipeInterface.onSwipeDown()
                            }
                            diffY < 0 -> {
                                swipeInterface.onSwipeUp()
                            }
                            else -> {
                            }
                        }
                        result = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }
    }

    companion object {
        const val SWIPE_MIN_DIST = 150
        const val SWIPE_MIN_VELOCITY = 100
    }
}

interface SwipeInterface {
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onSwipeDown()
    fun onSwipeUp()
}

