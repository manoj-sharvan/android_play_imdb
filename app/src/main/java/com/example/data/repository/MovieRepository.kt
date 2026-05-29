package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.CinemetaApi
import com.example.data.remote.CinemetaMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MovieRepository(
    private val api: CinemetaApi,
    private val watchlistDao: WatchlistDao,
    private val recentSearchDao: RecentSearchDao,
    private val watchHistoryDao: WatchHistoryDao
) {
    // --- Remote API Calls ---

    suspend fun searchMovies(query: String): Result<List<CinemetaMeta>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchMovies(query)
            Result.success(response.metas ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularMovies(): Result<List<CinemetaMeta>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPopularMovies()
            // Stremio cinemeta returns catalog, ensure non-null list
            Result.success(response.metas ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetails(imdbId: String): Result<CinemetaMeta> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMovieDetails(imdbId)
            val meta = response.meta
            if (meta != null) {
                Result.success(meta)
            } else {
                Result.failure(Exception("Movie metadata not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Watchlist DB Operations ---

    val watchlistStream: Flow<List<WatchlistItem>> = watchlistDao.getWatchlist()

    fun isWatchlistedStream(id: String): Flow<Boolean> = watchlistDao.isWatchlisted(id)

    suspend fun addToWatchlist(item: WatchlistItem) = withContext(Dispatchers.IO) {
        watchlistDao.insert(item)
    }

    suspend fun removeFromWatchlist(id: String) = withContext(Dispatchers.IO) {
        watchlistDao.deleteById(id)
    }

    // --- Recent Searches DB Operations ---

    val recentSearchesStream: Flow<List<RecentSearch>> = recentSearchDao.getRecentSearches()

    suspend fun saveRecentSearch(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            recentSearchDao.insert(RecentSearch(query = query.trim()))
        }
    }

    suspend fun deleteRecentSearch(query: String) = withContext(Dispatchers.IO) {
        recentSearchDao.deleteByQuery(query)
    }

    suspend fun clearRecentSearches() = withContext(Dispatchers.IO) {
        recentSearchDao.clearAll()
    }

    // --- Watch History DB Operations ---

    val watchHistoryStream: Flow<List<WatchHistoryItem>> = watchHistoryDao.getWatchHistory()

    suspend fun addToHistory(item: WatchHistoryItem) = withContext(Dispatchers.IO) {
        watchHistoryDao.insert(item)
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        watchHistoryDao.clearAll()
    }
}
