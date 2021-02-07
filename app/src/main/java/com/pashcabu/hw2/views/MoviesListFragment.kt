package com.pashcabu.hw2.views

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
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

        override fun onMovieLiked(movie: Movie) {
            viewModel.addToFavourite(endpoint, movie)
        }
    }

    private var adapter = NewMoviesListAdapter(openMovieListener)
    private var moviesListRecyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var offlineWarning: TextView? = null
    private lateinit var roomDB: Database
    private lateinit var viewModel: MoviesListViewModel
    private var endpoint: String? = null
    private var currentPage: Int = 1
    private var totalPages: Int = 0
    private var toast: Toast? = null

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
        offlineWarning = view.findViewById(R.id.offline_warning)
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
        viewModel.loadLiveData(endpoint, currentPage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomDB = Database.createDB(requireContext())
        val factory = MyViewModelFactory.MoviesListViewModelFactory(roomDB)
        viewModel = ViewModelProvider(this, factory).get(MoviesListViewModel::class.java)

    }

    override fun onResume() {
        super.onResume()
        if (endpoint == FAVOURITE) {
            viewModel.refreshMovieList(endpoint, currentPage)
        }
    }

    private fun refreshData() {
        currentPage = 1
        viewModel.refreshMovieList(endpoint, currentPage)
    }

    private fun getEndpoint() {
        endpoint = arguments?.getString(ENDPOINT)
    }

    private fun subscribeLiveData() {
        val stateObserver = Observer<Boolean> {
            swipeRefreshLayout?.isRefreshing = it
        }
        val listObserver = Observer<List<Movie?>> {
            it?.let { it1 ->
                adapter.loadMovies(it1)
//            viewModel.saveToDB(endpoint, it)
            }
        }
        val pagesObserver = Observer<Int> {
            totalPages = it
        }
        val errorsObserver = Observer<String> {
            if (it != NO_ERROR) {
                offlineWarning?.visibility = View.VISIBLE
                if (toast == null) {
                    toast = Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT)
                    toast?.show()
                } else {
                    toast?.cancel()
                    toast = Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT)
                    toast?.show()
                }
            } else offlineWarning?.visibility = View.GONE

        }
        viewModel.loadingState.observe(this.viewLifecycleOwner, stateObserver)
        viewModel.movieList.observe(this.viewLifecycleOwner, listObserver)
        viewModel.amountOfPages.observe(this.viewLifecycleOwner, pagesObserver)
        viewModel.errorState.observe(this.viewLifecycleOwner, errorsObserver)
    }

    private fun addLoadMoreListener() {
        val listener = MyScrollListener {
            currentPage += 1
            if (totalPages != 0) {
                if (currentPage <= totalPages) {
                    viewModel.loadMore(endpoint, currentPage)
                } else Toast.makeText(this.context, "No more pages to load!", Toast.LENGTH_SHORT)
                    .show()
            } else Toast.makeText(this.context, "No pages to load!", Toast.LENGTH_SHORT)
                .show()
        }
        if (endpoint != FAVOURITE) {
            moviesListRecyclerView?.addOnScrollListener(listener)
        }

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

    override fun onPause() {
        super.onPause()
        toast?.cancel()
    }


    class ScreenSlide(fr: Fragment) : FragmentStateAdapter(fr) {
        override fun getItemCount(): Int {
            return 5
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
//                0 -> MovieDetailsFragment.newInstance("latest", 0)
                0 -> newInstance(NOW_PLAYING)
                1 -> newInstance(POPULAR)
                2 -> newInstance(TOP_RATED)
                3 -> newInstance(UPCOMING)
                else -> newInstance(FAVOURITE)


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

        const val NOW_PLAYING = "now_playing"
        const val POPULAR = "popular"
        const val TOP_RATED = "top_rated"
        const val UPCOMING = "upcoming"
        const val ENDPOINT = "EndPoint"
        const val FAVOURITE = "favourite"

    }
}




