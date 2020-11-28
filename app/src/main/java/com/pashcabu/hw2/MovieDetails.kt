package com.pashcabu.hw2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class MovieDetails : Fragment() {

    var backArrow:ImageView?=null
    var movieDetailsClickListener : MovieDetailsClickListener?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_details_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backArrow=view.findViewById<ImageView?>(R.id.backArrow)?.apply{
            setOnClickListener{movieDetailsClickListener?.onBackArrowPressed()}
        }
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
}