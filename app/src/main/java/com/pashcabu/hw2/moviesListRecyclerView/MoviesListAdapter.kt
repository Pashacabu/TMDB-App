package com.pashcabu.hw2.moviesListRecyclerView


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.pashcabu.hw2.MoviesList
import com.pashcabu.hw2.R

class MoviesListAdapter(private val openMovieListener: MoviesListClickListener) : RecyclerView.Adapter<MoviesListAdapter.MoviesListViewHolder>() {

    private var list: List<Movie> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movies_list_recycler_item, parent, false)
        return MoviesListViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoviesListViewHolder, position: Int) {
        holder.onBindMovieData(list[position])
        holder.itemView.setOnClickListener {
            openMovieListener.onMovieSelected(list[position].title)
        }
    }

    class MoviesListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.title)
        private val pgRating: TextView = view.findViewById(R.id.pgRating)
        private val rating: RatingBar = view.findViewById(R.id.rating)
        private val tagLine: TextView = view.findViewById(R.id.tag_line)
        private val reviews: TextView = view.findViewById(R.id.reviews)
        private val duration: TextView = view.findViewById(R.id.duration)
        private val poster: ImageView = view.findViewById(R.id.poster_image)
        private val context: Context = view.context

        fun onBindMovieData(movie: Movie) {
            title.text = context.resources.getString(movie.title)
            pgRating.text = context.resources.getString(movie.pgRating)
            rating.rating = movie.rating.toFloat()
            tagLine.text = context.resources.getString(movie.tags)
            reviews.text = context.getString(R.string.reviews2, movie.reviews)
            duration.text = context.getString(R.string.duration, movie.duration)
            poster.setImageResource(movie.poster)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun loadMoviesList(moviesList: List<Movie>) {
        list = moviesList
    }
}
interface MoviesListClickListener {
    fun onMovieSelected(title: Int)
}

