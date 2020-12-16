package com.pashcabu.hw2

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.pashcabu.hw2.moviesListRecyclerView.Movie


class MainActivity : AppCompatActivity(), MovieDetails.MovieDetailsClickListener{

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

    companion object {
        private const val LIST_FRAGMENT_TAG = "MoviesList"
    }



    override fun onBackArrowPressed() {
        super.onBackPressed()
    }


}