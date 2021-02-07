package com.pashcabu.hw2.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pashcabu.hw2.model.data_classes.Database

class MyViewModelFactory {

    class MoviesListViewModelFactory(private val arg: Database) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Database::class.java).newInstance(arg)
        }
    }

    class MoviesDetailsViewModelFactory(private val arg: Database) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Database::class.java).newInstance(arg)
        }

    }

}