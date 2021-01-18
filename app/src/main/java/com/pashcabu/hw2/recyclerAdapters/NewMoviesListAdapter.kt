package com.pashcabu.hw2.recyclerAdapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashcabu.hw2.MoviesListFragment
import com.pashcabu.hw2.MyViewModel
import com.pashcabu.hw2.R
import com.pashcabu.hw2.ResultsItem

class NewMoviesListAdapter(
    private val openMovieListener: MoviesListClickListener
) : RecyclerView.Adapter<NewMoviesListViewHolder>() {

    private val list = ArrayList<ResultsItem?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMoviesListViewHolder {
        return NewMoviesListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.movies_list_recycler_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewMoviesListViewHolder, position: Int) {
        holder.onBindMovieData(list[position])
        holder.itemView.setOnClickListener {
            list[position]?.id?.let { movieId ->
                list[position]?.title?.let { movieTitle ->
                    openMovieListener.onMovieSelected(
                        movieId, movieTitle
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun loadMovies(newMovies: List<ResultsItem?>) {
        val callback = MoviesDiffCallback(this.list, newMovies)
        val result = DiffUtil.calculateDiff(callback)
//        list.clear()
        list.addAll(newMovies)
        result.dispatchUpdatesTo(this)
    }


}

class MyScrollListener() : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0) {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            val lastVisible = manager.findLastCompletelyVisibleItemPosition()
            val totalItems = recyclerView.adapter?.itemCount
            if (totalItems != null) {
                if (lastVisible == totalItems - 1) {
                    Log.d("Adapter", "its time to load more")
                    MyViewModel().showLoading()
//                    MyViewModel().loadMoreMovies()

                }

            }
        }
    }
}

class NewMoviesListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

class MoviesDiffCallback(
    private val oldList: List<ResultsItem?>?,
    private val newList: List<ResultsItem?>?
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
//        Log.d("ADAPTER", "old list size is ${oldList?.size}")
        return oldList?.size ?: 0

    }

    override fun getNewListSize(): Int {
//        Log.d("ADAPTER", "new list size is ${newList?.size}")
        return newList?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList?.get(oldItemPosition)
        val newItem = newList?.get(newItemPosition)
        return oldItem?.id == newItem?.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList?.get(oldItemPosition) == newList?.get(newItemPosition)
    }


}

const val imageBaseUrl = "https://image.tmdb.org/t/p/w185"

interface MoviesListClickListener {
    fun onMovieSelected(movieID: Int, title: String)
}
