package com.pashcabu.hw2.model.data_classes.dAOs

import androidx.room.*
import com.pashcabu.hw2.model.data_classes.room_db_tables.DBCastItem
import com.pashcabu.hw2.model.data_classes.room_db_tables.DBCrewItem
import com.pashcabu.hw2.model.data_classes.room_db_tables.DBMovieDetails

@Dao
interface DetailsDAO {

    @Insert(entity = DBMovieDetails::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieDetails(movie: DBMovieDetails?)

    @Insert(entity = DBCastItem::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCast(list: List<DBCastItem>)

    @Query("SELECT * FROM MovieDetails WHERE TMDB_ID = :id ")
    suspend fun getMovieDetails(id: Int): DBMovieDetails

    @Query("SELECT * FROM CastDetails WHERE movieID = :id")
    suspend fun getCast(id: Int): List<DBCastItem>

    @Insert(entity = DBCrewItem::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrew(list: List<DBCrewItem>)

    @Query("SELECT * FROM CrewDetails WHERE movieID = :id")
    suspend fun getCrew(id: Int): List<DBCrewItem>

    @Query("DELETE FROM MovieDetails")
    suspend fun deleteDetails()
    @Query("DELETE FROM CastDetails")
    suspend fun deleteCastDetails()
    @Query("DELETE FROM CrewDetails")
    suspend fun deleteCrewDetails()
    @Transaction
    suspend fun deleteAllDetails(){
        deleteDetails()
        deleteCastDetails()
        deleteCrewDetails()
    }

}