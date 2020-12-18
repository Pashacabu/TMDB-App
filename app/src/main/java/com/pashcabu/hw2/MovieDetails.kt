package com.pashcabu.hw2

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashcabu.hw2.data.loadMovie
import com.pashcabu.hw2.movieDetailsRecyclerView.MovieDetailsActorsClickListener
import com.pashcabu.hw2.movieDetailsRecyclerView.MovieDetailsAdapter
import kotlinx.coroutines.*

class MovieDetails : Fragment() {

    private var toast :Toast? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var backArrow:ImageView?=null
    private var backButton:TextView?=null
    private var movieDetailsClickListener : MovieDetailsClickListener?=null
    private var movieDetailsActorsClickListener = object : MovieDetailsActorsClickListener {
        override fun onActorSelected(actor: com.pashcabu.hw2.data.Actor) {
            if (toast==null){
                toast = Toast.makeText(context, actor.name, Toast.LENGTH_SHORT)
                toast?.show()
            } else {
                toast?.cancel()
                toast = Toast.makeText(context, actor.name, Toast.LENGTH_SHORT)
                toast?.show()
            }
        }
    }
    private val adapter = MovieDetailsAdapter(movieDetailsActorsClickListener )
    private var movieID = 0
    private var movie:com.pashcabu.hw2.data.Movie? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        movieID = arguments?.getInt(TITLE) ?: 0
        val job2 = coroutineScope.async {
            context?.let { loadMovie(it, movieID) }
        }
        movie = runBlocking { job2.await() }
        return inflater.inflate(R.layout.movie_details_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val poster: ImageView = view.findViewById(R.id.mainPoster)
        context?.let { Glide.with(it)
                .load(movie?.backdrop)
                .placeholder(R.drawable.big_poster_placeholder)
                .into(poster) }
        val pgRating: TextView = view.findViewById(R.id.pgRating)
        pgRating.text = movie?.minimumAge.toString()
        val title: TextView = view.findViewById(R.id.movie_title)
        title.text = movie?.title ?: "Nothing to show"
        var tagLine = ""
        movie?.let { movie -> movie.genres.forEach { tagLine+=it.name+", " } }
        val tags: TextView = view.findViewById(R.id.tag_line)
        tags.text = tagLine
        val rating: RatingBar = view.findViewById(R.id.rating)
        rating.rating = movie?.ratings?.div(2) ?: 0f
        val reviews: TextView = view.findViewById(R.id.reviews)
        reviews.text = context?.getString(R.string.reviews2, movie?.numberOfRatings ?: 0)
        val story: TextView = view.findViewById(R.id.storylineDescription)
        story.text = movie?.overview ?: "Nothing to show"
        backArrow=view.findViewById<ImageView?>(R.id.backArrow)?.apply{
            setOnClickListener{movieDetailsClickListener?.onBackArrowPressed()}
        }
        backButton=view.findViewById<TextView?>(R.id.backButton)?.apply{
            setOnClickListener{movieDetailsClickListener?.onBackArrowPressed()}
        }
        val actorsRecyclerView : RecyclerView= view.findViewById(R.id.actors_recycler_view)

        actorsRecyclerView.adapter = adapter
        actorsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        actorsRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 5))


    }

    override fun onStart() {
        super.onStart()
        movie?.actors?.let { adapter.loadActorsData(it) }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MovieDetailsClickListener){
            movieDetailsClickListener=context
        }
    }

    override fun onDetach() {
        super.onDetach()
        movieDetailsClickListener=null
    }


    interface MovieDetailsClickListener{
        fun onBackArrowPressed()
    }

    companion object {
        fun newInstance (movieID : Int ): MovieDetails{
            val arg = Bundle()
            arg.putInt(TITLE, movieID)
            val fragment = MovieDetails()
            fragment.arguments = arg
            return fragment
        }
        const val TITLE = "movieTitle"
    }
}