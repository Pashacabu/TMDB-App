package com.pashcabu.hw2.views.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pashcabu.hw2.views.MovieDetailsFragment
import com.pashcabu.hw2.views.MoviesListFragment

class ScreenSlideAdapter(fr: Fragment) : FragmentStateAdapter(fr) {

    override fun getItemCount(): Int {
        return 6
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MovieDetailsFragment()
            1 -> MoviesListFragment.newInstance(NOW_PLAYING)
            2 -> MoviesListFragment.newInstance(POPULAR)
            3 -> MoviesListFragment.newInstance(TOP_RATED)
            4 -> MoviesListFragment.newInstance(UPCOMING)
            else -> MoviesListFragment.newInstance(FAVOURITE)
        }
    }


    companion object {
        const val NOW_PLAYING = "now_playing"
        const val POPULAR = "popular"
        const val TOP_RATED = "top_rated"
        const val UPCOMING = "upcoming"
        const val ENDPOINT = "EndPoint"
        const val FAVOURITE = "favourite"

    }
}