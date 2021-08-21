package com.pashacabu.tmdb_app.views.adapters


import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CastResponse
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CastItem
import com.pashacabu.tmdb_app.R


class MovieDetailsCastAdapter(private var movieDetailsActorsClickListener: MovieDetailsActorsClickListener) :
    RecyclerView.Adapter<ActorsViewHolder>() {
    var actors: List<CastItem?>? = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cast_recycler_item, parent, false)
        val orientation = parent.context.resources.configuration.orientation
        val width = parent.context.resources.displayMetrics.widthPixels
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> view.layoutParams.width =
                (width * 0.9 / 3.5).toInt()
            Configuration.ORIENTATION_LANDSCAPE -> view.layoutParams.width =
                (width * 0.9 / 5.5).toInt()
        }
        return ActorsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActorsViewHolder, position: Int) {
        holder.onBind(actors?.get(position))
        holder.itemView.transitionName = holder.itemView.context.getString(
            R.string.shared_person,
            actors?.get(position)?.id,
            position
        )
        holder.itemView.setOnClickListener {
            actors?.get(position)?.id.let { it1 ->
                movieDetailsActorsClickListener.onActorSelected(
                    it1 ?: 0,
                    holder.itemView
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return actors?.size ?: 0
    }

    fun loadActorsData(cast: CastResponse) {
        actors = SortPeopleByPhoto().sortCast(cast.castList)
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
    fun onActorSelected(actorId: Int, view: View)
}


