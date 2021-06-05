package com.pashcabu.hw2.views

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.pashcabu.hw2.R
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.view_model.*
import com.pashcabu.hw2.views.adapters.ScreenSlideAdapter

class ViewPagerFragment : Fragment() {

    var viewPager: ViewPager2? = null
    lateinit var tabLayout: TabLayout
    lateinit var tabs: Array<String>
    lateinit var searchButton: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exitTransition = MaterialElevationScale(false).apply {
            duration = 500
        }
        return inflater.inflate(R.layout.new_fragment_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        tabLayout = view.findViewById(R.id.tab_layout)
        tabs = resources.getStringArray(R.array.tabs)
        viewPager = view.findViewById(R.id.movies_list_viewpager)
        searchButton = view.findViewById(R.id.searchButton)
        searchButton.transitionName = SEARCH
        val fragment = SearchFragment.newInstance(searchButton.transitionName)

        fragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 500
            scrimColor = Color.TRANSPARENT
        }
        searchButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(SEARCH)
                ?.addSharedElement(searchButton, searchButton.transitionName)
                ?.commit()
        }
        val vPAdapter = ScreenSlideAdapter(this)
        viewPager?.adapter = vPAdapter
        TabLayoutMediator(tabLayout, viewPager as ViewPager2) { tab, position ->
            tab.text = tabs[position]
        }.attach()
        viewPager?.setCurrentItem(1, false)

    }
}
