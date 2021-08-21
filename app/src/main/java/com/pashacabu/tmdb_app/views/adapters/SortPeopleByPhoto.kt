package com.pashacabu.tmdb_app.views.adapters

import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CastItem
import com.pashacabu.tmdb_app.model.data_classes.networkResponses.CrewItem

class SortPeopleByPhoto {

    fun sortCast(people: MutableList<CastItem?>?): List<CastItem> {
        // Sorting with image first
        val list: MutableList<CastItem> = mutableListOf()
        people?.forEach { it ->
            if (it != null) {
                list.add(it)
            }
        }
        for (i in 0 until list.size) {
            for (j in i until list.size) {
                if (list[i].actorPhoto == null) {
                    if (list[j].actorPhoto == null) {
                        continue
                    } else {
                        val temp = list[i]
                        list[i] = list[j]
                        list[j] = temp
                    }
                }
            }
        }
        return list
    }

    fun sortCrew(people: MutableList<CrewItem?>?): List<CrewItem> {
        // Sorting with image first
        val list: MutableList<CrewItem> = mutableListOf()
        people?.forEach { it ->
            if (it != null) {
                list.add(it)
            }
        }
        for (i in 0 until list.size) {
            for (j in i until list.size) {
                if (list[i].profilePath == null) {
                    if (list[j].profilePath == null) {
                        continue
                    } else {
                        val temp = list[i]
                        list[i] = list[j]
                        list[j] = temp
                    }
                }
            }
        }
        return list
    }

}