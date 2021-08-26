package com.pashacabu.tmdb_app.views.adapters

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.Movie

class MoviesListAdapter(private val listener: MoviesListAdapterInterface) :
    ListAdapter<Movie, MoviesListViewHolder>(MoviesListDiffCallback()) {

    private var animation: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesListViewHolder {
        val orientation = parent.context.resources.configuration.orientation
        val displayWidth = parent.context.resources.displayMetrics.widthPixels
        val container = LayoutInflater.from(parent.context)
            .inflate(R.layout.movies_list_recycler_item, parent, false)
        val layoutParams = container.layoutParams
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                layoutParams.width =
                    (displayWidth / 2 * 0.9).toInt()
                layoutParams.height = (1.71 * layoutParams.width).toInt()

                container.layoutParams = layoutParams
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                layoutParams.width =
                    (displayWidth * 0.9 / 4).toInt()
                layoutParams.height = (1.71 * layoutParams.width).toInt()
                container.layoutParams = layoutParams
            }

        }
        return MoviesListViewHolder(container)
    }

    override fun onBindViewHolder(
        holder: MoviesListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val o = payloads[0] as Bundle
            for (string in o.keySet()) {
                when (string) {
                    "addedToFavourite" -> {
                        val liked = o.getBoolean(string)
                        if (liked) {
                            holder.inFavourite.setImageResource(R.drawable.like_positive)
                        } else {
                            holder.inFavourite.setImageResource(R.drawable.like)
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MoviesListViewHolder, position: Int) {
        val movie = getItem(position)
        holder.onBindMovieData(movie)
        holder.itemView.transitionName =
            holder.itemView.context.getString(R.string.shared_details, movie.id ?: 0)
//        Log.d("HOLDER", holder.itemView.transitionName)
        holder.itemView.animation =
            animation?.let { AnimationUtils.loadAnimation(holder.itemView.context, it) }

        holder.itemView.setOnClickListener {
            movie?.id?.let { movieId ->
                movie.title?.let { movieTitle ->
                    listener.onMovieSelected(
                        movieId, movieTitle, holder.itemView
                    )
                }
            }
        }
        holder.inFavourite.setOnClickListener {

            if (getItem(holder.adapterPosition)?.addedToFavourite == false) {
                holder.inFavourite.setImageResource(R.drawable.like_positive)
            } else {
                holder.inFavourite.setImageResource(R.drawable.like)
            }
            getItem(holder.adapterPosition)?.let { movie ->
                listener.onMovieLiked(movie)
            }
        }
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

class BottomOfTheListListener(private val callback: () -> Unit) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0 && !recyclerView.canScrollVertically(1)) {
            callback()
        }
    }
}

class SimpleScrollListener(private val callback: (scrolling: Boolean) -> Unit) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        when {
            dy > 50 -> {
                callback(true)
            }
            else -> {
                callback(false)
            }
        }
    }
}

class MoviesListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.title)
    private val pgRating: TextView = view.findViewById(R.id.pgRating)
    private val rating: RatingBar = view.findViewById(R.id.rating)
    private val tagLine: TextView = view.findViewById(R.id.tag_line)
    private val reviews: TextView = view.findViewById(R.id.reviews)
    val inFavourite: ImageView = view.findViewById(R.id.like)
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
        Glide.with(context)
            .load(imageBaseUrl + movie?.posterPath)
            .apply {
                RequestOptions().dontTransform()
            }
            .placeholder(R.drawable.poster_small_placeholder)
            .into(poster)
    }

    companion object {
        const val imageBaseUrl = "https://image.tmdb.org/t/p/w185"
    }
}

class MoviesListDiffCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.title == newItem.title && oldItem.addedToFavourite == newItem.addedToFavourite
    }

    override fun getChangePayload(oldItem: Movie, newItem: Movie): Any {
        val difference = Bundle()
        if (oldItem.addedToFavourite != newItem.addedToFavourite) {
            difference.putBoolean("addedToFavourite", newItem.addedToFavourite)
        }
        return difference
    }

}


interface MoviesListAdapterInterface {
    fun onMovieSelected(id: Int, title: String, view: View)
    fun onMovieLiked(movie: Movie)

}