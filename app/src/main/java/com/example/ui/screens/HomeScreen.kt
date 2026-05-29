package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.remote.CinemetaMeta
import com.example.ui.components.MovieCard
import com.example.ui.components.ShimmerCard
import com.example.ui.viewmodel.HomeUiState
import com.example.ui.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieSelected: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onWatchlistClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(homeState) {
        if (homeState !is HomeUiState.Loading) {
            isRefreshing = false
        }
    }

    val genres = listOf("All", "Action", "Drama", "Sci-Fi", "Adventure", "Thriller", "Horror", "Comedy")

    Scaffold(
        modifier = modifier.testTag("home_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Play",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "IMDb",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSearchClicked,
                        modifier = Modifier.testTag("action_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search movies",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = onWatchlistClicked,
                        modifier = Modifier.testTag("action_watchlist")
                    ) {
                        // Watchlist icon drawing from standard core icon
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "My Watchlist",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onSettingsClicked,
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        // Custom system settings trigger
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "App Settings",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchPopularMovies()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
            // Genre Selector Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres) { genre ->
                        val isSelected = selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectGenre(genre) },
                            label = { Text(genre) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF1E1E1E),
                                labelColor = Color.LightGray,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.Black
                            ),
                            border = null
                        )
                    }
                }
            }

            // High-fidelity Trending Hero banner/Carousel
            item {
                when (homeState) {
                    is HomeUiState.Success -> {
                        val popularMovies = (homeState as HomeUiState.Success).popular
                        if (popularMovies.isNotEmpty()) {
                            val heroMovie = popularMovies.first()
                            TrendingHeroBanner(
                                movie = heroMovie,
                                onPlayClick = {
                                    viewModel.recordPlayAction(heroMovie)
                                    onMovieSelected(heroMovie.id)
                                },
                                onDetailsClick = { onMovieSelected(heroMovie.id) }
                            )
                        }
                    }
                    is HomeUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            ShimmerCard(modifier = Modifier.fillMaxSize())
                        }
                    }
                    else -> Spacer(modifier = Modifier.height(1.dp))
                }
            }

            // Recent Searches Row (Dynamic context check)
            if (recentSearches.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Recently Searched",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentSearches) { search ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E1E1E))
                                        .clickable { viewModel.performSearch(search.query); onSearchClicked() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = search.query,
                                        color = Color.LightGray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Popular Grid / Horizontal lists
            item {
                Text(
                    text = if (selectedGenre == "All") "Popular Blockbusters" else "Top in $selectedGenre",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
                )
            }

            when (homeState) {
                is HomeUiState.Success -> {
                    val movies = (homeState as HomeUiState.Success).popular
                    if (movies.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No movies in this genre, try another!",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Chunk the list to represent a beautiful modern grid inside LazyColumn safely
                        val itemsPerRow = 2
                        val rowCount = (movies.size + itemsPerRow - 1) / itemsPerRow
                        items(rowCount) { rowIndex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (columnIndex in 0 until itemsPerRow) {
                                    val itemIndex = rowIndex * itemsPerRow + columnIndex
                                    if (itemIndex < movies.size) {
                                        MovieCard(
                                            movie = movies[itemIndex],
                                            onMovieClick = onMovieSelected,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                is HomeUiState.Loading -> {
                    items(4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).height(240.dp)) {
                                ShimmerCard(modifier = Modifier.fillMaxSize())
                            }
                            Box(modifier = Modifier.weight(1f).height(240.dp)) {
                                ShimmerCard(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
                is HomeUiState.Error -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Unable to connect to Cinemeta",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = (homeState as HomeUiState.Error).message,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.fetchPopularMovies() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Retry Connection", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun TrendingHeroBanner(
    movie: CinemetaMeta,
    onPlayClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("trending_hero_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.background ?: movie.poster,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Premium cinema gradient shadow fading upwards from the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
            // Rating tag top right corner
            movie.imdbRating?.let { rating ->
                if (rating.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rating,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Details and actions absolute bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "No. 1 Trending Today",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = movie.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Play Now",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDetailsClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Info Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
