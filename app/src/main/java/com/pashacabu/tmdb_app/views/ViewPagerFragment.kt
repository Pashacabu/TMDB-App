package com.pashacabu.tmdb_app.views

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.view_model.*
import com.pashacabu.tmdb_app.views.adapters.ScreenSlideAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewPagerFragment : Fragment() {

    private var viewPager: ViewPager2? = null
    lateinit var tabLayout: TabLayout
    lateinit var tabs: Array<String>
    lateinit var searchButton: ImageView
    lateinit var vpAdapter: ScreenSlideAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exitTransition = MaterialElevationScale(false).apply {
            duration = requireContext().resources.getInteger(R.integer.transition_duration).toLong()
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


        searchButton.setOnClickListener {
            val fragment = SearchFragment.newInstance(searchButton.transitionName)
            fragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
                duration = resources.getInteger(R.integer.transition_duration).toLong()
                scrimColor = Color.TRANSPARENT
            }
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(VIEWPAGER)
                ?.addSharedElement(searchButton, searchButton.transitionName)
                ?.commit()
        }
        vpAdapter = ScreenSlideAdapter(this)
        viewPager?.adapter = vpAdapter
        TabLayoutMediator(tabLayout, viewPager as ViewPager2) { tab, position ->
            tab.text = tabs[position]
        }.attach()
        viewPager?.setCurrentItem(1, false)

    }
    companion object{
        const val VIEWPAGER="ViewPager"
    }
}
