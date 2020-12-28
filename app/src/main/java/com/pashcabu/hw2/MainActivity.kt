package com.pashcabu.hw2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


class MainActivity : AppCompatActivity(), MovieDetailsFragment.MovieDetailsClickListener {

    var fragmentMoviesList = MoviesListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragmentMoviesList, LIST_FRAGMENT_TAG)
                .commit()
        } else {
            fragmentMoviesList =
                supportFragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG) as MoviesListFragment
        }
    }

    companion object {
        private const val LIST_FRAGMENT_TAG = "MoviesList"
    }

    override fun onBackArrowPressed() {
        super.onBackPressed()
    }


}