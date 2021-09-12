package com.pashacabu.tmdb_app.model.data_classes

import javax.inject.Inject

data class SpinnerSorting(

    var text: String,
    var direction: Byte,
    var query: String,
    var id: Long = 0L,
)


class SortingsList {

    private val list = listOf(
        SpinnerSorting("$POPULARITY $DES_TEXT", DESCEND, POP_Q_DW),
        SpinnerSorting("$POPULARITY $ASC_TEXT", ASCEND, POP_Q_UP),
        SpinnerSorting("$RELEASE_DATE $DES_TEXT", DESCEND, REL_Q_DW),
        SpinnerSorting("$RELEASE_DATE $ASC_TEXT", ASCEND, REL_Q_UP),
        SpinnerSorting("$REVENU $DES_TEXT", DESCEND, REV_Q_DW),
        SpinnerSorting("$REVENU $ASC_TEXT", ASCEND, REV_Q_UP),
        SpinnerSorting("$TITLE $DES_TEXT", DESCEND, TIT_Q_DW),
        SpinnerSorting("$TITLE $ASC_TEXT", ASCEND, TIT_Q_UP),
        SpinnerSorting("$VOTE $DES_TEXT", DESCEND, VO_Q_DW),
        SpinnerSorting("$VOTE $ASC_TEXT", ASCEND, VO_Q_UP),

        )

    fun spinnerList(): List<SpinnerSorting> {
        val res: List<SpinnerSorting> = list
        res.forEach {
            it.id = list.indexOf(it).toLong()
        }
        return res
    }

    companion object {
        const val ASCEND: Byte = 1
        const val DESCEND: Byte = 0
        const val DES_TEXT = "Descending"
        const val ASC_TEXT = "Ascending"
        const val POPULARITY = "Popularity"
        const val POP_Q_UP = "popularity.asc"
        const val POP_Q_DW = "popularity.desc"
        const val RELEASE_DATE = "Release date"
        const val REL_Q_UP = "release_date.asc"
        const val REL_Q_DW = "release_date.desc"
        const val REVENU = "Revenu"
        const val REV_Q_UP = "revenue.asc"
        const val REV_Q_DW = "revenue.desc"
        const val TITLE = "Title"
        const val TIT_Q_UP = "original_title.asc"
        const val TIT_Q_DW = "original_title.desc"
        const val VOTE = "Vote"
        const val VO_Q_UP = "vote_average.asc"
        const val VO_Q_DW = "vote_average.desc"
    }
}