package com.pashcabu.hw2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity(), MoviesList.MovieClickListener, MovieDetails.MovieDetailsClickListener{
    var fragmentMovieDetails=MovieDetails()
    var fragmentMoviesList = MoviesList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState==null){
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragmentMoviesList, LIST_FRAGMENT_TAG)
                    .commit()
        } else {
            fragmentMoviesList=supportFragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG) as MoviesList
        }
    }

    override fun openMovieDetails() {
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragmentMovieDetails, DETAILS_FRAGMENT_TAG)
                .addToBackStack(DETAILS_FRAGMENT_TAG)
                .commit()
    }

    override fun onBackArrowPressed() {
        super.onBackPressed()
    }
    companion object {
        private const val LIST_FRAGMENT_TAG = "MoviesList"
        private const val DETAILS_FRAGMENT_TAG = "MovieDetails"
    }
}