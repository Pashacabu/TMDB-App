package com.pashcabu.hw2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity(), MoviesList.MovieClickListener, MovieDetails.MovieDetailsClickListener{
    var fragmentContainer :FrameLayout?=null
    var fragmentMovieDetails=MovieDetails()
    var fragmentMoviesList = MoviesList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentContainer=findViewById(R.id.fragment_container)
        if (savedInstanceState==null){
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragmentMoviesList, "MoviesList")
                    .commit()
        } else {
            fragmentMoviesList=supportFragmentManager.findFragmentByTag("MoviesList") as MoviesList
        }
    }

    override fun openMovieDetails() {
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragmentMovieDetails, "MovieDetails")
                .addToBackStack("Movie Details")
                .commit()
    }

    override fun onBackArrowPressed() {
        super.onBackPressed()
    }

}