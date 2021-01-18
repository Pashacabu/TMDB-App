package com.pashcabu.hw2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity(), MovieDetailsFragment.MovieDetailsClickListener {

    var fragmentMoviesList = ViewPagerFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragmentMoviesList, LIST_FRAGMENT_TAG)
                .commit()
        } else {
            fragmentMoviesList =
                supportFragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG) as ViewPagerFragment
        }



//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .add(R.id.fragment_container, fragmentMoviesList, LIST_FRAGMENT_TAG)
//                .commit()
//        } else {
//            fragmentMoviesList =
//                supportFragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG) as MoviesListFragment
//        }
    }


    companion object {
        private const val LIST_FRAGMENT_TAG = "MoviesList"
    }

    override fun onBackArrowPressed() {
        super.onBackPressed()
    }


}