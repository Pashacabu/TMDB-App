package com.pashacabu.tmdb_app.views.myNestedScrollView

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class PersonFragmentNestedScrollView(context: Context, attributeSet: AttributeSet, defStyle: Int) :
    MyNestedScrollView(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
//        if (dy > 0 && !viewCanScrollUp(target) || dy < 0 && viewCanScrollDown(this) ) {  - can't scroll target down in this case, fling only
////            scrollBy(0, dy)
////            consumed[1] = dy
//            return
//        }
        if(dy>0 && viewCanScrollDown(this)|| dy>0 && !viewCanScrollUp(this)){
            scrollBy(0, dy)
            consumed[1]= dy
            return
        }
    }


    private fun viewCanScrollDown(view: View): Boolean{
//        return when (view) {
//            is SwipeRefreshLayout -> {
//                true
//            }
//            else -> {
//                view.canScrollVertically(1)
//            }
//        }
        return view.canScrollVertically(1)
    }

    private fun viewCanScrollUp(view: View): Boolean {
        return view.canScrollVertically(-1)
    }
}