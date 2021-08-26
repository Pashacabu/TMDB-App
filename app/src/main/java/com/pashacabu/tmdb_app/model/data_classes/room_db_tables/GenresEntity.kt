package com.pashacabu.tmdb_app.model.data_classes.room_db_tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
open class GenresEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey
    open var id: Int? = null,
    @ColumnInfo(name = "name")
    open var name: String? = null

)

@Entity(tableName = "Genres")
class DBGenres() : GenresEntity()