package com.pashacabu.tmdb_app.di

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import androidx.work.WorkManager
import com.pashacabu.tmdb_app.model.ClassConverter
import com.pashacabu.tmdb_app.model.ConnectionChecker
import com.pashacabu.tmdb_app.model.DBHandler
import com.pashacabu.tmdb_app.model.NetworkModule
import com.pashacabu.tmdb_app.model.data_classes.Database
import com.pashacabu.tmdb_app.views.ViewPagerFragment
import com.pashacabu.tmdb_app.views.adapters.ScreenSlideAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMyDB(
        @ApplicationContext context : Context
    )= Database.createDB(context)

    @Singleton
    @Provides
    fun provideDBHandler(
        db : Database,
        converter: ClassConverter
    ) = DBHandler(db, converter)

    @Singleton
    @Provides
    fun provideWorker(
        @ApplicationContext context: Context
    ) = WorkManager.getInstance(context)

    @Singleton
    @Provides
    fun provideNetwork() = NetworkModule().apiService

    @Singleton
    @Provides
    fun provideConnectionChecker (
        @ApplicationContext context: Context
    ) = ConnectionChecker(context)




}