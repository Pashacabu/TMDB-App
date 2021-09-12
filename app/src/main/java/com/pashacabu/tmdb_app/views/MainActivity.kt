package com.pashacabu.tmdb_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.views.utils.GoBackInterface
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), GoBackInterface {

    private var viewPagerFragment = ViewPagerFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, viewPagerFragment, VIEW_PAGER_TAG)
                .commit()
            handleIntent(intent)
        } else {
            viewPagerFragment =
                supportFragmentManager.findFragmentByTag(VIEW_PAGER_TAG) as ViewPagerFragment
        }

    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val id = intent.data?.lastPathSegment?.toIntOrNull()
                if (id != null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MovieDetailsFragment.newInstance(id))
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
        const val VIEW_PAGER_TAG = "ViewPager"
    }

    override fun goBack() {
        super.onBackPressed()
    }


}