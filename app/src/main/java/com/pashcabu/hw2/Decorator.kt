package com.pashcabu.hw2

import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class Decorator : RecyclerView.ItemDecoration() {
    var spacing = 0

    fun itemSpacing (view: View, space: Int) : RecyclerView.ItemDecoration{
        this.spacing=space*view.context.resources.displayMetrics.densityDpi/DisplayMetrics.DENSITY_DEFAULT
        return this
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(spacing,spacing,spacing,spacing)
    }
}