package com.pashcabu.hw2.movieDetailsRecyclerView

import com.pashcabu.hw2.R

class ActorData {
    fun avengers(): List<Actor>{
        return listOf(
                Actor(R.drawable.robert_downey_jr2, R.string.robert_dw_jr ),
                Actor(R.drawable.chris_evans2, R.string.chris_evans),
                Actor(R.drawable.mark_ruffalo2, R.string.mark_ruffalo),
                Actor(R.drawable.chris_hemsworth2, R.string.chris_hemsworth),
                Actor(R.drawable.brie_larsen, R.string.brie_larsen),
                Actor(R.drawable.paul_rudd, R.string.paul_rudd),
                Actor(R.drawable.jeremy_renner, R.string.jeremy_renner),
                Actor(R.drawable.karen_gilligan, R.string.caren_gillian),
                Actor(R.drawable.tom_holland, R.string.tom_hollnd)
        )
    }
    fun tenet(): List<Actor>{
        return listOf(
                Actor(R.drawable.elozabeth_debicki, R.string.elizabeth_debicki ),
                Actor(R.drawable.robert_pattinson, R.string.robert_pattinson),
                Actor(R.drawable.john_david_washington, R.string.john_david_washington),
                Actor(R.drawable.clemence_poesy, R.string.clemence_poesy),
                Actor(R.drawable.kenneth_branagh, R.string.kenneth_branagh),
                Actor(R.drawable.aaron_taylor_johnson, R.string.aaron_taylor_johnson)

        )
    }
    fun blackWidow(): List<Actor>{
        return listOf(
                Actor(R.drawable.scarlett_johansson, R.string.scarlet_johansson ),
                Actor(R.drawable.florence_pugh, R.string.florence_pugh),
                Actor(R.drawable.robert_downey_jr2, R.string.robert_dw_jr),
                Actor(R.drawable.rachel_weisz, R.string.rachel_weisz),
                Actor(R.drawable.david_harbour, R.string.david_harbour),

        )
    }
    fun wonderWoman(): List<Actor>{
        return listOf(
                Actor(R.drawable.gal_gadot, R.string.gal_gadot ),
                Actor(R.drawable.chris_pine, R.string.chris_pine),
                Actor(R.drawable.kristen_wiig, R.string.kristen_wiig),
                Actor(R.drawable.pedro_pascal, R.string.pedro_pascal),
                Actor(R.drawable.connie_nielsen, R.string.connie_nielsen),

        )
    }
}