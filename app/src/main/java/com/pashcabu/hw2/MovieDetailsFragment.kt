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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashcabu.hw2.recyclerAdapters.MovieDetailsActorsClickListener
import com.pashcabu.hw2.recyclerAdapters.MovieDetailsAdapter


class MovieDetailsFragment : Fragment() {


    private lateinit var poster: ImageView
    private lateinit var pgRating: TextView
    private lateinit var title: TextView
    private lateinit var tags: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var reviews: TextView
    private lateinit var story: TextView
    private lateinit var castTitle: TextView
    private lateinit var actorsRecyclerView: RecyclerView
    private var toast: Toast? = null
    private var backArrow: ImageView? = null
    private var backButton: TextView? = null
    private var movieDetailsClickListener: MovieDetailsClickListener? = null
    private var movieDetailsActorsClickListener = object : MovieDetailsActorsClickListener {
        override fun onActorSelected(actor: CastItem?) {
            if (toast == null) {
                toast = Toast.makeText(context, actor?.actorName, Toast.LENGTH_SHORT)
                toast?.show()
            } else {
                toast?.cancel()
                toast = Toast.makeText(context, actor?.actorName, Toast.LENGTH_SHORT)
                toast?.show()
            }
        }
    }
    private val adapter = MovieDetailsAdapter(movieDetailsActorsClickListener)
    private var movieID = 0
    private val viewModel: MyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.movie_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        movieID = arguments?.getInt(TITLE) ?: 0
        loadMovieDetailsData()
        viewModel.liveMovieDetailsData.observe(this.viewLifecycleOwner, {
            updateMovieData(it)
        })
        viewModel.liveCastData.observe(this.viewLifecycleOwner, {
            updateActorsData(it)
        })
    }

    private fun loadMovieDetailsData() {
        viewModel.loadMovieDetailsToLiveData(movieID)
    }

    private fun updateMovieData(movie: MovieDetailsResponse) {
        context?.let {
            Glide.with(it)
                .load(imageBaseUrl + movie.backdropPath)
                .placeholder(R.drawable.poster_big_placeholder)
                .into(poster)
        }
        if (movie.adult == true) {
            pgRating.text = context?.resources?.getString(R.string.pg, 16)
        } else {
            pgRating.text = context?.resources?.getString(R.string.pg, 13)
        }
        title.text = movie.movieTitle
        val tagsList: List<String?> = movie.genres?.map { it?.genreName } ?: listOf()
        tags.text = android.text.TextUtils.join(", ", tagsList)
        ratingBar.rating = (movie.voteAverage?.div(2))?.toFloat() ?: 0f
        reviews.text = context?.getString(R.string.reviews, movie.reviews)
        story.text = movie.overview
    }

    private fun updateActorsData(actors: Cast) {
        if (actors.castList.isNullOrEmpty()) {
            castTitle.text = getString(R.string.no_actors_data)
        }
        actorsRecyclerView.adapter = adapter
        adapter.loadActorsData(actors)
    }

    private fun findViews(view: View) {
        poster = view.findViewById(R.id.mainPoster)
        pgRating = view.findViewById(R.id.pgRating)
        title = view.findViewById(R.id.movie_title)
        tags = view.findViewById(R.id.tag_line)
        ratingBar = view.findViewById(R.id.rating)
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
        actorsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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

const val imageBaseUrl = "https://image.tmdb.org/t/p/w780"