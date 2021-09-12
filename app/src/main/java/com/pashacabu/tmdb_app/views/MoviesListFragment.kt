package com.pashacabu.tmdb_app.views

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.transition.MaterialContainerTransform
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.model.ConnectionChecker
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.Movie
import com.pashacabu.tmdb_app.view_model.*
import com.pashacabu.tmdb_app.views.adapters.*
import com.pashacabu.tmdb_app.views.utils.Decorator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MoviesListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var movieListener: MoviesListAdapterInterface = object : MoviesListAdapterInterface {
        override fun onMovieSelected(id: Int, title: String, view: View) {
            var detailsFragment = activity?.supportFragmentManager?.findFragmentByTag("details$id")
            if (detailsFragment == null) {
                detailsFragment = MovieDetailsFragment.newInstance(id)
                val bundle = detailsFragment.arguments ?: Bundle()
                bundle.putString("transition_name", view.transitionName)
                detailsFragment.arguments = bundle
                detailsFragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
                    duration =
                        requireContext().resources.getInteger(R.integer.transition_duration)
                            .toLong()
                    scrimColor = Color.TRANSPARENT
                }
                detailsFragment.sharedElementReturnTransition = MaterialContainerTransform().apply {
                    duration =
                        requireContext().resources.getInteger(R.integer.transition_duration)
                            .toLong()
                    scrimColor = Color.TRANSPARENT
                }
            }
            val tag = when (endpoint) {
                PersonFragment.LIST -> PersonFragment.LIST
                else -> MOVIESLIST
            }
            activity?.supportFragmentManager?.beginTransaction()
                ?.setReorderingAllowed(true)
                ?.replace(
                    R.id.fragment_container,
                    detailsFragment,
                    "details$id"
                )
                ?.addToBackStack(tag)
                ?.addSharedElement(view, view.transitionName)
                ?.commit()

        }

        override fun onMovieLiked(movie: Movie) {
            viewModel.onLikedButtonPressed(endpoint, movie)
        }

    }

    private val adapter = MoviesListAdapter(movieListener)
    private lateinit var moviesListRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var offlineWarning: TextView
    private val viewModel : MoviesListViewModel by viewModels()
    private var endpoint: String? = null
    private var currentPage: Int = 1
    private var totalPages: Int = 0
    private var toast: Toast? = null
    @Inject
    lateinit var connectionChecker: ConnectionChecker
    @Inject
    lateinit var decorator: Decorator
    private var recyclerOnGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postponeEnterTransition()
        view?.doOnPreDraw { startPostponedEnterTransition() }
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
        val width = view.context.resources.displayMetrics.widthPixels
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 2)
            moviesListRecyclerView.addItemDecoration(decorator.itemSpacing((width * 0.1 / 6).toInt()))
            moviesListRecyclerView.setPadding((width * 0.1 / 6).toInt())
        } else {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 4)
            moviesListRecyclerView.addItemDecoration(decorator.itemSpacing((width * 0.1 / 10).toInt()))
            moviesListRecyclerView.setPadding((width * 0.1 / 10).toInt())
        }
        moviesListRecyclerView.adapter = adapter
    }

    private fun loadData(endpoint: String?) {
        when (endpoint) {
            PersonFragment.LIST -> {
                val id = arguments?.getInt(PersonFragment.PERSON)
                viewModel.loadLiveData(endpoint, currentPage, id ?: 0)
            }
            else -> {
                viewModel.loadLiveData(endpoint, currentPage)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (endpoint == FAVOURITE) {
            viewModel.refreshMovieList(endpoint, currentPage)
        } else {
            viewModel.updateLoadedListBasedOnFavourite(endpoint)
        }
    }

    private fun refreshData() {
        currentPage = 1
        viewModel.refreshMovieList(endpoint, currentPage)
    }

    fun callToRefresh() {
        refreshData()
    }

    private fun getEndpoint() {
        endpoint = arguments?.getString(ENDPOINT)
    }

    private fun subscribeLiveData() {
        val stateObserver = Observer<Boolean> {
            swipeRefreshLayout.isRefreshing = it
        }
        val listObserver = Observer<List<Movie?>?> {
            adapter.submitList(it)
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
        viewModel.loadingState.observe(this.viewLifecycleOwner, stateObserver)
        viewModel.moviesList.observe(this.viewLifecycleOwner, listObserver)
        viewModel.amountOfPages.observe(this.viewLifecycleOwner, pagesObserver)
        viewModel.errorState.observe(this.viewLifecycleOwner, errorsObserver)
        viewModel.pageStep.observe(this.viewLifecycleOwner, {
            currentPage = it
        })
        connectionChecker.observe(this.viewLifecycleOwner, {
            connectionObserver(it)
        })
        viewModel.scrollUp.observe(this.viewLifecycleOwner, {
            if (it) {
                scrollUp()
            }
        })
    }

    private fun scrollUp() {
        if (recyclerOnGlobalLayoutListener == null) {
            recyclerOnGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                moviesListRecyclerView.layoutManager?.smoothScrollToPosition(
                    moviesListRecyclerView,
                    RecyclerView.State(),
                    0
                )
            }
            moviesListRecyclerView.viewTreeObserver.addOnGlobalLayoutListener {
                recyclerOnGlobalLayoutListener?.onGlobalLayout()
            }
        }
        viewModel.resetScrollUp()
        CoroutineScope(Dispatchers.Default).launch {
            delay(750)
            removeGlobalLayoutObserver()
        }
    }

    private fun removeGlobalLayoutObserver() {
        moviesListRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(
            recyclerOnGlobalLayoutListener
        )
        recyclerOnGlobalLayoutListener = null
    }

    private fun connectionObserver(state: Boolean) {
        viewModel.setConnectionState(state)
        when (state) {
            true -> offlineWarning.visibility = View.GONE
            else -> offlineWarning.visibility = View.VISIBLE
        }
    }

    private fun addLoadMoreListener() {
        val listener = BottomOfTheListListener {
            val pageToLoad = currentPage + 1
            if (totalPages != 0 && offlineWarning.visibility == View.GONE) {
                if (pageToLoad <= totalPages) {
                    viewModel.loadMore(endpoint, pageToLoad)
                } else {
                    Toast.makeText(this.context, requireContext().getString(R.string.no_more_to_load), Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (offlineWarning.visibility == View.VISIBLE) {
                Toast.makeText(this.context, requireContext().getString(R.string.no_connection), Toast.LENGTH_SHORT)
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
        subscribeLiveData()
        setUpAdapter(view)
        addLoadMoreListener()
        addAnimationScrollListener()

    }

    private fun addAnimationScrollListener() {
        moviesListRecyclerView.addOnScrollListener(adapter.AnimationScrollListener())
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

    companion object {
        fun newInstance(endpoint: String): MoviesListFragment {
            val arguments = Bundle()
            arguments.putString(ENDPOINT, endpoint)
            val fragment = MoviesListFragment()
            fragment.arguments = arguments
            return fragment
        }

        const val ENDPOINT = "EndPoint"
        const val FAVOURITE = "favourite"
        const val MOVIESLIST = "MoviesList"

    }
}




