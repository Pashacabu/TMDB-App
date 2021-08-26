package com.pashacabu.tmdb_app.views.myNestedScrollView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.*
import androidx.core.widget.NestedScrollView

open class MyNestedScrollView(context: Context, attrs: AttributeSet, defStyle: Int) :
    NestedScrollView(context, attrs, defStyle), NestedScrollingParent2 {

    private val parentHelper = NestedScrollingParentHelper(this)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)



    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        var res = false
        if (axes != 0 && ViewCompat.SCROLL_AXIS_VERTICAL != 0) {
            res = true
        }
        return res
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        val oldScroll = scrollY
        scrollBy(0, dyUnconsumed)
        val myConsumed = scrollY - oldScroll
        val myUnconsumed = dyConsumed - myConsumed
        dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            ViewCompat.TYPE_TOUCH
        )
    }

    override fun onStopNestedScroll(target: View) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
    }

    override fun getNestedScrollAxes(): Int {
        return parentHelper.nestedScrollAxes
    }


}