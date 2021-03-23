package com.pashcabu.hw2.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.work.WorkManager
import com.pashcabu.hw2.R
import com.pashcabu.hw2.model.ConnectionChecker
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
import com.pashcabu.hw2.view_model.*
import com.pashcabu.hw2.views.adapters.MoviesListClickListener
import com.pashcabu.hw2.views.adapters.MyScrollListener
import com.pashcabu.hw2.views.adapters.NewMoviesListAdapter


class MoviesListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    private var openMovieListener: MoviesListClickListener = object : MoviesListClickListener {
        override fun onMovieSelected(movieID: Int, title: String) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.add(R.id.fragment_container, MovieDetailsFragment.newInstance(movieID))
                ?.addToBackStack(title)
                ?.commit()
        }

        override fun onMovieLiked(movie: Movie) {
            viewModel.onLikedButtonPressed(endpoint, movie)
        }
    }

    private var adapter = NewMoviesListAdapter(openMovieListener)
    private lateinit var moviesListRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var offlineWarning: TextView
    private lateinit var roomDB: Database
    private lateinit var viewModel: MoviesListViewModel
    private lateinit var connectionViewModel: ConnectionViewModel
    private var endpoint: String? = null
    private var currentPage: Int = 1
    private var totalPages: Int = 0
    private var toast: Toast? = null
    private var connectionChecker: ConnectionChecker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.movies_list_fragment, container, false)
    }

    private fun findViews(view: View) {
        moviesListRecyclerView = view.findViewById(R.id.movies_list_recycler_view)
        moviesListRecyclerView.setHasFixedSize(true)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
        offlineWarning = view.findViewById(R.id.offline_warning)

    }

    private fun setUpAdapter(view: View) {
        val orientation = view.context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 2)
            moviesListRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 7))
        } else {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 3)
            moviesListRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 14))
        }
        moviesListRecyclerView.adapter = adapter
    }

    private fun loadData(endpoint: String?) {
        viewModel.loadLiveData(endpoint, currentPage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomDB = Database.createDB(requireContext())
        val worker = WorkManager.getInstance(requireContext())
        val factory = MyViewModelFactory.MoviesListViewModelFactory(roomDB, worker)
        val factoryConnection = MyViewModelFactory.ConnectionViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(MoviesListViewModel::class.java)
        connectionViewModel =
            ViewModelProvider(this, factoryConnection).get(ConnectionViewModel::class.java)
        connectionChecker = ConnectionChecker(requireContext())
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
            swipeRefreshLayout.isRefreshing = it
        }
        val listObserver = Observer<List<Movie?>> {
            it?.let { it1 ->
                adapter.loadMovies(it1)
            }
        }
        val pagesObserver = Observer<Int> {
            totalPages = it
        }
        val errorsObserver = Observer<String> {
            if (it != NO_ERROR) {
                if (toast == null) {
                    toast = Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT)
                    toast?.show()
                } else {
                    toast?.cancel()
                    toast = Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT)
                    toast?.show()
                }
            }
        }
        val connectionObserver = Observer<Boolean> {
            viewModel.setConnectionState(it)
            if (it) {
                offlineWarning.visibility = View.GONE
            } else {
                offlineWarning.visibility = View.VISIBLE
            }
        }
        viewModel.loadingState.observe(this.viewLifecycleOwner, stateObserver)
        viewModel.movieList.observe(this.viewLifecycleOwner, listObserver)
        viewModel.amountOfPages.observe(this.viewLifecycleOwner, pagesObserver)
        viewModel.errorState.observe(this.viewLifecycleOwner, errorsObserver)
        viewModel.pageStepBack.observe(this.viewLifecycleOwner, {
            currentPage += it
        })
        connectionChecker?.observe(this.viewLifecycleOwner, {
            connectionViewModel.setConnectionState(it)
        })
        connectionViewModel.connectionState.observe(this.viewLifecycleOwner, connectionObserver)
    }

    private fun addLoadMoreListener() {
        val listener = MyScrollListener {
            currentPage += 1
            if (totalPages != 0 && offlineWarning.visibility == View.GONE) {
                if (currentPage <= totalPages) {
                    viewModel.loadMore(endpoint, currentPage)
                } else {
                    Toast.makeText(this.context, "No more pages to load!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (offlineWarning.visibility == View.VISIBLE) {
                Toast.makeText(this.context, "No connection!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        if (endpoint != FAVOURITE) {
            moviesListRecyclerView.addOnScrollListener(listener)
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
        swipeRefreshLayout.isRefreshing = true
        refreshData()
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onPause() {
        super.onPause()
        toast?.cancel()
    }

    override fun onStop() {
        super.onStop()
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




