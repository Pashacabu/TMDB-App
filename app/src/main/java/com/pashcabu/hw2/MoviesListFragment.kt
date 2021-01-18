package com.pashcabu.hw2

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pashcabu.hw2.recyclerAdapters.*

class MoviesListFragment : Fragment() {


    private var openMovieListener: MoviesListClickListener = object : MoviesListClickListener {
        override fun onMovieSelected(movieID: Int, title: String) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.add(R.id.fragment_container, MovieDetailsFragment.newInstance("", movieID))
                ?.addToBackStack(title)?.commit()
        }
    }
    private var adapter = NewMoviesListAdapter(openMovieListener)
    private var moviesListRecyclerView: RecyclerView? = null
    private var loadingIndicator: ProgressBar? = null
    private val viewModel: MyViewModel by viewModels()
    private var endpoint: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.movies_list_fragment, container, false)
    }

    private fun findViews(view: View) {
        moviesListRecyclerView = view.findViewById(R.id.movies_list_recycler_view)
        moviesListRecyclerView?.setHasFixedSize(true)
        loadingIndicator = view.findViewById(R.id.movies_list_recycler_loading_indicator)

    }

    private fun setUpAdapter(view: View) {
        val orientation = view.context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            moviesListRecyclerView?.layoutManager = GridLayoutManager(context, 2)
            moviesListRecyclerView?.addItemDecoration(Decorator().itemSpacing(view, 7))
        } else {
            moviesListRecyclerView?.layoutManager = GridLayoutManager(context, 3)
            moviesListRecyclerView?.addItemDecoration(Decorator().itemSpacing(view, 14))
        }
        moviesListRecyclerView?.adapter = adapter
    }

    fun notifyAdapter() {
        adapter.notifyDataSetChanged()
    }

    private fun loadData(endpoint: String?) {
        viewModel.loadLiveData(endpoint)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        endpoint = arguments?.getString("EndPoint")
        Log.d("Fragment", "endpoint is $endpoint")
        findViews(view)
        loadData(endpoint)
        setUpAdapter(view)
        val stateObserver = Observer<Boolean> { showLoadingIndicator(it) }
        val listObserver = Observer<List<ResultsItem?>> {
            adapter.loadMovies(it)
            Log.d("Fragment", "observing new data")
        }
        viewModel.loadingState.observe(this.viewLifecycleOwner, stateObserver)
        viewModel.movieList.observe(this.viewLifecycleOwner, listObserver)
        val listener = MyScrollListener()
        moviesListRecyclerView?.addOnScrollListener(listener)
    }


    class ScreenSlide(fr: Fragment) : FragmentStateAdapter(fr) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
//                0 -> MovieDetailsFragment.newInstance("latest", 0)
                0 -> newInstance("now_playing")
                1 -> newInstance("popular")
                2 -> newInstance("top_rated")
                else -> newInstance("upcoming")

            }
        }
    }


    private fun showLoadingIndicator(loadingState: Boolean) {
        Log.d("Fragment", "observing loading state change")
        loadingIndicator?.isVisible = loadingState
        loadingIndicator?.isInvisible = !loadingState
    }

    companion object {
        fun newInstance(endpoint: String): MoviesListFragment {
            val arguments = Bundle()
            arguments.putString("EndPoint", endpoint)
            val fragment = MoviesListFragment()
            fragment.arguments = arguments
            return fragment
        }
    }
}




