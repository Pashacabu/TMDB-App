package com.pashcabu.hw2.views.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashcabu.hw2.model.data_classes.networkResponses.CastResponse
import com.pashcabu.hw2.model.data_classes.networkResponses.CastItem
import com.pashcabu.hw2.R


class MovieDetailsAdapter(private var movieDetailsActorsClickListener: MovieDetailsActorsClickListener) :
    RecyclerView.Adapter<ActorsViewHolder>() {
    var actors: List<CastItem?>? = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cast_recycler_item, parent, false)
        return ActorsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActorsViewHolder, position: Int) {
        holder.onBind(actors?.get(position))
        holder.itemView.setOnClickListener {
            movieDetailsActorsClickListener.onActorSelected(
                actors?.get(
                    position
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return actors?.size ?: 0
    }

    fun loadActorsData(cast: CastResponse) {
        actors = cast.castList
    }
}

class ActorsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val actorPhoto: ImageView = view.findViewById(R.id.actor_photo)
    private val actorName: TextView = view.findViewById(R.id.actor_name)
    private val context = view.context

    fun onBind(actor: CastItem?) {
        Glide.with(context)
            .load(baseImageUrl + actor?.actorPhoto)
            .placeholder(R.drawable.empty_person)
            .into(actorPhoto)
        actorName.text = context.getString(R.string.starring, actor?.actorName, actor?.character)
    }

    companion object {
        private const val baseImageUrl = "https://image.tmdb.org/t/p/w185"
    }
}

interface MovieDetailsActorsClickListener {
    fun onActorSelected(actor: CastItem?)
}


