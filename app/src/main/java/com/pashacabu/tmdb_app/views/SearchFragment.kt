package com.pashacabu.tmdb_app.views


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialElevationScale
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.view_model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var searchLayout: LinearLayout
    private lateinit var searchET: EditText
    private lateinit var backArrow: ImageView
    private lateinit var backTV: TextView
    private lateinit var transName: String
    private var currentPage = 1
    private var searchQuery = String()
    private lateinit var viewModel: MoviesListViewModel
    private var currentFragment: MoviesListFragment? = null
    private var goBackClickListener: GoBack? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.transition_duration).toLong()
        }
        return inflater.inflate(R.layout.search_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        transName = arguments?.getString("TransName") ?: SEARCH
        findViews(view)
        val fragment = childFragmentManager.findFragmentByTag(TAG) as MoviesListFragment?
        if (fragment == null) {
            val listFragment = MoviesListFragment.newInstance(SEARCH)
            childFragmentManager.beginTransaction()
                .replace(R.id.search_results, listFragment, TAG)
                .commit()
            currentFragment = listFragment
        } else {
            currentFragment = fragment
            childFragmentManager.beginTransaction()
                .replace(R.id.search_results, currentFragment!!, TAG)
                .commit()
        }
        if (currentFragment != null && !this::viewModel.isInitialized) {
            getViewModel(currentFragment!!)
        }

    }


    private fun findViews(view: View) {
        searchET = view.findViewById(R.id.searchET)
        searchLayout = view.findViewById(R.id.search_layout)
        searchLayout.transitionName = transName
        backArrow = view.findViewById(R.id.backArrow)
        backTV = view.findViewById(R.id.backButton)
        backArrow.setOnClickListener { goBackClickListener?.onBackArrowPressed() }
        backTV.setOnClickListener { goBackClickListener?.onBackArrowPressed() }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GoBack) {
            goBackClickListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        goBackClickListener = null
    }


    private fun getViewModel(currentFragment: MoviesListFragment) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(100)
            viewModel = ViewModelProvider(currentFragment).get(MoviesListViewModel::class.java)
            CoroutineScope(Dispatchers.Main).launch { subscribePageStep() }
        }
    }

    override fun onResume() {
        super.onResume()
        addTextWatcher()
    }

    private fun addTextWatcher() {
        CoroutineScope(Dispatchers.Default).launch {
            var time = 0
            while (!this@SearchFragment::viewModel.isInitialized) {
                delay(1)
                time += 1
                if (time > 500) break
            }
            searchET.addTextChangedListener(MyWatcher(viewModel))
        }
    }

    private fun subscribePageStep() {
        try {
            viewModel.pageStep.observe(viewLifecycleOwner, {
                currentPage = it
            })
        } catch (e: Exception) {
//            Toast.makeText(requireContext(), "View model is empty!", Toast.LENGTH_SHORT).show()
        }
    }

    interface GoBack {
        fun onBackArrowPressed()
    }

    companion object {
        fun newInstance(str: String): SearchFragment {
            val args = Bundle()
            args.putString("TransName", str)
            val fragment = SearchFragment()
            fragment.arguments = args
            return fragment
        }

        val TAG = "search results"
    }

    inner class MyWatcher(private val viewModel: MoviesListViewModel) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchQuery = s.toString()
            viewModel.search(searchQuery, currentPage, isLoadingMore = false)
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
}



