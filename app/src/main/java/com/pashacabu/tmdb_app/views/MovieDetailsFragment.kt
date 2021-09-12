package com.pashacabu.tmdb_app.views

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.MaterialSharedAxis.Y
import com.pashacabu.tmdb_app.*
import com.pashacabu.tmdb_app.model.ConnectionChecker
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CastResponse
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CrewItem
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.MovieDetailsResponse
import com.pashacabu.tmdb_app.view_model.MovieDetailsViewModel
import com.pashacabu.tmdb_app.views.adapters.MovieDetailsActorsClickListener
import com.pashacabu.tmdb_app.views.adapters.MovieDetailsCastAdapter
import com.pashacabu.tmdb_app.view_model.NO_ERROR
import com.pashacabu.tmdb_app.views.adapters.MovieDetailsCrewAdapter
import com.pashacabu.tmdb_app.views.adapters.MovieDetailsCrewClickListener
import com.pashacabu.tmdb_app.views.utils.Decorator
import com.pashacabu.tmdb_app.views.utils.GoBackInterface
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MovieDetailsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    private var hasConnection = false
    private lateinit var poster: ImageView
    private lateinit var pgRating: TextView
    private lateinit var title: TextView
    private lateinit var tags: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var reviews: TextView
    private lateinit var budget: TextView
    private lateinit var revenu: TextView
    private lateinit var release: TextView
    private lateinit var runtime: TextView
    private lateinit var story: TextView
    private lateinit var castTitle: TextView
    private lateinit var crewTitle: TextView
    private lateinit var actorsRecyclerView: RecyclerView
    private lateinit var crewRecyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var scrollView: ScrollView
    private lateinit var offlineWarning: TextView
    @Inject
    lateinit var connectionChecker: ConnectionChecker
    @Inject
    lateinit var decorator: Decorator
    private lateinit var watchLaterBtn: Button
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var likeBtn: ImageView
    private var movieWithDetails = MovieDetailsResponse()
    private var toast: Toast? = null
    private lateinit var backArrow: ImageView
    private lateinit var backButton: TextView
    private lateinit var backTouchZone: ImageView
    private lateinit var homeBtn: ImageView
    private var goBackClickListener: GoBackInterface? = null
    private var movieDetailsActorsClickListener = object : MovieDetailsActorsClickListener {
        override fun onActorSelected(personID: Int, view: View) {
            when (hasConnection) {
                true -> {
                    var personFragment =
                        requireActivity().supportFragmentManager.findFragmentByTag("Person$personID")
                    if (personFragment == null) {
                        personFragment = PersonFragment.newInstance(personID)
                        val bundle = personFragment.arguments ?: Bundle()
                        bundle.putString("transition_name", view.transitionName)
                        personFragment.arguments = bundle
                        personFragment.sharedElementEnterTransition =
                            MaterialContainerTransform().apply {
                                duration =
                                    requireContext().resources.getInteger(R.integer.transition_duration)
                                        .toLong()
                                scrimColor = Color.TRANSPARENT
                            }
                        personFragment.sharedElementReturnTransition =
                            MaterialContainerTransform().apply {
                                duration =
                                    requireContext().resources.getInteger(R.integer.transition_duration)
                                        .toLong()
                                scrimColor = Color.TRANSPARENT
                            }
                    }
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.setReorderingAllowed(true)
                        ?.replace(
                            R.id.fragment_container,
                            personFragment,
                            "Person$personID"
                        )
                        ?.addToBackStack(DETAILS)
                        ?.addSharedElement(view, view.transitionName)
                        ?.commit()
                }
                else -> {
                    toast = Toast.makeText(requireContext(), requireContext().getString(R.string.no_connection), Toast.LENGTH_SHORT)
                    toast?.show()
                }
            }

        }
    }
    private val actorsAdapter = MovieDetailsCastAdapter(movieDetailsActorsClickListener)
    private val crewClickListener = object : MovieDetailsCrewClickListener {
        override fun onCrewPersonSelected(person: CrewItem?, view: View) {
            person?.id?.let { movieDetailsActorsClickListener.onActorSelected(it, view) }
        }
    }
    private val crewAdapter = MovieDetailsCrewAdapter(crewClickListener)
    private var movieID = 0
    private val viewModel: MovieDetailsViewModel by viewModels()
    private var isRationaleShown = false

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("Details")
            if (fragment != null && fragment.isVisible) {
                outState.putInt("scroll", scrollView.scrollY)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun connectionObserver(state: Boolean) {
        hasConnection = state
        viewModel.setConnectionState(state)
        when (state) {
            true -> offlineWarning.visibility = View.GONE
            else -> offlineWarning.visibility = View.VISIBLE
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
        postponeEnterTransition()
        findViews(view)
        scrollView.scrollY = savedInstanceState?.getInt("scroll") ?: 0
        setupAdapter()
        movieID = arguments?.getInt(TITLE) ?: 0
        swipeRefresh.transitionName = context?.getString(R.string.shared_details, movieID)
        loadMovieDetailsData()
        viewModel.loadingState.observe(this.viewLifecycleOwner, {
            showLoadingIndicator(it)
        })
        viewModel.movieDetailsData.observe(this.viewLifecycleOwner, {
            updateMovieData(it)
        })
        viewModel.castData.observe(this.viewLifecycleOwner, {
            updatePeopleData(it)
        })
        connectionChecker.observe(this.viewLifecycleOwner, {
            connectionObserver(it)
        })
        viewModel.errorState.observe(this.viewLifecycleOwner, errorsObserver)
        when (movieID) {
            0 -> {
                backArrow.visibility = View.GONE
                backButton.visibility = View.GONE
                homeBtn.visibility = View.GONE
            }
            else -> {
                backArrow.visibility = View.VISIBLE
                backButton.visibility = View.VISIBLE
                homeBtn.visibility = View.VISIBLE
            }
        }
        view.doOnPreDraw { startPostponedEnterTransition() }
        exitTransition = MaterialElevationScale(false).apply {
            duration = requireContext().resources.getInteger(R.integer.transition_duration).toLong()
        }

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
                .apply {
                    RequestOptions().dontTransform()
                }
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
        budget.text = requireContext().getString(R.string.budget, convertMoney(movie.budget))
        revenu.text = requireContext().getString(R.string.revenue, convertMoney(movie.revenue))
        release.text = requireContext().getString(R.string.release_date, when {
            movie.releaseDate.isNullOrEmpty() -> requireContext().getString(R.string.no_data)
            else -> movie.releaseDate
        } )
        runtime.text = requireContext().getString(R.string.runtime, convertRuntime(movie.runtime))
        story.text = movie.overview
        when (movie.liked) {
            true -> likeBtn.setImageResource(R.drawable.like_positive)
            else -> likeBtn.setImageResource(R.drawable.like)
        }
    }

    private fun convertRuntime(time : Int?) : String {
        var res = ""
        res = when (time) {
            0, null -> requireContext().getString(R.string.no_data)
            else -> {
                val m = time % 60
                val h = (time-m)/60
                "${h}h ${m}m"
            }
        }
        return res
    }

    private fun convertMoney(number: Long?): String {
        var res = ""
        if (number != null && number != 0L) {
            var str = number.toString()
            str = str.reversed()
            val list = str.chunked(3)
            list.reversed().forEach {
                res += it.reversed()
                res += " "
            }
            res.trim()
        } else {
            res = "No data"
        }
        return res
    }
    private fun convertMoney(number: Int?): String {
        var res = ""
        if (number != null && number != 0) {
            var str = number.toString()
            str = str.reversed()
            val list = str.chunked(3)
            list.reversed().forEach {
                res += it.reversed()
                res += " "
            }
            res.trim()
        } else {
            res = "No data"
        }
        return res
    }

    private fun updatePeopleData(cast: CastResponse) {
        if (cast.castList.isNullOrEmpty()) {
            castTitle.text = getString(R.string.no_actors_data)
        } else {
            castTitle.text = getString(R.string.cast)
        }
        if (cast.crew.isNullOrEmpty()) {
            crewTitle.text = getString(R.string.no_cast_data)
        } else {
            crewTitle.text = getString(R.string.crew)
        }
        actorsRecyclerView.adapter = actorsAdapter
        crewRecyclerView.adapter = crewAdapter
        actorsAdapter.loadActorsData(cast)
        crewAdapter.loadCrew(cast)
    }


    private fun findViews(view: View) {
        swipeRefresh = view.findViewById(R.id.details_swipe_refresh)
        swipeRefresh.setOnRefreshListener(this)
        scrollView = view.findViewById(R.id.details_scroll_view)
        poster = view.findViewById(R.id.mainPoster)
        pgRating = view.findViewById(R.id.pgRating)
        title = view.findViewById(R.id.movie_title)
        tags = view.findViewById(R.id.tag_line)
        ratingBar = view.findViewById(R.id.rating)
        reviews = view.findViewById(R.id.reviews)
        budget = view.findViewById(R.id.budget)
        revenu = view.findViewById(R.id.revenue)
        release = view.findViewById(R.id.release_date)
        runtime = view.findViewById(R.id.runtime)
        story = view.findViewById(R.id.storylineDescription)
        castTitle = view.findViewById(R.id.cast_title)
        crewTitle = view.findViewById(R.id.crew_title)
        offlineWarning = view.findViewById(R.id.offline_warning)
        actorsRecyclerView = view.findViewById(R.id.actors_recycler_view)
        crewRecyclerView = view.findViewById(R.id.crew_recycler_view)
        homeBtn = view.findViewById(R.id.home)
        homeBtn.transitionName = "home"
        homeBtn.setOnClickListener {
            val fragment =
                requireActivity().supportFragmentManager.findFragmentByTag(MainActivity.VIEW_PAGER_TAG)
            fragment?.enterTransition = MaterialSharedAxis(Y, true).apply {
                duration = requireContext().resources.getInteger(R.integer.transition_duration) * 2
                    .toLong()
            }
            this.exitTransition = MaterialSharedAxis(Y, true).apply {
                duration = requireContext().resources.getInteger(R.integer.transition_duration) * 2
                    .toLong()
            }
            requireActivity().supportFragmentManager.popBackStackImmediate(
                MoviesListFragment.MOVIESLIST,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        backArrow = view.findViewById<ImageView>(R.id.backArrow)
        backButton = view.findViewById<TextView>(R.id.backText)
        backTouchZone = view.findViewById<ImageView>(R.id.backTouch).apply {
            setOnClickListener { goBackClickListener?.goBack() }
        }
        watchLaterBtn = view.findViewById(R.id.watchLaterButton)
        watchLaterBtn.setOnClickListener {
            checkForPermissionsAndSchedule()
        }
        likeBtn = view.findViewById(R.id.like)
        likeBtn.setOnClickListener { onLikedListener() }
    }

    private fun onLikedListener() {
        viewModel.likeMovie()
    }

    private fun setupAdapter() {

        crewRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        actorsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val orientation = resources.configuration.orientation
        val width = resources.displayMetrics.widthPixels
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                actorsRecyclerView.addItemDecoration(decorator.itemSpacing((width * 0.1 / 6).toInt()))
                crewRecyclerView.addItemDecoration(decorator.itemSpacing((width * 0.1 / 6).toInt()))
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                actorsRecyclerView.addItemDecoration(decorator.itemSpacing((width * 0.1 / 12).toInt()))
                crewRecyclerView.addItemDecoration(decorator.itemSpacing((width * 0.1 / 12).toInt()))
            }
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
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
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
                requestPermission()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        isRationaleShown = true
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
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                var granted = false
                permissions.entries.forEach {
                    granted = when (it.value) {
                        false -> it.value
                        true -> it.value
                    }
                }
                when (granted) {
                    true -> onPermissionGranted()
                    false -> onPermissionNotGranted()
                }
            }
        if (context is GoBackInterface) {
            goBackClickListener = context
        }
    }

    private fun onPermissionNotGranted() {
        Toast.makeText(requireContext(), R.string.cal_permission_not_granted, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDetach() {
        super.onDetach()
        goBackClickListener = null
        requestPermissionLauncher.unregister()
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
        const val DETAILS = "Details"
    }

    override fun onRefresh() {
        refreshMovieDetailData()

    }


}






