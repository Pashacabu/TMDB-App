package com.pashacabu.tmdb_app.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.pashacabu.tmdb_app.model.data_classes.Database
import javax.inject.Inject

class MyViewModelFactory {

    class MoviesListViewModelFactory @Inject constructor(
        private val db: Database,
        private val worker: WorkManager
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Database::class.java, WorkManager::class.java)
                .newInstance(db, worker)
        }
    }

    class MoviesDetailsViewModelFactory @Inject constructor (private val arg: Database) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Database::class.java).newInstance(arg)
        }
    }

    class ConnectionViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor().newInstance()
        }

    }

    class PersonViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor().newInstance()
        }

    }

}