package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.remote.CinemetaApi
import com.example.data.repository.MovieRepository

class PlayImdbApplication : Application() {
    lateinit var movieRepository: MovieRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        val api = CinemetaApi.create()
        movieRepository = MovieRepository(
            api = api,
            watchlistDao = database.watchlistDao(),
            recentSearchDao = database.recentSearchDao(),
            watchHistoryDao = database.watchHistoryDao()
        )
    }
}
