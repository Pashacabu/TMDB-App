package com.pashcabu.hw2

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pashcabu.hw2.data.loadMovies
import com.pashcabu.hw2.moviesListRecyclerView.*
import kotlinx.coroutines.*

class MoviesListFragment : Fragment() {

    private var openMovieListener = object : MoviesListClickListener {
        override fun onMovieSelected(movieID: Int, title: String) {
            activity?.supportFragmentManager?.beginTransaction()?.add(R.id.fragment_container, MovieDetailsFragment.newInstance(movieID))?.addToBackStack(title)?.commit()
        }
    }
    private var adapter: MoviesListAdapter = MoviesListAdapter(openMovieListener)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movies_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val moviesListRecyclerView: RecyclerView = view.findViewById(R.id.movies_list_recycler_view)
        moviesListRecyclerView.setHasFixedSize(true)
        val orientation = view.context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 2)
            moviesListRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 7))
        } else {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 3)
            moviesListRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 14))
        }
        coroutineScope.launch {
            val listOfMovies = loadMovies(view.context)
            moviesListRecyclerView.adapter = adapter
            adapter.loadMoviesList(listOfMovies)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

}


