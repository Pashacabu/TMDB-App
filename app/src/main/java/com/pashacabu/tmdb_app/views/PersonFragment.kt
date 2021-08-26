package com.pashacabu.tmdb_app.views

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.model.ConnectionChecker
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.PersonResponse
import com.pashacabu.tmdb_app.view_model.MoviesListViewModel
import com.pashacabu.tmdb_app.view_model.MyViewModelFactory
import com.pashacabu.tmdb_app.view_model.PersonViewModel
import com.pashacabu.tmdb_app.views.adapters.MySpinnerAdapter
import com.pashacabu.tmdb_app.views.adapters.SpinnerSortingInterface
import com.pashacabu.tmdb_app.views.gallery.GalleryDialogFragment
import com.pashacabu.tmdb_app.views.myNestedScrollView.MyNestedScrollView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PersonFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    AdapterView.OnItemSelectedListener {

    private var personId = 0
    private var person: PersonResponse = PersonResponse()
    private lateinit var viewModel: PersonViewModel
    private lateinit var connectionChecker: ConnectionChecker
    private lateinit var picture: ImageView
    private lateinit var name: TextView
    private lateinit var occupation: TextView
    private lateinit var gender: TextView
    private lateinit var birthday: TextView
    private lateinit var deathDay: TextView
    private lateinit var placeOfBirth: TextView
    private lateinit var bio: TextView
    private lateinit var sortSpinner: Spinner
    private lateinit var back: View
    private lateinit var warning: TextView
    private lateinit var personSwipeRefresh: SwipeRefreshLayout
    private lateinit var personScrollView: MyNestedScrollView
    private lateinit var perssonMoviesContainer: FragmentContainerView
    private var goBackClickListener: MovieDetailsFragment.GoBackClickListener? = null
    private lateinit var listViewModel: MoviesListViewModel
    private lateinit var fragment: MoviesListFragment
    private lateinit var gallery: GalleryDialogFragment

    private val spinnerClickListener = object : SpinnerSortingInterface {
        override fun sortingSelected(query: String, position: Int) {
            listViewModel.setSorting(query)
            sortSpinner.setSelection(position, true)
            sortSpinner.javaClass.getDeclaredMethod("onDetachedFromWindow").apply {
                isAccessible = true
                invoke(sortSpinner)
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MovieDetailsFragment.GoBackClickListener) {
            goBackClickListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = MyViewModelFactory.PersonViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(PersonViewModel::class.java)
        connectionChecker = ConnectionChecker.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.person_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        findViews(view)
        fragment = addMoviesListFragment()
        personId = arguments?.getInt(PERSON) ?: 0
        personSwipeRefresh.transitionName = arguments?.getString("transition_name")
        viewModel.loadData(personId)
        CoroutineScope(Dispatchers.Default).launch {
            delay(100) //waiting for fragment to initialize and attach to context
            listViewModel =
                fragment.let { ViewModelProvider(it).get(MoviesListViewModel::class.java) }
            if (this@PersonFragment::listViewModel.isInitialized) {
                CoroutineScope(Dispatchers.Main).launch { subscribeViewModels() }
            }
        }
    }

    private fun addMoviesListFragment(): MoviesListFragment {
        var fragment = childFragmentManager.findFragmentByTag(LIST)
        when (fragment) {
            null -> {
                fragment = MoviesListFragment()
                val arg = Bundle()
                arg.putInt(PERSON, arguments?.getInt(PERSON) ?: 0)
                arg.putString(MoviesListFragment.ENDPOINT, LIST)
                fragment.arguments = arg
                childFragmentManager.beginTransaction()
                    .add(R.id.personMoviesContainer, fragment, LIST)
                    .commit()
            }
            else -> {
                if (!fragment.isAdded) {
                    childFragmentManager.beginTransaction()
                        .add(R.id.personMoviesContainer, fragment, LIST)
                        .commit()
                }
            }
        }
        return fragment as MoviesListFragment
    }

    private fun subscribeViewModels() {
        connectionChecker.observe(this.viewLifecycleOwner, {
            connectionObserver(it)
            viewModel.setConnectionState(it)
        })
        viewModel.personData.observe(this.viewLifecycleOwner, {
            person = it
            showPersonData()
        })
        viewModel.loadingState.observe(this.viewLifecycleOwner, {
            personSwipeRefresh.isRefreshing = it

        })
    }


    private fun showPersonData() {
        val baseURL = "https://image.tmdb.org/t/p/w780"
        Glide.with(requireContext())
            .load(baseURL + person.profilePath)
            .apply {
                RequestOptions().dontTransform()
            }
            .placeholder(R.drawable.empty_person)
            .into(picture)
        name.text = person.name
        occupation.text = person.knownForDepartment
        when (person.gender) {
            1 -> gender.text = activity?.getString(R.string.gender_female)
            else -> gender.text = activity?.getString(R.string.gender_male)
        }
        birthday.text = person.birthDay
        placeOfBirth.text = person.placeOfBirth
        when (person.deathDay) {
            null -> deathDay.text = "-"
            else -> deathDay.text = person.deathDay
        }
        bio.text = person.biography
        if (bio.lineCount > BIOLINES) {
            bio.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient)
        }
    }

    private fun connectionObserver(state: Boolean) {
        when (state) {
            true -> {
                warning.visibility = View.GONE
            }
            false -> {
                warning.visibility = View.VISIBLE
            }
        }
    }

    private fun findViews(view: View) {
        personSwipeRefresh = view.findViewById(R.id.person_swipe_refresh)
        personSwipeRefresh.setOnRefreshListener(this)
        personScrollView = view.findViewById<MyNestedScrollView>(R.id.personScrollView)
        perssonMoviesContainer = view.findViewById(R.id.personMoviesContainer)
        val params = perssonMoviesContainer.layoutParams
        params.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        perssonMoviesContainer.layoutParams = params
        picture = view.findViewById(R.id.personPicture)
        picture.setOnClickListener {
            val manager = requireActivity().supportFragmentManager
            gallery = GalleryDialogFragment.newInstance(person)
            gallery.show(manager, person.name)
        }
        name = view.findViewById(R.id.personName)
        occupation = view.findViewById(R.id.knownAs)
        gender = view.findViewById(R.id.genderDescr)
        birthday = view.findViewById(R.id.birthDayNumber)
        deathDay = view.findViewById(R.id.deathDayNumber)
        placeOfBirth = view.findViewById(R.id.placeOfBirthString)
        bio = view.findViewById(R.id.biographyDescription)
        bio.setOnClickListener {
            collapseBio(it as TextView)
        }
        back = view.findViewById(R.id.backTouch)
        back.setOnClickListener {
            goBackClickListener?.onBackArrowPressed()
        }
        warning = view.findViewById(R.id.offline_warning)
        sortSpinner = view.findViewById(R.id.sortBySpinner)
        sortSpinner.adapter = MySpinnerAdapter(spinnerClickListener)
        sortSpinner.dropDownHorizontalOffset =
            resources.getDimension(R.dimen.crew_recycler_spacer).toInt()
    }

    private fun collapseBio(tv: TextView) {
        val collapsedLines = BIOLINES
        val totalLines = tv.lineCount
        val maxLines = tv.maxLines
        var animation = ObjectAnimator()
        if (totalLines > collapsedLines) {
            when (maxLines) {
                collapsedLines -> {
                    animation = ObjectAnimator.ofInt(tv, "maxLines", maxLines, totalLines)
                    tv.background = null
                }
                totalLines -> {
                    animation = ObjectAnimator.ofInt(tv, "maxLines", maxLines, collapsedLines)
                    tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient)
                }
                else -> {
                }
            }
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.duration = resources.getInteger(R.integer.animation_duration).toLong()
            animation.start()
        }
    }

    override fun onDetach() {
        super.onDetach()
        goBackClickListener = null
    }


    override fun onRefresh() {
        viewModel.refreshData(personId)
        fragment.callToRefresh()
    }

    companion object {
        fun newInstance(personId: Int): PersonFragment {
            val fragment = PersonFragment()
            val arg = Bundle()
            arg.putInt(PERSON, personId)
            fragment.arguments = arg
            return fragment
        }

        const val PERSON = "personId"
        const val LIST = "list"
        const val BIOLINES = 3
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        spinner.setSelection(position, true)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        val spinner = parent as Spinner
        spinner.setSelection(0)
    }

}