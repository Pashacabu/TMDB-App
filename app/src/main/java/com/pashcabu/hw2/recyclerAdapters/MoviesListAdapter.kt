package com.pashcabu.hw2.recyclerAdapters


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashcabu.hw2.MoviesListFragment
import com.pashcabu.hw2.MyViewModel
import com.pashcabu.hw2.R
import com.pashcabu.hw2.ResultsItem

class MoviesListAdapter(private val openMovieListener: MoviesListClickListener) :
    RecyclerView.Adapter<MoviesListAdapter.MoviesListViewHolder>() {

    var list: List<ResultsItem?> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.movies_list_recycler_item, parent, false)
        return MoviesListViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoviesListViewHolder, position: Int) {
        holder.onBindMovieData(list[position])
        holder.itemView.setOnClickListener {
            list[position]?.id?.let { movieId ->
                list[position]?.title?.let { movieTitle ->
                    openMovieListener.onMovieSelected(
                        movieId,
                        movieTitle
                    )
                }
            }
        }
    }

    class MoviesListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.title)
        private val pgRating: TextView = view.findViewById(R.id.pgRating)
        private val rating: RatingBar = view.findViewById(R.id.rating)
        private val tagLine: TextView = view.findViewById(R.id.tag_line)
        private val reviews: TextView = view.findViewById(R.id.reviews)

        //        private val duration: TextView = view.findViewById(R.id.duration)
        private val poster: ImageView = view.findViewById(R.id.poster_image)
        private val context = view.context

        fun onBindMovieData(movie: ResultsItem?) {
            title.text = movie?.title
            if (movie?.adult == true) {
                pgRating.text = context.resources.getString(R.string.pg, 16)
            } else {
                pgRating.text = context.resources.getString(R.string.pg, 13)
            }
            rating.rating = (movie?.voteAverage?.div(2))?.toFloat() ?: 0f
            tagLine.text = movie?.genres?.let { android.text.TextUtils.join(", ", it) }
            reviews.text = context.resources.getString(R.string.reviews, movie?.voteCount)
//            duration.text = context.getString(R.string.duration, movie.runtime)
            Glide.with(context)
                .load(imageBaseUrl + movie?.posterPath)
                .placeholder(R.drawable.poster_small_placeholder)
                .into(poster)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun loadMoviesList(movies: List<ResultsItem?>) {
        list = movies
    }

}

const val imageBaseUrl = "https://image.tmdb.org/t/p/w185"

interface MoviesListClickListener {
    fun onMovieSelected(movieID: Int, title: String)
}





