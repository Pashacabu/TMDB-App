package com.pashcabu.hw2.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialElevationScale
import com.pashcabu.hw2.R

class ViewPagerFragment : Fragment() {

    var viewPager : ViewPager2?=null
    lateinit var tabLayout : TabLayout
    lateinit var tabs : Array<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exitTransition = MaterialElevationScale(false).apply {
            duration = 500
        }
        return inflater.inflate(R.layout.fragment_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tab_layout)
        tabs = resources.getStringArray(R.array.tabs)
        viewPager=view.findViewById(R.id.movies_list_viewpager)
        val vPAdapter = MoviesListFragment.ScreenSlide(this)
        viewPager?.adapter = vPAdapter
        TabLayoutMediator(tabLayout, viewPager as ViewPager2) {tab, position ->
            tab.text = tabs[position+1]
        }.attach()
    }

}