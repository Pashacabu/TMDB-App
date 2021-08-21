package com.pashacabu.tmdb_app.model

import com.pashacabu.tmdb_app.model.data_classes.networkResponses.*
import com.pashacabu.tmdb_app.model.data_classes.room_db_tables.*

class ClassConverter {
    fun movieToEntityItem(movie: Movie?): MoviesListItem {
        val item = MoviesListItem()
        item.id = movie?.id!!
        item.title = movie.title
        item.addedToFavourite = movie.addedToFavourite
        item.adult = movie.adult
        item.genres = movie.genres
        item.genreIds = movie.genreIds
        item.posterPath = movie.posterPath
        item.voteAverage = movie.voteAverage
        item.voteCount = movie.voteCount
        item.backdropPath = movie.backdropPath
        item.popularity = movie.popularity
        item.releaseDate = movie.releaseDate
        return item
    }

    fun detailsResponseToMovie(movie: MovieDetailsResponse): Movie {
        val output = Movie()
        output.id = movie.movieId
        output.addedToFavourite = movie.liked
        output.adult = movie.adult
        output.overview = movie.overview
        output.originalLanguage = movie.originalLanguage
        output.originalTitle = movie.originalTitle
        output.video = movie.video
        output.title = movie.movieTitle
        output.genreIds = movie.genres?.map { it?.genreId }
        output.genres = movie.genres?.map { it?.genreName }
        output.posterPath = movie.posterPath
        output.backdropPath = movie.backdropPath
        output.releaseDate = movie.releaseDate
        output.popularity = movie.popularity
        output.voteAverage = movie.voteAverage
        output.voteCount = movie.reviews
        return output
    }

    fun entityItemToMovie(item: MoviesListItem): Movie {
        val movie = Movie()
        movie.id = item.id
        movie.title = item.title
        movie.addedToFavourite = item.addedToFavourite
        movie.adult = item.adult
        movie.genres = item.genres
        movie.genreIds = item.genreIds
        movie.posterPath = item.posterPath
        movie.voteAverage = item.voteAverage
        movie.voteCount = item.voteCount
        movie.backdropPath = item.backdropPath
        movie.releaseDate = item.releaseDate
        movie.popularity = item.popularity
        return movie
    }

    fun movieListToEntityList(movies: List<Movie?>): List<MoviesListItem> {
        val items = mutableListOf<MoviesListItem>()
        for (movie in movies) {
            items.add(movieToEntityItem(movie))
        }
        return items
    }

    fun entityItemsListToMovieList(items: List<MoviesListItem>): List<Movie?> {
        val movies = mutableListOf<Movie>()
        for (item in items) {
            movies.add(entityItemToMovie(item))
        }
        return movies
    }

    fun genreResponseToEntity(response: GenresListItem?): GenresEntity {
        val entity = GenresEntity()
        entity.id = response?.id
        entity.name = response?.name
        return entity
    }

    fun genreEntityToResponse(entity: GenresEntity?): GenresListItem {
        val response = GenresListItem()
        response.id = entity?.id
        response.name = entity?.name
        return response
    }

    fun genresListRespToEntity(list: List<GenresListItem?>): List<GenresEntity?> {
        val output = mutableListOf<GenresEntity>()
        for (i in list) {
            genreResponseToEntity(i).let { output.add(it) }
        }
        return output
    }

    fun genresListEntityToResp(list: List<GenresEntity?>): List<GenresListItem?> {
        val output = mutableListOf<GenresListItem>()
        for (i in list) {
            genreEntityToResponse(i).let { output.add(it) }
        }
        return output
    }

    fun movieDetailsResponseToEntity(movie: MovieDetailsResponse?): DBMovieDetails {
        val output = DBMovieDetails()
        output.adult = movie?.adult
        output.backdropPath = movie?.backdropPath
        output.budget = movie?.budget
        output.homepage = movie?.homepage
        output.imdbId = movie?.imdbId
        output.movieId = movie?.movieId
        output.movieTitle = movie?.movieTitle
        output.originalLanguage = movie?.originalLanguage
        output.originalTitle = movie?.originalTitle
        output.overview = movie?.overview
        output.popularity = movie?.popularity
        output.posterPath = movie?.posterPath
        output.releaseDate = movie?.releaseDate
        output.revenue = movie?.revenue
        output.reviews = movie?.reviews
        output.runtime = movie?.runtime
        output.status = movie?.status
        output.tagline = movie?.tagline
        output.video = movie?.video
        output.voteAverage = movie?.voteAverage
        return output
    }

    fun movieDetailsEntityToResponse(movie: DBMovieDetails?): MovieDetailsResponse {
        val output = MovieDetailsResponse()
        output.adult = movie?.adult
        output.backdropPath = movie?.backdropPath
        output.budget = movie?.budget
        output.homepage = movie?.homepage
        output.imdbId = movie?.imdbId
        output.movieId = movie?.movieId
        output.movieTitle = movie?.movieTitle
        output.originalLanguage = movie?.originalLanguage
        output.originalTitle = movie?.originalTitle
        output.overview = movie?.overview
        output.popularity = movie?.popularity
        output.posterPath = movie?.posterPath
        output.releaseDate = movie?.releaseDate
        output.revenue = movie?.revenue
        output.reviews = movie?.reviews
        output.runtime = movie?.runtime
        output.status = movie?.status
        output.tagline = movie?.tagline
        output.video = movie?.video
        output.voteAverage = movie?.voteAverage

        return output
    }

    private fun castResponseToEntity(item: CastItem): DBCastItem {
        val output = DBCastItem()
        output.actorName = item.actorName
        output.actorPhoto = item.actorPhoto
        output.adult = item.adult
        output.castId = item.castId
        output.character = item.character
        output.creditId = item.creditId
        output.gender = item.gender
        output.id = item.id ?: 0
        output.knownForDepartment = item.knownForDepartment
        output.order = item.order
        output.originalName = item.originalName
        output.popularity = item.popularity
        return output
    }

    private fun castEntityToResponse(item: DBCastItem): CastItem {
        val output = CastItem()
        output.actorName = item.actorName
        output.actorPhoto = item.actorPhoto
        output.adult = item.adult
        output.castId = item.castId
        output.character = item.character
        output.creditId = item.creditId
        output.gender = item.gender
        output.id = item.id
        output.knownForDepartment = item.knownForDepartment
        output.order = item.order
        output.originalName = item.originalName
        output.popularity = item.popularity
        return output
    }

    fun castResponseListToEntityList(cast: List<CastItem?>): List<DBCastItem> {
        val output = mutableListOf<DBCastItem>()
        if (!cast.isNullOrEmpty()) {
            for (item in cast) {
                item?.let { castResponseToEntity(it) }?.let { output.add(it) }
            }
        }
        return output
    }

    fun castEntityListToResponseList(list: List<DBCastItem>): MutableList<CastItem?> {
        val output = CastResponse()
        val castList: MutableList<CastItem?> = mutableListOf<CastItem?>()
        for (item in list) {
            castList.add(castEntityToResponse(item))
        }
        output.castList = castList
        return castList
    }

    private fun crewResponseToEntity(item: CrewItem): DBCrewItem {
        val output = DBCrewItem()
        output.adult = item.adult
        output.creditId = item.creditId
        output.department = item.department
        output.gender = item.gender
        output.id = item.id ?: 0
        output.job = item.job
        output.knownForDepartment = item.knownForDepartment
        output.name = item.name
        output.originalName = item.originalName
        output.popularity = item.popularity
        output.profilePath = item.profilePath
        return output
    }

    private fun crewEntityToResponse(item: DBCrewItem): CrewItem {
        val output = CrewItem()
        output.adult = item.adult
        output.creditId = item.creditId
        output.department = item.department
        output.gender = item.gender
        output.id = item.id
        output.job = item.job
        output.knownForDepartment = item.knownForDepartment
        output.name = item.name
        output.originalName = item.originalName
        output.popularity = item.popularity
        output.profilePath = item.profilePath
        return output
    }

    fun crewResponseListToEntityList(crew: MutableList<CrewItem?>): List<DBCrewItem> {
        val output = mutableListOf<DBCrewItem>()
        if (!crew.isNullOrEmpty()) {
            for (item in crew) {
                item?.let { crewResponseToEntity(it) }?.let { output.add(it) }
            }
        }
        return output
    }

    fun crewEntityListToResponseList(list: List<DBCrewItem>): MutableList<CrewItem?> {
        val output = CastResponse()
        val crewList: MutableList<CrewItem?> = mutableListOf<CrewItem?>()
        for (item in list) {
            crewList.add(crewEntityToResponse(item))
        }
        output.crew = crewList
        return crewList
    }

    fun genresIntToStrings(item: Movie?, genresMap: Map<Int?, String?>?): Movie? {
        val ids = item?.genreIds
        val genresBasedOnIds = ids?.map { genresMap?.get(it) }
        item?.genres = genresBasedOnIds
        return item
    }

}