package com.pashcabu.hw2.movieDetailsRecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pashcabu.hw2.R
import java.util.zip.Inflater

class MovieDetailsAdapter(private var movieDetailsActorsClickListener : MovieDetailsActorsClickListener) : RecyclerView.Adapter<ActorsViewHolder>() {
    var actors : List<Actor> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorsViewHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.cast_recycler_item, parent,false)
        return ActorsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActorsViewHolder, position: Int) {
        holder.onBind(actors[position])
        holder.itemView.setOnClickListener { movieDetailsActorsClickListener.onActorSelected(actors[position]) }
    }

    override fun getItemCount(): Int {
        return actors.size
    }
    fun loadActorsData(title : String?){
        when (title){
            "Avengers: End Game" -> actors = ActorData().avengers()
            "Tenet" -> actors = ActorData().tenet()
            "Black Widow" -> actors = ActorData().blackWidow()
            "Wonder Woman 1984" -> actors = ActorData().wonderWoman()
        }

    }
}
class ActorsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val actorPhoto : ImageView = view.findViewById(R.id.actor_photo)
    private val actorName : TextView = view.findViewById(R.id.actor_name)
    private val context : Context = view.context

    fun onBind(actor : Actor){
        actorPhoto.setImageResource(actor.image)
        actorName.text=context.getString(actor.name)
    }

}

interface MovieDetailsActorsClickListener {
    fun onActorSelected(actor: Actor)
}