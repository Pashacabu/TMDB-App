package com.pashcabu.hw2.views


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialElevationScale
import com.pashcabu.hw2.R
import com.pashcabu.hw2.view_model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var searchLayout: LinearLayout
    private lateinit var searchET: EditText
    private lateinit var transName: String
    private var currentPage = 1
    private var searchQuery = String()
    private lateinit var viewModel: MoviesListViewModel
    private var currentFragment: MoviesListFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exitTransition = MaterialElevationScale(false).apply {
            duration = 500
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
//            addListFragment()
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
    }

    private fun getViewModel(currentFragment: MoviesListFragment) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(100)
            viewModel = ViewModelProvider(currentFragment).get(MoviesListViewModel::class.java)
            searchET.addTextChangedListener(MyWatcher(viewModel))
            CoroutineScope(Dispatchers.Main).launch { subscribePageStep() }
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Default).launch {
            var time = 0
            while (!this@SearchFragment::viewModel.isInitialized) {
                delay(50)
                time += 50
            }
        }

    }

    private fun subscribePageStep() {
        try {
            viewModel.pageStep.observe(viewLifecycleOwner, {
                currentPage = it
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "View model is empty!", Toast.LENGTH_SHORT).show()
        }
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



