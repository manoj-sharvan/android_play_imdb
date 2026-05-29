package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistItem(
    @PrimaryKey val id: String, // IMDb ID (e.g. tt0816692)
    val name: String,
    val poster: String?,
    val year: String?,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearch(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryItem(
    @PrimaryKey val id: String,
    val name: String,
    val poster: String?,
    val year: String?,
    val watchedAt: Long = System.currentTimeMillis()
)
