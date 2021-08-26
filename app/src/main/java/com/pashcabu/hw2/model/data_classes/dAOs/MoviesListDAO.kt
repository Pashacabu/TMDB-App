package com.pashcabu.hw2.model.data_classes.dAOs

import androidx.room.*
import com.pashcabu.hw2.model.data_classes.room_db_tables.*

@Dao
interface MoviesListDAO {

    @Insert(entity = DBNowPlaying::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNowPlaying(movies: List<MoviesListItem>)

    @Query("SELECT * FROM NowPlaying")
    suspend fun getNowPlaying(): List<MoviesListItem>

    @Query("DELETE FROM NowPlaying")
    suspend fun deleteNowPlaying()

    @Insert(entity = DBPopular::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPopular(movies: List<MoviesListItem>)

    @Query("SELECT * FROM Popular")
    suspend fun getPopular(): List<MoviesListItem>


    @Query("DELETE FROM Popular")
    suspend fun deletePopular()

    @Insert(entity = DBTopRated::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopRated(movies: List<MoviesListItem>)

    @Query("SELECT * FROM TopRated")
    suspend fun getTopRated(): List<MoviesListItem>

    @Query("DELETE FROM TopRated")
    suspend fun deleteTopRated()

    @Insert(entity = DBUpcoming::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpcoming(movies: List<MoviesListItem>)

    @Query("SELECT * FROM Upcoming")
    suspend fun getUpcoming(): List<MoviesListItem>

    @Query("DELETE FROM Upcoming")
    suspend fun deleteUpcoming()

    @Insert(entity = DBFavourite::class, onConflict = OnConflictStrategy.ABORT)
    suspend fun addToFavourite(item: MoviesListItem)

    @Query("UPDATE NowPlaying SET Favourite = :state WHERE TMDB_ID = :TMDB_ID")
    suspend fun updateNowPlaying(TMDB_ID: Int, state: Boolean)

    @Query("UPDATE Popular SET Favourite = :state WHERE TMDB_ID = :TMDB_ID")
    suspend fun updatePopular(TMDB_ID: Int, state: Boolean)

    @Query("UPDATE TopRated SET Favourite = :state WHERE TMDB_ID = :TMDB_ID")
    suspend fun updateTopRated(TMDB_ID: Int, state: Boolean)

    @Query("UPDATE Upcoming SET Favourite = :state WHERE TMDB_ID = :TMDB_ID")
    suspend fun updateUpcoming(TMDB_ID: Int, state: Boolean)

    @Query("SELECT * From ListOfFavourite")
    suspend fun getListOfFavourite(): List<MoviesListItem>

    @Query("SELECT * FROM ListOfFavourite Where TMDB_ID=:id")
    suspend fun getOneFromFavourite(id: Int): List<MoviesListItem>

    @Query("DELETE FROM ListOfFavourite WHERE TMDB_ID=:id")
    suspend fun deleteFromFavourite(id: Int)


}
