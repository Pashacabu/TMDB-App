package com.pashcabu.hw2.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.TimeUnit
import android.os.Build
import android.util.Log
import android.util.TimeUtils
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.work.*
import com.bumptech.glide.Glide
import com.pashcabu.hw2.R
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.GenresListItem
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
import com.pashcabu.hw2.view_model.*
import com.pashcabu.hw2.views.MainActivity
import kotlinx.coroutines.*

class MyWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private val db = Database.createDB(context)
    private val network = NetworkModule().apiService
    private val converter = ClassConverter()
    private val dbHandler = DBHandler(db)
    private val array = arrayOf(NOW_PLAYING, POPULAR, TOP_RATED, UPCOMING)
    private lateinit var difference: MutableList<Movie>


    override fun doWork(): Result {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            refreshDB()
            delay(2000L)
            dbHandler.clearDBTable(NOW_PLAYING)
            delay(5000L)
            refreshDB()
        }
        return Result.success()
    }


    private suspend fun refreshDB() {
        difference = mutableListOf()
        for (endpoint in array) {
            loadFromAPI(endpoint, 1)
        }
    }

    private suspend fun loadFromAPI(endpoint: String?, currentPage: Int) {
        loadGenresFromAPI()
        loadMoviesFromAPI(endpoint, currentPage)
    }

    private fun notifyUser(difference: List<Movie?>?, notTitle: String) {
        var mostRatedMovie = Movie()
        if (difference?.isNotEmpty() == true) {
            for (movie in difference) {
                if (movie?.voteAverage ?: 0.0 >= mostRatedMovie.voteAverage ?: 0.0) {
                    if (movie != null) {
                        mostRatedMovie = movie
                    }
                }
            }
        }
        val notManager = NotificationManagerCompat.from(context)
        val channelID = context.getString(R.string.channelID)
        val name = context.getString(R.string.channelName)
        val channel = NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_HIGH)
        notManager.createNotificationChannel(channel)
        val imageBaseUrl = context.getString(R.string.baseImageURL)
        val fTarget = Glide.with(context)
            .asBitmap()
            .centerCrop()
            .load(imageBaseUrl + mostRatedMovie.posterPath)
            .submit()
        val myBitmap = fTarget.get()
        val baseURL = context.getString(R.string.deepLinkBaseURL)
        val uri = (baseURL+mostRatedMovie.id).toUri()
        val intent = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .setData(uri)
        val pendingIntent =
            PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle(notTitle)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(myBitmap))
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.icon))
            .setSmallIcon(R.drawable.icon)
            .setContentText(mostRatedMovie.title)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notManager.notify("Movie", mostRatedMovie.id ?: 0, notification)
    }

    private suspend fun loadGenresFromAPI() {
        val list: List<GenresListItem?>
        try {
            list = network.getGenres(NetworkModule.api_key).genres ?: listOf()
            dbHandler.saveGenresToDB(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadMoviesFromAPI(endpoint: String?, pageToLoad: Int) {
        try {
            val mapOfGenres = dbHandler.loadGenresFromDB()
            val newListOfMovies = network.getMoviesList(
                endpoint,
                NetworkModule.api_key,
                pageToLoad
            )
            val newListWithGenres =
                newListOfMovies.results?.map { movie ->
                    converter.genresIntToStrings(
                        movie,
                        mapOfGenres
                    )
                }
            val listOfFavourite =
                converter.entityItemsListToMovieList(
                    db.movieDAO().getListOfFavourite()
                )
            newListWithGenres?.forEach { checkIfInFavourite(it, listOfFavourite) }
            val currentListOfMovies = dbHandler.loadMoviesListFromDB(endpoint) ?: listOf()
            if (endpoint == NOW_PLAYING) {
                newListWithGenres?.map { movie ->
                    if (!checkIfContainsElement(currentListOfMovies, movie)){
                            if (movie != null) {
                                difference.add(movie)
                            }
                        }
                }
                if (difference.isEmpty()) {
                    notifyUser(
                        newListWithGenres as List<Movie>,
                        context.getString(R.string.notificationTitle_highestRating)
                    )
                } else {
                    notifyUser(difference, context.getString(R.string.notificationTitle_new))
                }
            }
            dbHandler.clearDBTable(endpoint)
            dbHandler.saveMoviesListToDB(endpoint, newListWithGenres)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkIfContainsElement(list : List<Movie?>, element : Movie?) : Boolean {
        val id = element?.id
        var result = false
        for (movie in list){
            if (movie?.id==id){
                result = true
            }
        }
        return result
    }

    private fun checkIfInFavourite(movie: Movie?, listOfFavourite: List<Movie?>): Movie? {
        val id = movie?.id
        for (item in listOfFavourite) {
            if (item?.id == id) {
                movie?.addedToFavourite = true
            }
        }
        return movie
    }
}
