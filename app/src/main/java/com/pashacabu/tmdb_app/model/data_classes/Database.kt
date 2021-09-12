package com.pashacabu.tmdb_app.model.data_classes

import android.content.Context
import androidx.room.*
import androidx.room.Database
import com.pashacabu.tmdb_app.model.data_classes.dAOs.DetailsDAO
import com.pashacabu.tmdb_app.model.data_classes.dAOs.GenresDAO
import com.pashacabu.tmdb_app.model.data_classes.dAOs.MoviesListDAO
import com.pashacabu.tmdb_app.model.data_classes.room_db_tables.*

@Database(
    entities = [
        DBNowPlaying::class,
        DBPopular::class,
        DBTopRated::class,
        DBUpcoming::class,
        DBGenres::class,
        DBFavourite::class,
        DBMovieDetails::class,
        DBCastItem::class,
        DBCrewItem::class,
        DBLatestMovieDetails::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun movieDAO(): MoviesListDAO
    abstract fun genresDAO(): GenresDAO
    abstract fun detailsDAO(): DetailsDAO

    companion object {
        const val DB_NAME = "myDB"

        fun createDB(context: Context): com.pashacabu.tmdb_app.model.data_classes.Database {
            return Room.databaseBuilder(
                context,
                com.pashacabu.tmdb_app.model.data_classes.Database::class.java,
                DB_NAME
            )
                .build()
        }
    }
}