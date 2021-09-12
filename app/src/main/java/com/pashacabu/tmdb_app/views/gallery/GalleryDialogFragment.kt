package com.pashacabu.tmdb_app.views.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.PersonResponse

class GalleryDialogFragment() : DialogFragment() {

    private lateinit var person: PersonResponse
    private lateinit var bPrev: ImageView
    private lateinit var bNext: ImageView
    private lateinit var iBig: ImageView
    private lateinit var imagesRecycler: RecyclerView
    private lateinit var adapter: AdapterGallery
    private var width: Int? = null
    private var height: Int? = null


    private val photoInterface = object : PhotoInterface {

        override fun showPhoto(photo: String?) {
            context?.let {
                Glide.with(it)
                    .load("https://image.tmdb.org/t/p/original$photo")
                    .into(iBig)
            }
        }

    }

    private val swipeInterface = object : SwipeInterface {
        override fun onSwipeLeft() {
            loadBigP(adapter.getPrevious())
        }

        override fun onSwipeRight() {
            loadBigP(adapter.getNext())
        }

        override fun onSwipeDown() {
            this@GalleryDialogFragment.dismiss()
        }

        override fun onSwipeUp() {
            this@GalleryDialogFragment.dismiss()
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gallery_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        person = Gson().fromJson(arguments?.getString("person"), PersonResponse::class.java)
        getSizes()
        findViews(view)
        setUpAdapter(imagesRecycler)
        setUpSwipeListener()
        loadImages(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpSwipeListener() {
        iBig.setOnTouchListener(OnSwipeTouchListener(requireContext(), swipeInterface))
    }

    private fun getSizes() {
        width = requireContext().resources.displayMetrics.widthPixels
        height = requireContext().resources.displayMetrics.heightPixels
    }

    private fun loadImages(savedInstanceState: Bundle?) {
        adapter.loadPerson(person)
        when (val current = savedInstanceState?.getString(cImageKey)) {
            "", null -> loadBigP(adapter.getFirstImage())
            else -> {
                loadBigP(current)
                adapter.select(savedInstanceState.getInt(cPositionKey))
            }
        }

    }

    private fun loadBigP(string: String?) {
        Glide.with(requireContext())
            .load("https://image.tmdb.org/t/p/original${string}")
            .into(iBig)
        imagesRecycler.scrollToPosition(adapter.outCurrentItem)
    }

    private fun setUpAdapter(imagesRecycler: RecyclerView) {
        adapter = AdapterGallery(photoInterface)
        imagesRecycler.adapter = adapter
        imagesRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun findViews(view: View) {
        imagesRecycler = view.findViewById(R.id.recycler)
        bPrev = view.findViewById(R.id.previous)
        bPrev.setOnClickListener {
            loadBigP(adapter.getPrevious())
            imagesRecycler.scrollToPosition(adapter.outCurrentItem)
        }
        bNext = view.findViewById(R.id.next)
        bNext.setOnClickListener {
            loadBigP(adapter.getNext())
            imagesRecycler.scrollToPosition(adapter.outCurrentItem)
        }
        iBig = view.findViewById(R.id.bigImage)
        val overallHeight = height?.times(0.8)?.toInt()
        val overallWidth = width?.times(0.9)?.toInt()
        if (overallHeight != null && overallWidth != null) {
            view.layoutParams.height = overallHeight
            view.layoutParams.width = overallWidth
            iBig.minimumHeight = (overallHeight * 0.8).toInt()
            iBig.minimumWidth = (overallWidth * 0.8).toInt()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("current_image", adapter.getCurrentImage())
        outState.putInt("current_pos", adapter.outCurrentItem)
    }

    companion object {
        fun newInstance(person: PersonResponse): GalleryDialogFragment {
            val arg = Bundle()
            val gson = Gson().toJson(person)
            arg.putString("person", gson)
            val fragment = GalleryDialogFragment()
            fragment.arguments = arg
            return fragment
        }

        const val cImageKey = "current_image"
        const val cPositionKey = "current_pos"

    }
}