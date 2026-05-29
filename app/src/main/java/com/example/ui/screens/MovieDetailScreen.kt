package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import coil.compose.SubcomposeAsyncImage
import com.example.data.remote.CinemetaMeta
import com.example.ui.components.ShimmerCard
import com.example.ui.viewmodel.DetailUiState
import com.example.ui.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    viewModel: MovieViewModel,
    onBackClick: () -> Unit,
    onPlayClick: (String, String) -> Unit,
    onRecommendedClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailUiState.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        viewModel.loadMovieDetails(movieId)
    }

    Scaffold(
        modifier = modifier.testTag("movie_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Movie Info", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.85f),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = detailState) {
                is DetailUiState.Loading -> {
                    DetailLoadingView()
                }
                is DetailUiState.Success -> {
                    val movie = state.movie
                    val isWatchlisted by viewModel.isMovieWatchlisted(movie.id).collectAsStateWithLifecycle(initialValue = false)
                    val useInternalPlayer by viewModel.useInternalPlayer.collectAsStateWithLifecycle()
                    val streamServerUrl by viewModel.streamServerUrl.collectAsStateWithLifecycle()

                    DetailSuccessView(
                        movie = movie,
                        similar = state.similar,
                        isWatchlisted = isWatchlisted,
                        useInternalPlayer = useInternalPlayer,
                        streamServerUrl = streamServerUrl,
                        onUseInternalPlayerToggle = { viewModel.setPlaybackMode(it) },
                        onStreamServerUrlChange = { viewModel.setStreamServerUrl(it) },
                        onWatchlistToggle = { viewModel.toggleWatchlist(movie, isWatchlisted) },
                        onPlayClick = {
                            viewModel.recordPlayAction(movie)
                            onPlayClick(movie.id, movie.name)
                        },
                        onRecommendedClick = onRecommendedClick
                    )
                }
                is DetailUiState.Error -> {
                    DetailErrorView(message = state.message, onRetry = { viewModel.loadMovieDetails(movieId) })
                }
                is DetailUiState.Idle -> {
                    DetailLoadingView()
                }
            }
        }
    }
}

@Composable
fun DetailLoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            ShimmerCard(modifier = Modifier.fillMaxSize())
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(160.dp)
            ) {
                ShimmerCard(modifier = Modifier.fillMaxSize())
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(24.dp)) { ShimmerCard(modifier = Modifier.fillMaxSize()) }
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp)) { ShimmerCard(modifier = Modifier.fillMaxSize()) }
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp)) { ShimmerCard(modifier = Modifier.fillMaxSize()) }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) { ShimmerCard(modifier = Modifier.fillMaxSize()) }
    }
}

@Composable
fun DetailSuccessView(
    movie: CinemetaMeta,
    similar: List<CinemetaMeta>,
    isWatchlisted: Boolean,
    useInternalPlayer: Boolean,
    streamServerUrl: String,
    onUseInternalPlayerToggle: (Boolean) -> Unit,
    onStreamServerUrlChange: (String) -> Unit,
    onWatchlistToggle: () -> Unit,
    onPlayClick: () -> Unit,
    onRecommendedClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // High-res Background Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                AsyncImage(
                    model = movie.background ?: movie.poster,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Linear background fader shadow to merge seamlessly with body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color(0xFF121212)
                                )
                            )
                        )
                )
            }
        }

        // Meta core summary block
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-30).dp)
            ) {
                // Overlay poster
                Card(
                    modifier = Modifier
                        .width(105.dp)
                        .height(150.dp)
                        .testTag("detail_movie_poster"),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = movie.poster,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = { ShimmerCard(modifier = Modifier.fillMaxSize()) }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Movie Meta details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = movie.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${movie.year ?: "N/A"}  •  ${movie.runtime ?: "N/A"}",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    movie.imdbRating?.let { rating ->
                        if (rating.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "IMDb Rating",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = rating,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "/10",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "IMDb: ${movie.id}",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Play and Bookmark primary actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-10).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_play_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play on IMDb",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onWatchlistToggle,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("detail_watchlist_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isWatchlisted) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isWatchlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Watchlist status toggle",
                        tint = if (isWatchlisted) MaterialTheme.colorScheme.primary else Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isWatchlisted) "Watchlisted" else "Watchlist",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Stream Server Info card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("detail_stream_server_box")
            ) {
                Text(
                    text = "Stream Source",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                val streamHost = remember(streamServerUrl) {
                    try {
                        android.net.Uri.parse(streamServerUrl).host ?: "streamimdb.ru"
                    } catch (e: Exception) {
                        "streamimdb.ru"
                    }
                }
                Text(
                    text = if (streamHost.contains("streamimdb")) "StreamIMDb Video Player" else "PlayIMDb Video Player",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Source: $streamServerUrl",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        // Plot description synopsis
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Storyline Plot",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = movie.description?.ifBlank { null } ?: "No Plot Synopsis is currently available for this movie title. Use 'Play on IMDb' to query fully updated stream logs or watch summaries.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // Genres cards list
        movie.genres?.let { genreList ->
            if (genreList.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Genres & Categorization",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(genreList) { item ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E1E1E))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = item,
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recommendations Similar Section
        if (similar.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                        Text(
                            text = "You May Also Like",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp)
                        )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(similar, key = { "similar_${it.id}" }) { item ->
                            Column(
                                modifier = Modifier
                                    .width(95.dp)
                                    .clickable { onRecommendedClick(item.id) }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(135.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = item.poster,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = { ShimmerCard(modifier = Modifier.fillMaxSize()) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Failed to load movie info",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Retry Connection", color = Color.Black)
        }
    }
}
