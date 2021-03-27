package com.pashcabu.hw2.views

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
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
import java.util.*


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
    private lateinit var watchLaterBtn: Button
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var movieWithDetails = MovieDetailsResponse()
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

    var isRationaleShown = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomDB = Database.createDB(requireContext())
        val factory = MyViewModelFactory.MoviesDetailsViewModelFactory(roomDB)
        val factoryConnection = MyViewModelFactory.ConnectionViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(MovieDetailsViewModel::class.java)
        connectionViewModel =
            ViewModelProvider(this, factoryConnection).get(ConnectionViewModel::class.java)
        connectionChecker = ConnectionChecker(requireContext())

//        sharedElementEnterTransition = MaterialContainerTransform().apply {
//            duration = 500
//            scrimColor = Color.TRANSPARENT
//        }
//        sharedElementReturnTransition = MaterialContainerTransform().apply {
//            duration = 50000
//        }
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
        val transName = arguments?.getString("transition_name")
        swipeRefresh.transitionName = transName
        movieID = arguments?.getInt(TITLE) ?: 0
        loadMovieDetailsData()

//        swipeRefresh.transitionName += movieID
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
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
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
        movieWithDetails = movie
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
        watchLaterBtn = view.findViewById(R.id.watchLaterButton)
        watchLaterBtn.setOnClickListener {
            checkForPermissionsAndSchedule()
        }
    }

    private fun checkForPermissionsAndSchedule() {
        activity?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.READ_CALENDAR)
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_CALENDAR)
                        == PackageManager.PERMISSION_GRANTED -> onPermissionGranted()
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR) ->
                    showPermissionExplanationDialog()
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR) ->
                    showPermissionExplanationDialog()
                isRationaleShown -> showPermissionDeniedDialog()
                else -> requestPermission()
            }
        }

    }

    private fun requestPermission() {
        context?.let {
            requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
            requestPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.this_will_not_work_without_permission)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + context?.packageName)
                    )
                )
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.ask_for_permission)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                isRationaleShown = true
                requestPermission()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun onPermissionGranted() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val startMillis: Long = Calendar.getInstance().run {
            set(year, month, dayOfMonth + 1, hour, minute)
            timeInMillis
        }
        val endMillis: Long = Calendar.getInstance().run {
            set(year, month, dayOfMonth, hour + 1, minute)
            timeInMillis
        }
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.TITLE, "Watch " + movieWithDetails.movieTitle)
        startActivity(intent)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                onPermissionNotGranted()
            }
        }
        if (context is MovieDetailsClickListener) {
            movieDetailsClickListener = context
        }
    }

    private fun onPermissionNotGranted() {
        Toast.makeText(requireContext(), R.string.cal_permission_not_granted, Toast.LENGTH_LONG)
            .show()
    }

    override fun onDetach() {
        super.onDetach()
        movieDetailsClickListener = null
        requestPermissionLauncher.unregister()
    }


    interface MovieDetailsClickListener {
        fun onBackArrowPressed()
    }


    companion object {
        fun newInstance(movieID: Int): MovieDetailsFragment {
            val arg = Bundle()
            arg.putInt(TITLE, movieID)
            val fragment = MovieDetailsFragment()
            fragment.arguments = arg
            return fragment
        }

        const val TITLE = "movieTitle"
        private const val imageBigBaseUrl = "https://image.tmdb.org/t/p/w780"
    }

    override fun onRefresh() {
        refreshMovieDetailData()

    }


}






