package com.pashacabu.tmdb_app.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class Decorator : RecyclerView.ItemDecoration() {
    var spacing = 0

    fun itemSpacing(space: Int): RecyclerView.ItemDecoration {
        this.spacing = space
        return this
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(spacing, spacing, spacing, spacing)
    }

}