package com.pashcabu.hw2

import android.content.Context
import android.os.Bundle
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
import com.pashcabu.hw2.data.Movie
import com.pashcabu.hw2.data.loadMovie
import com.pashcabu.hw2.movieDetailsRecyclerView.MovieDetailsActorsClickListener
import com.pashcabu.hw2.movieDetailsRecyclerView.MovieDetailsAdapter
import kotlinx.coroutines.*

class MovieDetailsFragment : Fragment() {


    private lateinit var poster: ImageView
    private lateinit var pgRating: TextView
    private lateinit var title: TextView
    private lateinit var tags: TextView
    private lateinit var rating: RatingBar
    private lateinit var reviews: TextView
    private lateinit var story: TextView
    private lateinit var castTitle: TextView
    private lateinit var actorsRecyclerView: RecyclerView
    private var toast: Toast? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var backArrow: ImageView? = null
    private var backButton: TextView? = null
    private var movieDetailsClickListener: MovieDetailsClickListener? = null
    private var movieDetailsActorsClickListener = object : MovieDetailsActorsClickListener {
        override fun onActorSelected(actor: com.pashcabu.hw2.data.Actor) {
            if (toast == null) {
                toast = Toast.makeText(context, actor.name, Toast.LENGTH_SHORT)
                toast?.show()
            } else {
                toast?.cancel()
                toast = Toast.makeText(context, actor.name, Toast.LENGTH_SHORT)
                toast?.show()
            }
        }
    }
    private val adapter = MovieDetailsAdapter(movieDetailsActorsClickListener)
    private var movieID = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        coroutineScope.launch {
            movieID = arguments?.getInt(TITLE) ?: 0
            val movie = loadMovie(requireContext(), movieID)
            updateData(movie)
        }
    }

    private suspend fun updateData(movie: Movie) = withContext(Dispatchers.Main) {
        context?.let {
            Glide.with(it)
                    .load(movie.backdrop)
                    .placeholder(R.drawable.poster_big_placeholder)
                    .into(poster)
        }
        pgRating.text = context?.resources?.getString(R.string.pg, movie.minimumAge) ?: ""
        title.text = movie.title
        var tagLine = ""
        movie.let { movie -> movie.genres.forEach { tagLine += it.name + ", " } }
        tags.text = tagLine
        rating.rating = movie.ratings / 2
        reviews.text = context?.getString(R.string.reviews, movie.numberOfRatings)
        story.text = movie.overview
        if (movie.actors.isEmpty()) {
            castTitle.text = getString(R.string.no_actors_data)
        }
        actorsRecyclerView.adapter = adapter
        adapter.loadActorsData(movie.actors)

    }

    private fun findViews(view: View) {
        poster = view.findViewById(R.id.mainPoster)
        pgRating = view.findViewById(R.id.pgRating)
        title = view.findViewById(R.id.movie_title)
        tags = view.findViewById(R.id.tag_line)
        rating = view.findViewById(R.id.rating)
        reviews = view.findViewById(R.id.reviews)
        story = view.findViewById(R.id.storylineDescription)
        castTitle = view.findViewById(R.id.cast_title)
        actorsRecyclerView = view.findViewById(R.id.actors_recycler_view)
        backArrow = view.findViewById<ImageView?>(R.id.backArrow)?.apply {
            setOnClickListener { movieDetailsClickListener?.onBackArrowPressed() }
        }
        backButton = view.findViewById<TextView?>(R.id.backButton)?.apply {
            setOnClickListener { movieDetailsClickListener?.onBackArrowPressed() }
        }
        actorsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        actorsRecyclerView.addItemDecoration(Decorator().itemSpacing(view, 5))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MovieDetailsClickListener) {
            movieDetailsClickListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        movieDetailsClickListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }


    interface MovieDetailsClickListener {
        fun onBackArrowPressed()
    }

    companion object {
        fun newInstance(movieID: Int): MovieDetailsFragment {
            val arg = Bundle()
            arg.putInt(TITLE, movieID)
            val fragment = MovieDetailsFragment()
            fragment.arguments = arg
            return fragment
        }

        const val TITLE = "movieTitle"
    }
}