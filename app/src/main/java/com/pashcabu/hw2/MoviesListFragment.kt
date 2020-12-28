package com.pashcabu.hw2

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pashcabu.hw2.recyclerAdapters.MoviesListAdapter
import com.pashcabu.hw2.recyclerAdapters.MoviesListClickListener

class MoviesListFragment : Fragment() {

    private var openMovieListener: MoviesListClickListener = object : MoviesListClickListener {
        override fun onMovieSelected(movieID: Int, title: String) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.add(R.id.fragment_container, MovieDetailsFragment.newInstance(movieID))
                ?.addToBackStack(title)?.commit()
        }
    }
    private var adapter = MoviesListAdapter(openMovieListener)
    private var moviesListRecyclerView: RecyclerView? = null
    private val viewModel: MyViewModel by viewModels()


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


    }

    private fun loadData() {
        viewModel.loadMoviesListToLiveData(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        loadData()
        setUpAdapter(view)
        viewModel.liveMovieListData.observe(this.viewLifecycleOwner, {
            moviesListRecyclerView?.adapter = adapter
            adapter.loadMoviesList(it)
        })
    }

}


