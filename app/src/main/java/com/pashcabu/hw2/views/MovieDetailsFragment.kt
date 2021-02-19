package com.pashcabu.hw2.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.pashcabu.hw2.*
import com.pashcabu.hw2.model.ConnectionChecker
import com.pashcabu.hw2.model.data_classes.networkResponses.CastResponse
import com.pashcabu.hw2.model.data_classes.networkResponses.CastItem
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.MovieDetailsResponse
import com.pashcabu.hw2.view_model.ConnectionViewModel
import com.pashcabu.hw2.view_model.MovieDetailsViewModel
import com.pashcabu.hw2.views.adapters.MovieDetailsActorsClickListener
import com.pashcabu.hw2.views.adapters.MovieDetailsAdapter
import com.pashcabu.hw2.view_model.MyViewModelFactory
import com.pashcabu.hw2.view_model.NO_ERROR


class MovieDetailsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    private lateinit var poster: ImageView
    private lateinit var pgRating: TextView
    private lateinit var title: TextView
    private lateinit var tags: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var reviews: TextView
    private lateinit var story: TextView
    private lateinit var castTitle: TextView
    private lateinit var actorsRecyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var offlineWarning: TextView
    private lateinit var connectionViewModel: ConnectionViewModel
    private var toast: Toast? = null
    private var backArrow: ImageView? = null
    private var backButton: TextView? = null
    private var movieDetailsClickListener: MovieDetailsClickListener? = null
    private var movieDetailsActorsClickListener = object : MovieDetailsActorsClickListener {
        override fun onActorSelected(actor: CastItem?) {
            if (toast == null) {
                toast = Toast.makeText(context, actor?.actorName, Toast.LENGTH_SHORT)
                toast?.show()
            } else {
                toast?.cancel()
                toast = Toast.makeText(context, actor?.actorName, Toast.LENGTH_SHORT)
                toast?.show()
            }
        }
    }
    private val adapter = MovieDetailsAdapter(movieDetailsActorsClickListener)
    private var movieID = 0
    private lateinit var viewModel: MovieDetailsViewModel
    private lateinit var roomDB: Database
    private var connectionChecker: ConnectionChecker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomDB = Database.createDB(requireContext())
        val factory = MyViewModelFactory.MoviesDetailsViewModelFactory(roomDB)
        val factoryConnection = MyViewModelFactory.ConnectionViewModelfactory()
        viewModel = ViewModelProvider(this, factory).get(MovieDetailsViewModel::class.java)
        connectionViewModel = ViewModelProvider(this, factoryConnection).get(ConnectionViewModel::class.java)
        connectionChecker = ConnectionChecker(requireContext())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.movie_details_fragment, container, false)
    }

    override fun onPause() {
        super.onPause()
        toast?.cancel()
    }

    private val connectionObserver = Observer<Boolean> {
        viewModel.setConnectionState(it)
        if (it) {
            offlineWarning.visibility = View.GONE
        } else {
            offlineWarning.visibility = View.VISIBLE
        }
    }
    private val errorsObserver = Observer<String> {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        movieID = arguments?.getInt(TITLE) ?: 0
        loadMovieDetailsData()
        viewModel.loadingState.observe(this.viewLifecycleOwner, {
            showLoadingIndicator(it)
        })
        viewModel.movieDetailsData.observe(this.viewLifecycleOwner, {
            updateMovieData(it)
        })
        viewModel.castData.observe(this.viewLifecycleOwner, {
            updateActorsData(it)
        })
        viewModel.errorState.observe(this.viewLifecycleOwner, errorsObserver)
        connectionChecker?.observe(this.viewLifecycleOwner, {
            connectionViewModel.setConnectionState(it)
        })
        connectionViewModel.connectionState.observe(this.viewLifecycleOwner, connectionObserver)
    }

    private fun showLoadingIndicator(loadingState: Boolean) {
        swipeRefresh.isRefreshing = loadingState
    }

    private fun loadMovieDetailsData() {
        viewModel.loadData(movieID)
    }

    private fun refreshMovieDetailData() {
        viewModel.refreshMovieDetailsData(movieID)
    }

    private fun updateMovieData(movie: MovieDetailsResponse) {
        context?.let {
            Glide.with(it)
                    .load(imageBigBaseUrl + movie.backdropPath)
                    .placeholder(R.drawable.poster_big_placeholder)
                    .into(poster)
        }
        if (movie.adult == true) {
            pgRating.text = context?.resources?.getString(R.string.pg, 16)
        } else {
            pgRating.text = context?.resources?.getString(R.string.pg, 13)
        }
        title.text = movie.movieTitle
        val tagsList: List<String?> = movie.genres?.map { it?.genreName } ?: listOf()
        tags.text = android.text.TextUtils.join(", ", tagsList)
        ratingBar.rating = (movie.voteAverage?.div(2))?.toFloat() ?: 0f
        reviews.text = context?.getString(R.string.reviews, movie.reviews)
        story.text = movie.overview
    }

    private fun updateActorsData(actors: CastResponse) {
        if (actors.castList.isNullOrEmpty()) {
            castTitle.text = getString(R.string.no_actors_data)
        } else {
            castTitle.text = getString(R.string.cast)
        }
        actorsRecyclerView.adapter = adapter
        adapter.loadActorsData(actors)
    }

    private fun findViews(view: View) {
        swipeRefresh = view.findViewById(R.id.details_swipe_refresh)
        swipeRefresh.setOnRefreshListener(this)
        poster = view.findViewById(R.id.mainPoster)
        pgRating = view.findViewById(R.id.pgRating)
        title = view.findViewById(R.id.movie_title)
        tags = view.findViewById(R.id.tag_line)
        ratingBar = view.findViewById(R.id.rating)
        reviews = view.findViewById(R.id.reviews)
        story = view.findViewById(R.id.storylineDescription)
        castTitle = view.findViewById(R.id.cast_title)
        offlineWarning = view.findViewById(R.id.offline_warning)
        actorsRecyclerView = view.findViewById(R.id.actors_recycler_view)
        backArrow = view.findViewById<ImageView?>(R.id.backArrow)?.apply {
            setOnClickListener { movieDetailsClickListener?.onBackArrowPressed() }
        }
        backButton = view.findViewById<TextView?>(R.id.backButton)?.apply {
            setOnClickListener { movieDetailsClickListener?.onBackArrowPressed() }
        }
        actorsRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        actorsRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 5))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MovieDetailsClickListener) {
            movieDetailsClickListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        movieDetailsClickListener = null
    }

    interface MovieDetailsClickListener {
        fun onBackArrowPressed()
    }

    companion object {
        fun newInstance(endpoint: String, movieID: Int): MovieDetailsFragment {
            val arg = Bundle()
            arg.putInt(TITLE, movieID)
            arg.putString(ENDPOINT, endpoint)
            val fragment = MovieDetailsFragment()
            fragment.arguments = arg
            return fragment
        }

        const val TITLE = "movieTitle"
        private const val ENDPOINT = "endPoint"
        private const val imageBigBaseUrl = "https://image.tmdb.org/t/p/w780"
    }

    override fun onRefresh() {
        refreshMovieDetailData()

    }

}

