package com.pashcabu.hw2

import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.util.DisplayMetrics
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
import com.google.android.material.snackbar.Snackbar
import com.pashcabu.hw2.movieDetailsRecyclerView.Actor
import com.pashcabu.hw2.movieDetailsRecyclerView.MovieDetailsActorsClickListener
import com.pashcabu.hw2.movieDetailsRecyclerView.MovieDetailsAdapter
import com.pashcabu.hw2.moviesListRecyclerView.Movie
import com.pashcabu.hw2.moviesListRecyclerView.MoviesData

class MovieDetails : Fragment() {


    private var backArrow:ImageView?=null
    private var movieDetailsClickListener : MovieDetailsClickListener?=null
    private var movieDetailsActorsClickListener = object : MovieDetailsActorsClickListener {
        override fun onActorSelected(actor: Actor) {
//            if (toast==null){
//                toast = Toast.makeText(context, actor.name, Toast.LENGTH_SHORT)
//                toast?.show()
//            } else {
//                toast?.cancel()
//                toast = Toast.makeText(context, actor.name, Toast.LENGTH_SHORT)
//                toast?.show()
//            }
        }
    }
    private val adapter = MovieDetailsAdapter(movieDetailsActorsClickListener )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_details_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val movie:Movie = MoviesData().findMovieByName(arguments?.getString(TITLE))
//        Toast.makeText(context, movie.title, Toast.LENGTH_SHORT).show()
        val poster : ImageView = view.findViewById(R.id.mainPoster)
        poster.setImageResource(movie.bigPoster)
        val pgRating:TextView = view.findViewById(R.id.pgRating)
        pgRating.text=movie.pgRating
        val title : TextView = view.findViewById(R.id.movie_title)
        title.text= movie.title
        val tags : TextView = view.findViewById(R.id.tag_line)
        tags.text = movie.tags
        val rating : RatingBar = view.findViewById(R.id.rating)
        rating.rating = movie.rating.toFloat()
        val reviews : TextView = view.findViewById(R.id.reviews)
        reviews.text = context?.getString(R.string.reviews2, movie.reviews)
        val story : TextView = view.findViewById(R.id.storylineDescription)
        story.text = context?.getString(movie.story)
        backArrow=view.findViewById<ImageView?>(R.id.backArrow)?.apply{
            setOnClickListener{movieDetailsClickListener?.onBackArrowPressed()}
        }
        val actorsRecyclerView : RecyclerView= view.findViewById(R.id.actors_recycler_view)

        actorsRecyclerView.adapter = adapter
        actorsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        actorsRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 5))


    }

    override fun onStart() {
        super.onStart()
        adapter.loadActorsData(arguments?.getString("title"))
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
        fun newInstance (title:String): MovieDetails{
            val arg = Bundle()
            arg.putString(TITLE, title)
            val fragment = MovieDetails()
            fragment.arguments = arg
            return fragment
        }
        var toast :Toast?=null
        const val TITLE = "title"
    }
}