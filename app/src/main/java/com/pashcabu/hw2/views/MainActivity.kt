package com.pashcabu.hw2.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pashcabu.hw2.R


class MainActivity : AppCompatActivity(), MovieDetailsFragment.GoBackClickListener,
    SearchFragment.GoBack {


    private var fragmentMoviesList = ViewPagerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragmentMoviesList, LIST_FRAGMENT_TAG)
                .commit()
            handleIntent(intent)
        } else {
            fragmentMoviesList =
                supportFragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG) as ViewPagerFragment
        }


    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val id = intent.data?.lastPathSegment?.toIntOrNull()
                if (id != null) {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, MovieDetailsFragment.newInstance(id))
                        .addToBackStack("Details")
                        .commit()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleIntent(intent)
        }
    }


    companion object {
        private const val LIST_FRAGMENT_TAG = "MoviesList"
    }

    override fun onBackArrowPressed() {
        super.onBackPressed()
    }


}