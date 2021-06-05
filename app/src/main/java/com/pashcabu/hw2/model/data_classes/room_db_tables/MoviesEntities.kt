package com.pashcabu.hw2.model.data_classes.room_db_tables

import androidx.room.*

@Entity(primaryKeys =  ["roomID", "TMDB_ID"])
open class EntityItem(
//    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "roomID")
    open var roomID : Int=0,
    @ColumnInfo(name = "Title")
    open var title: String? = null,
    @TypeConverters(Converters::class)
    open var genreIds: List<Int?>? = null,
    @ColumnInfo(name = "Genres")
    @TypeConverters(Converters::class)
    open var genres: List<String?>? = listOf(),
    @ColumnInfo(name = "Poster")
    open var posterPath: String? = null,
	open var backdropPath: String? = null,
	open var releaseDate: String? = null,
	open var popularity: Double? = null,
    @ColumnInfo(name = "VoteAVR")
    open var voteAverage: Double? = null,
//    @PrimaryKey
    @ColumnInfo(name = "TMDB_ID")
    open var id: Int = 0,
    @ColumnInfo(name = "Adult")
    open var adult: Boolean? = null,
    @ColumnInfo(name = "VoteCount")
    open var voteCount: Int? = null,
    @ColumnInfo(name = "Favourite")
    open var addedToFavourite: Boolean = false
){

}

@Entity(tableName = "NowPlaying")
class DBNowPlaying() : EntityItem()
@Entity(tableName = "Popular")
class DBPopular() : EntityItem()
@Entity(tableName = "TopRated")
class DBTopRated() : EntityItem()
@Entity(tableName = "Upcoming")
class DBUpcoming() : EntityItem()

@Entity(tableName = "ListOfFavourite")
class DBFavourite() :EntityItem()

class Converters {

    @TypeConverter
    fun genresListToString(list: List<String?>?): String? {
        return if (list == null) null else {
            return list.joinToString(",")
        }
    }

    @TypeConverter
    fun genresStringToList(string: String?): List<String?>? {
        return string?.split(",")
    }

    @TypeConverter
    fun intListToString( list : List<Int?>?) : String?{
        return list?.joinToString("/")
    }

    @TypeConverter
    fun stringToListOfInt( str: String?) : List<Int?> {
        val result= mutableListOf<Int?>()
        if (!str.isNullOrEmpty()){
            val list= str.split("/")
            if (!list.isNullOrEmpty()) {
                for (i in list.indices){
                    result.add(list[i].toInt())
                }
            }
        }

        return result
    }
}