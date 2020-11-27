package com.pashcabu.hw2

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class MoviesList : Fragment() {

    private var movieClickListener: MovieClickListener?=null
    private var movie:ImageView?=null
    private var like:ImageView?=null
    private var isLiked:Boolean=false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movies_list_fragment, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        movie=view.findViewById<ImageView?>(R.id.movie).apply{
            setOnClickListener { movieClickListener?.openMovieDetails() }
        }
        like=view.findViewById<ImageView?>(R.id.like).apply {
            setOnClickListener { addToFavorite() }
        }


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MovieClickListener){
            movieClickListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        movieClickListener=null
    }

    private fun addToFavorite(){
        isLiked = if (!isLiked){
            like?.setImageResource(R.drawable.like_selected)
            true
        } else{
            like?.setImageResource(R.drawable.like)
            false
        }


    }

    interface MovieClickListener{
        fun openMovieDetails()

    }
}

