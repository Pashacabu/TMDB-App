package com.pashcabu.hw2.model

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pashcabu.hw2.model.data_classes.networkResponses.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


class NetworkModule {


    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val inter = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val contentType = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(inter)
        .build()


    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    val apiService: TMDBInterface = retrofit.create()

    interface TMDBInterface {
        @GET("movie/{movieID}")
        suspend fun getMovieDetails(
            @Path("movieID") movieID: Int,
            @Query("api_key") api_key: String
        ): MovieDetailsResponse

        @GET("movie/{endPoint}")
        suspend fun getMoviesList(
            @Path("endPoint") endpoint: String?,
            @Query("api_key") api_key: String,
            @Query("page") page: Int
        ): MovieListResponse

        @GET("genre/movie/list")
        suspend fun getGenres(@Query("api_key") api_key: String): GenresResponse

        @GET("movie/{movieID}/credits")
        suspend fun getActors(
            @Path("movieID") id: Int,
            @Query("api_key") api_key: String
        ): CastResponse

        @GET("movie/latest")
        suspend fun getLatest(@Query("api_key") api_key: String): MovieDetailsResponse

        @GET("search/movie")
        suspend fun search(
            @Query("api_key") api_key: String,
            @Query("query") str: String,
            @Query("page") page: Int
        ): MovieListResponse

        @GET("person/{personId}")
        suspend fun getPerson(
            @Path("personId") personId: Int,
            @Query("api_key") api_key: String,
            @Query("append_to_response") query: String = "images"
        ): PersonResponse

        @GET("discover/movie")
        suspend fun getPersonMovies(
            @Query("api_key") api_key: String,
            @Query("sort_by") sortBy: String = "popularity.desc",
            @Query("page") page: Int,
            @Query("with_people") personID: Int
        ): PersonsMoviesListResponse

    }

    companion object {
        const val api_key = "690d3ea0f7ef1f69512be4c95fc7a886"
        private const val baseUrl = "https://api.themoviedb.org/3/"
    }
}

object SingleNetwork {
    private val network = NetworkModule()
    val service = network.apiService
}
