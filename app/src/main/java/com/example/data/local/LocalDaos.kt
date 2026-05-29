package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getWatchlist(): Flow<List<WatchlistItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :id)")
    fun isWatchlisted(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistItem)

    @Query("DELETE FROM watchlist WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: RecentSearch)

    @Query("DELETE FROM recent_searches WHERE `query` = :query")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 20")
    fun getWatchHistory(): Flow<List<WatchHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchHistoryItem)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
