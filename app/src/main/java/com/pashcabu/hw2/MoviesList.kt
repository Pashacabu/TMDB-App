package com.pashcabu.hw2

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pashcabu.hw2.moviesListRecyclerView.*

class MoviesList : Fragment() {

    private var openMovieListener = object : MoviesListClickListener {
        override fun onMovieSelected(title: String) {
//            Toast.makeText(context, title+" is selected", Toast.LENGTH_SHORT).show()
          activity?.supportFragmentManager?.beginTransaction()
                  ?.add(R.id.fragment_container, MovieDetails.newInstance(title))
                  ?.addToBackStack(title)
                  ?.commit()
        }
    }
    private var adapter : MoviesListAdapter = MoviesListAdapter(openMovieListener)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movies_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val moviesListRecyclerView: RecyclerView = view.findViewById(R.id.movies_list_recycler_view)
        moviesListRecyclerView.setHasFixedSize(true)
        moviesListRecyclerView.adapter = adapter
        val orientation = view.context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 2)
            moviesListRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 7))
        } else {
            moviesListRecyclerView.layoutManager = GridLayoutManager(context, 3)
            moviesListRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 14))
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.loadMoviesList(MoviesData().getMovies())
    }
}


