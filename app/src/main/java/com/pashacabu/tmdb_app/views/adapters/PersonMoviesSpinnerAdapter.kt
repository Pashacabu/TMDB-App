package com.pashacabu.tmdb_app.views.adapters

import android.database.DataSetObserver
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pashacabu.tmdb_app.R
import com.pashacabu.tmdb_app.model.data_classes.SortingsList
import com.pashacabu.tmdb_app.model.data_classes.SpinnerSorting

interface SpinnerSortingInterface {
    fun sortingSelected(query: String, position: Int)
}

class MySpinnerAdapter(
    private val spinnerInterface: SpinnerSortingInterface
) : SpinnerAdapter {

    private val sortingTypes = SortingsList().spinnerList()

    override fun registerDataSetObserver(observer: DataSetObserver?) {

    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {

    }

    override fun getCount(): Int {
        return sortingTypes.size
    }

    override fun getItem(position: Int): SpinnerSorting {
        return sortingTypes[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position)
        val view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.spinner_dropdown_item, parent, false)

        val textView: TextView = view.findViewById(R.id.spinnerText)
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            parent?.context?.resources?.getDimension(R.dimen.spinner_item_text_size)!!
        )
        textView.text = item.text

        return view
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position)
        val view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.spinner_dropdown_item, parent, false)
        val textView: TextView = view.findViewById(R.id.spinnerText)
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            parent?.context?.resources?.getDimension(R.dimen.spinner_item_drop_text_size)!!
        )
        textView.text = item.text
        view.setOnClickListener { spinnerInterface.sortingSelected(item.query, position) }
        return view
    }

}