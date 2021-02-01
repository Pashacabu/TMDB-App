package com.pashcabu.hw2.views

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pashcabu.hw2.recyclerAdapters.*

class MoviesListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    private var openMovieListener: MoviesListClickListener = object : MoviesListClickListener {
        override fun onMovieSelected(movieID: Int, title: String) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.add(R.id.fragment_container, MovieDetailsFragment.newInstance("", movieID))
                ?.addToBackStack(title)?.commit()
        }
    }

    private var adapter = NewMoviesListAdapter(openMovieListener)
    private var moviesListRecyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var refreshButton: Button? = null
    private val viewModel: MyViewModel by viewModels()
    private var endpoint: String? = null
    private var currentPage: Int = 1
    private var totalPages: Int = 0

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
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        swipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
        refreshButton = view.findViewById(R.id.refresh_button)
        refreshButton?.setOnClickListener {
            refreshData()
        }
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

    private fun loadData(endpoint: String?) {
        viewModel.loadLiveData(endpoint)
    }

    private fun refreshData() {
        currentPage = 1
        viewModel.refreshMovieList(endpoint)
    }

    private fun getEndpoint() {
        endpoint = arguments?.getString(ENDPOINT)
    }

    private fun subscribeLiveData() {
        val stateObserver = Observer<Boolean> {
            swipeRefreshLayout?.isRefreshing = it
            refreshButton?.isVisible = it
        }
        val listObserver = Observer<List<ResultsItem?>> {
            it?.let { it1 -> adapter.loadMovies(it1) }
        }
        val pagesObserver = Observer<Int> {
            totalPages = it
        }
        viewModel.loadingState.observe(this.viewLifecycleOwner, stateObserver)
        viewModel.movieList.observe(this.viewLifecycleOwner, listObserver)
        viewModel.amountOfPages.observe(this.viewLifecycleOwner, pagesObserver)
    }

    private fun addLoadMoreListener() {
        val listener = MyScrollListener {
            currentPage += 1
            if (totalPages != 0) {
                if (currentPage <= totalPages) {
                    viewModel.loadMoreMovies(endpoint, currentPage)
                } else Toast.makeText(this.context, "No more pages to load!", Toast.LENGTH_SHORT)
                    .show()
            } else Toast.makeText(this.context, "No pages to load!", Toast.LENGTH_SHORT)
                .show()
        }
        moviesListRecyclerView?.addOnScrollListener(listener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getEndpoint()
        findViews(view)
        loadData(endpoint)
        setUpAdapter(view)
        subscribeLiveData()
        addLoadMoreListener()
    }

    override fun onRefresh() {
        swipeRefreshLayout?.isRefreshing = true
        refreshData()
        swipeRefreshLayout?.isRefreshing = false
    }


    class ScreenSlide(fr: Fragment) : FragmentStateAdapter(fr) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
//                0 -> MovieDetailsFragment.newInstance("latest", 0)
                0 -> newInstance(NOW_PLAYING)
                1 -> newInstance(POPULAR)
                2 -> newInstance(TOP_RATED)
                else -> newInstance(UPCOMING)

            }
        }
    }

    companion object {
        fun newInstance(endpoint: String): MoviesListFragment {
            val arguments = Bundle()
            arguments.putString(ENDPOINT, endpoint)
            val fragment = MoviesListFragment()
            fragment.arguments = arguments
            return fragment
        }

        private const val NOW_PLAYING = "now_playing"
        private const val POPULAR = "popular"
        private const val TOP_RATED = "top_rated"
        private const val UPCOMING = "upcoming"
        private const val ENDPOINT = "EndPoint"

    }
}




