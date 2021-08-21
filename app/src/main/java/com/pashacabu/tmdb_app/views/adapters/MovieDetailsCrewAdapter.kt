package com.pashacabu.tmdb_app.views.adapters

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CastResponse
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CrewItem

class MovieDetailsCrewAdapter(private val clickListener: MovieDetailsCrewClickListener) :
    RecyclerView.Adapter<CrewViewHolder>() {

    private var crew: List<CrewItem?>? = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.crew_recycler_item, parent, false)
        val orientation = parent.context.resources.configuration.orientation
        val width = parent.context.resources.displayMetrics.widthPixels
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> view.layoutParams.width =
                (width * 0.9 / 3.5).toInt()
            Configuration.ORIENTATION_LANDSCAPE -> view.layoutParams.width =
                (width * 0.9 / 5.5).toInt()
        }
        return CrewViewHolder(view)
    }

    override fun onBindViewHolder(holder: CrewViewHolder, position: Int) {
        holder.onBind(crew?.get(position))
        holder.itemView.transitionName = holder.itemView.context.getString(
            R.string.shared_person,
            crew?.get(position)?.id,
            position
        )
        holder.itemView.setOnClickListener {
            clickListener.onCrewPersonSelected(crew?.get(position), holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return crew?.size ?: 0
    }

    fun loadCrew(cast: CastResponse) {
        crew = SortPeopleByPhoto().sortCrew(cast.crew)
    }
}

class CrewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val name = itemView.findViewById<TextView>(R.id.crew_name)
    private val job = itemView.findViewById<TextView>(R.id.crew_job)
    private val picture = itemView.findViewById<ImageView>(R.id.crew_photo)


    fun onBind(crewMember: CrewItem?) {
        name.text = crewMember?.name
        job.text = crewMember?.job
        Glide.with(itemView.context)
            .load(baseImageUrl + crewMember?.profilePath)
            .placeholder(R.drawable.empty_person)
            .into(picture)
    }

    companion object {
        private const val baseImageUrl = "https://image.tmdb.org/t/p/w185"
    }
}

interface MovieDetailsCrewClickListener {
    fun onCrewPersonSelected(person: CrewItem?, view: View)
}