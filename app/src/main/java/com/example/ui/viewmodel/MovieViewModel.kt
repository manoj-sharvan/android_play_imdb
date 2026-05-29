package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.PlayImdbApplication
import com.example.data.local.WatchHistoryItem
import com.example.data.local.WatchlistItem
import com.example.data.remote.CinemetaMeta
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val popular: List<CinemetaMeta>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<CinemetaMeta>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

sealed interface DetailUiState {
    object Idle : DetailUiState
    object Loading : DetailUiState
    data class Success(val movie: CinemetaMeta, val similar: List<CinemetaMeta>) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class MovieViewModel(
    private val repository: MovieRepository,
    private val application: Application
) : ViewModel() {

    private val prefs = application.getSharedPreferences("play_imdb_prefs", android.content.Context.MODE_PRIVATE)

    // --- Home States ---
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>("All")
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    // --- Search States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    val recentSearches = repository.recentSearchesStream
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Detail States ---
    private val _detailUiState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detailUiState: StateFlow<DetailUiState> = _detailUiState.asStateFlow()

    // --- Watchlist States ---
    val watchlist = repository.watchlistStream
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Watch History States ---
    val watchHistory = repository.watchHistoryStream
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Playback Preferences State ---
    private val _useInternalPlayer = MutableStateFlow(prefs.getBoolean("use_internal_player", true))
    val useInternalPlayer: StateFlow<Boolean> = _useInternalPlayer.asStateFlow()

    private val _streamServerUrl = MutableStateFlow(prefs.getString("stream_server_url", "https://streamimdb.ru/embed/movie/{id}") ?: "https://streamimdb.ru/embed/movie/{id}")
    val streamServerUrl: StateFlow<String> = _streamServerUrl.asStateFlow()

    // Fully cached movies list from popular for filtering
    private var allPopularMovies: List<CinemetaMeta> = emptyList()

    init {
        // Enforce/Upgrade in-app player defaults to ensure seamless stream playback
        val upgradeInitKey = "internal_player_v5_force_internal_v2"
        if (!prefs.getBoolean(upgradeInitKey, false)) {
            setPlaybackMode(true)
            setStreamServerUrl("https://streamimdb.ru/embed/movie/{id}")
            prefs.edit().putBoolean(upgradeInitKey, true).apply()
        } else {
            if (!prefs.contains("use_internal_player")) {
                setPlaybackMode(true)
            }
            if (!prefs.contains("stream_server_url")) {
                setStreamServerUrl("https://streamimdb.ru/embed/movie/{id}")
            }
        }
        fetchPopularMovies()
        observeSearchQuery()
    }

    // --- Home API Logic ---
    fun fetchPopularMovies() {
        viewModelScope.launch {
            _homeUiState.value = HomeUiState.Loading
            repository.getPopularMovies()
                .onSuccess { movies ->
                    allPopularMovies = movies
                    filterByGenre(_selectedGenre.value)
                }
                .onFailure { error ->
                    _homeUiState.value = HomeUiState.Error(error.localizedMessage ?: "Failed to fetch popular movies")
                }
        }
    }

    fun selectGenre(genre: String?) {
        _selectedGenre.value = genre
        filterByGenre(genre)
    }

    private fun filterByGenre(genre: String?) {
        if (genre == null || genre == "All") {
            _homeUiState.value = HomeUiState.Success(allPopularMovies)
        } else {
            val filtered = allPopularMovies.filter { movie ->
                movie.genres?.any { it.equals(genre, ignoreCase = true) } == true
            }
            if (filtered.isEmpty() && allPopularMovies.isNotEmpty()) {
                // Return some items as fallback
                _homeUiState.value = HomeUiState.Success(allPopularMovies.shuffled().take(6))
            } else {
                _homeUiState.value = HomeUiState.Success(filtered)
            }
        }
    }

    // --- Search Debounced API ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchUiState.value = SearchUiState.Idle
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchUiState.value = SearchUiState.Idle
                    } else if (query.trim().length >= 2) {
                        performSearch(query.trim())
                    }
                }
        }
    }

    fun performSearch(query: String) {
        viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            repository.saveRecentSearch(query)
            repository.searchMovies(query)
                .onSuccess { results ->
                    _searchUiState.value = SearchUiState.Success(results)
                }
                .onFailure { error ->
                    _searchUiState.value = SearchUiState.Error(error.localizedMessage ?: "Search failed")
                }
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    // --- Movie details API ---
    fun loadMovieDetails(imdbId: String) {
        viewModelScope.launch {
            _detailUiState.value = DetailUiState.Loading
            repository.getMovieDetails(imdbId)
                .onSuccess { movie ->
                    // To suggest similar movies, find movies in popular list matching primary genre
                    val primaryGenre = movie.genres?.firstOrNull()
                    val similar = if (primaryGenre != null) {
                        allPopularMovies.filter { it.id != imdbId && it.genres?.contains(primaryGenre) == true }.take(8)
                    } else {
                        emptyList()
                    }
                    val finalSimilar = similar.ifEmpty {
                        allPopularMovies.filter { it.id != imdbId }.shuffled().take(6)
                    }
                    _detailUiState.value = DetailUiState.Success(movie, finalSimilar)
                }
                .onFailure { error ->
                    _detailUiState.value = DetailUiState.Error(error.localizedMessage ?: "Failed to find movie details")
                }
        }
    }

    // --- Watchlist interactions ---
    fun isMovieWatchlisted(id: String): Flow<Boolean> {
        return repository.isWatchlistedStream(id)
    }

    fun toggleWatchlist(movie: CinemetaMeta, isCurrentlyWatchlisted: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyWatchlisted) {
                repository.removeFromWatchlist(movie.id)
            } else {
                repository.addToWatchlist(
                    WatchlistItem(
                        id = movie.id,
                        name = movie.name,
                        poster = movie.poster,
                        year = movie.year
                    )
                )
            }
        }
    }

    // --- Watch History ---
    fun recordPlayAction(movie: CinemetaMeta) {
        viewModelScope.launch {
            repository.addToHistory(
                WatchHistoryItem(
                    id = movie.id,
                    name = movie.name,
                    poster = movie.poster,
                    year = movie.year
                )
            )
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    // --- Settings Preferences ---
    fun setPlaybackMode(useInternal: Boolean) {
        _useInternalPlayer.value = useInternal
        prefs.edit().putBoolean("use_internal_player", useInternal).apply()
    }

    fun setStreamServerUrl(url: String) {
        _streamServerUrl.value = url
        prefs.edit().putString("stream_server_url", url).apply()
    }

    // --- ViewModel Provider Factory ---
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = (application as PlayImdbApplication).movieRepository
                return MovieViewModel(repo, application) as T
            }
        }
    }
}
