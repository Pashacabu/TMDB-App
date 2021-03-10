package com.pashcabu.hw2.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pashcabu.hw2.R


class MainActivity : AppCompatActivity(), MovieDetailsFragment.MovieDetailsClickListener {

    private var fragmentMoviesList = ViewPagerFragment()

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


    }


    companion object {
        private const val LIST_FRAGMENT_TAG = "MoviesList"
    }

    override fun onBackArrowPressed() {
        super.onBackPressed()
    }


}