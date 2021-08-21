package com.pashacabu.tmdb_app.views.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.PersonResponse
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.ProfilesItem

class AdapterGallery(private val photoListener: PhotoInterface) :
    RecyclerView.Adapter<GalleryViewHolder>() {

    private var person: PersonResponse? = null
    private var size = 0
    private var currentItem = 0
    val outCurrentItem get() = currentItem

    private var width = 0
    private var height = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gallery_recycler_item, parent, false)
        width = parent.context.resources.displayMetrics.widthPixels
        height = parent.context.resources.displayMetrics.heightPixels
        view.layoutParams.width = (width / 4.5).toInt()
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val isSelected = position == currentItem
        holder.itemView.isSelected = isSelected
        person?.images?.profiles?.get(position)?.let { holder.onBind(it, isSelected) }
        holder.view.setOnClickListener {
            select(position)
            val photo = person?.images?.profiles?.get(position)?.filePath
            photoListener.showPhoto(photo)
//            currentItem=position
        }

    }

    private fun select(position: Int) {
        notifyItemChanged(currentItem)
        currentItem = position
        notifyItemChanged(currentItem)
    }

    override fun getItemCount(): Int {
        size = person?.images?.profiles?.size ?: 0
        return size
    }

    fun loadPerson(_person: PersonResponse) {
        person = _person
    }

    fun getFirstImage(): String? {
        currentItem = 0
        return person?.images?.profiles?.get(currentItem)?.filePath
    }

    fun getNext(): String? {
        var result: String? = null
        result = when {
            currentItem < size - 1 -> {
                select(currentItem + 1)
                person?.images?.profiles?.get(currentItem)?.filePath
            }
            else -> {
                select(0)
                person?.images?.profiles?.get(currentItem)?.filePath
            }
        }
        return result
    }

    fun getPrevious(): String? {
        var result: String? = null
        result = when {
            currentItem > 0 -> {
                select(currentItem - 1)
                person?.images?.profiles?.get(currentItem)?.filePath
            }
            else -> {
                select(size - 1)
                person?.images?.profiles?.get(currentItem)?.filePath
            }
        }
        return result
    }
}

class GalleryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<ImageView>(R.id.imageItem)


    fun onBind(item: ProfilesItem, isShown: Boolean) {
        Glide.with(view.context)
            .load(BASE_URL + item.filePath)
            .into(image)

    }

    companion object {
        val BASE_URL = "https://image.tmdb.org/t/p/w185"
    }
}

interface PhotoInterface {
    fun showPhoto(photo: String?)
}