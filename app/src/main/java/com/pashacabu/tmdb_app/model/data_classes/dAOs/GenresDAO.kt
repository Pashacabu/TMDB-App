package com.pashacabu.tmdb_app.model.data_classes.dAOs

import androidx.room.*
import com.pashacabu.tmdb_app.model.data_classes.room_db_tables.DBGenres
import com.pashacabu.tmdb_app.model.data_classes.room_db_tables.GenresEntity

@Dao
interface GenresDAO {
    @Insert(entity = DBGenres::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenresEntity?>)

    @Query("SELECT * FROM Genres")
    suspend fun getGenres(): List<GenresEntity>

    @Query("DELETE FROM Genres")
    suspend fun deleteGenres()
}