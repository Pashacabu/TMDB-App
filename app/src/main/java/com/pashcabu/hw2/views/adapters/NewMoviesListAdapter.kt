package com.pashcabu.hw2.views.adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashcabu.hw2.R
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie

class NewMoviesListAdapter(
    private val openMovieListener: MoviesListClickListener,
) : RecyclerView.Adapter<NewMoviesListViewHolder>() {

    private var list: MutableList<Movie?> = mutableListOf()
    var animation: Int? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMoviesListViewHolder {
        return NewMoviesListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.movies_list_recycler_item, parent, false)
        )
    }

    //    override fun onBindViewHolder(
//        holder: NewMoviesListViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//        if (payloads.isNotEmpty()){
////            var o = Bundle()
//            val o= payloads[0] as Bundle
//            for (string in o.keySet()){
//                if (string == "addedToFavourite"){
//                    val liked = o.getBoolean("addedToFavourite")
//                    if (liked){
//                        holder.inFavourite.setImageResource(R.drawable.like_positive)
//                    } else {
//                        holder.inFavourite.setImageResource(R.drawable.like)
//                    }
//                }
//                if (string == "title"){
//                    holder.title.text = o.getCharSequence("title")
//                }
//            }
//        } else {
//            onBindViewHolder(holder, position)
//        }
//    }
    override fun onBindViewHolder(holder: NewMoviesListViewHolder, position: Int) {
        holder.onBindMovieData(list[position])
        holder.itemView.transitionName = "transition+ ${list[position]?.id}"
        holder.itemView.animation =
            animation?.let { AnimationUtils.loadAnimation(holder.itemView.context, it) }

        holder.itemView.setOnClickListener {
            list[position]?.id?.let { movieId ->
                list[position]?.title?.let { movieTitle ->
                    openMovieListener.onMovieSelected(
                        movieId, movieTitle, holder.itemView
                    )
                }
            }
        }
        holder.inFavourite.setOnClickListener {

            if (list[holder.adapterPosition]?.addedToFavourite == false) {
                holder.inFavourite.setImageResource(R.drawable.like_positive)
            } else {
                holder.inFavourite.setImageResource(R.drawable.like)
            }
            list[holder.adapterPosition]?.let { movie ->
                openMovieListener.onMovieLiked(movie)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun loadMovies(newMovies: List<Movie?>) {
        val callback = MoviesDiffCallback(list, newMovies)
        val result = DiffUtil.calculateDiff(callback)
        list.clear()
        list.addAll(newMovies)
        result.dispatchUpdatesTo(this)
    }

    inner class AnimationScrollListener() : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            animation = if (dy > 0) {
                R.anim.recycler_animation_scroll_down
            } else {
                R.anim.recycler_animation_scroll_up
            }
        }
    }
}

class MyScrollListener(private val callback: () -> Unit) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0) {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            val lastVisible = manager.findLastCompletelyVisibleItemPosition()
            val totalItems = recyclerView.adapter?.itemCount
            if (totalItems != null) {
                if (lastVisible == totalItems - 1) {
                    callback()
                }
            }
        }
    }
}

class NewMoviesListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.title)
    private val pgRating: TextView = view.findViewById(R.id.pgRating)
    private val rating: RatingBar = view.findViewById(R.id.rating)
    private val tagLine: TextView = view.findViewById(R.id.tag_line)
    private val reviews: TextView = view.findViewById(R.id.reviews)
    val inFavourite: ImageView = view.findViewById(R.id.like)

    //        private val duration: TextView = view.findViewById(R.id.duration)
    private val poster: ImageView = view.findViewById(R.id.poster_image)
    private val context = view.context

    fun onBindMovieData(movie: Movie?) {
        title.text = movie?.title
        if (movie?.adult == true) {
            pgRating.text = context.resources.getString(R.string.pg, 16)
        } else {
            pgRating.text = context.resources.getString(R.string.pg, 13)
        }
        if (movie?.addedToFavourite == true) {
            inFavourite.setImageResource(R.drawable.like_positive)
        } else {
            inFavourite.setImageResource(R.drawable.like)
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

    companion object {
        const val imageBaseUrl = "https://image.tmdb.org/t/p/w185"
    }
}

class MoviesDiffCallback(
    private val oldList: MutableList<Movie?>?,
    private val newList: List<Movie?>?
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
//        Log.d("DIFFUTILLS", oldList?.get(0)?.title.toString())
        return oldList?.size ?: 0
    }

    override fun getNewListSize(): Int {
//        Log.d("DIFFUTILLS", newList?.get(0)?.title.toString())
        return newList?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList?.get(oldItemPosition)
        val newItem = newList?.get(newItemPosition)
        return oldItem?.id == newItem?.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        oldList?.get(oldItemPosition)?.title?.let { Log.d("DIFFUTILLS", it) }
        newList?.get(newItemPosition)?.title?.let { Log.d("DIFFUTILLS", it) }
        return oldList?.get(oldItemPosition) == newList?.get(newItemPosition)
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
//        val oldMovie = oldList?.get(oldItemPosition)
//        val newMovie =newList?.get(newItemPosition)
//
//        val difference = Bundle()
//        if (oldMovie?.addedToFavourite!=newMovie?.addedToFavourite){
//            newMovie?.addedToFavourite?.let { difference.putBoolean("addedToFavourite", it) }
//        }
//        if (oldMovie?.title!=newMovie?.title){
//            newMovie?.title?.let { difference.putString("title", it) }
//        }
//        return difference
    }
}


interface MoviesListClickListener {
    fun onMovieSelected(movieID: Int, title: String, view: View)
    fun onMovieLiked(movie: Movie)
}
