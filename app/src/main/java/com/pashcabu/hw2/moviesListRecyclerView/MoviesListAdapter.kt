package com.pashcabu.hw2.moviesListRecyclerView


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashcabu.hw2.R
import com.pashcabu.hw2.data.Movie

class MoviesListAdapter(private val openMovieListener: MoviesListClickListener) : RecyclerView.Adapter<MoviesListAdapter.MoviesListViewHolder>() {

    var list: List<Movie> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movies_list_recycler_item, parent, false)
        return MoviesListViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoviesListViewHolder, position: Int) {
        holder.onBindMovieData(list[position])
        holder.itemView.setOnClickListener {
            openMovieListener.onMovieSelected(list[position].id, list[position].title)
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
            title.text = movie.title
            pgRating.text = context.resources.getString(R.string.pg, movie.minimumAge)
            rating.rating = movie.ratings / 2
            var tags = ""
            movie.genres.forEach { tags += it.name + ", " }
            tagLine.text = tags
            reviews.text = context.resources.getString(R.string.reviews, movie.numberOfRatings)
            duration.text = context.getString(R.string.duration, movie.runtime)
            Glide.with(context)
                    .load(movie.poster)
                    .placeholder(R.drawable.poster_small_placeholder)
                    .into(poster)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun loadMoviesList(movies: List<Movie>) {
        list = movies

    }


}

interface MoviesListClickListener {
    fun onMovieSelected(movieID: Int, title: String)
}



