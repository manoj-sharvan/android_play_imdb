package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.WatchlistItem
import com.example.data.remote.CinemetaMeta
import com.example.ui.components.ShimmerCard
import com.example.ui.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: MovieViewModel,
    onMovieClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag("watchlist_screen"),
        topBar = {
            TopAppBar(
                title = { Text("My Watchlist", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("watchlist_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (watchlist.isEmpty()) {
                WatchlistEmptyView(onExploreClick = onBackClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(watchlist, key = { it.id }) { item ->
                        WatchlistItemCard(
                            item = item,
                            onMovieClick = onMovieClick,
                            onRemove = {
                                // Maps entities to repository remover
                                val fakeMeta = CinemetaMeta(
                                    id = item.id,
                                    name = item.name,
                                    poster = item.poster,
                                    year = item.year,
                                    type = null,
                                    background = null,
                                    description = null,
                                    imdbRating = null,
                                    runtime = null,
                                    genres = null
                                )
                                viewModel.toggleWatchlist(fakeMeta, isCurrentlyWatchlisted = true)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistItemCard(
    item: WatchlistItem,
    onMovieClick: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onMovieClick(item.id) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = item.poster,
                contentDescription = null,
                modifier = Modifier
                    .width(55.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
                loading = { ShimmerCard(modifier = Modifier.fillMaxSize()) },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF262626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎬", fontSize = 20.sp)
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.year ?: "N/A",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "IMDb ID: ${item.id}",
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.testTag("watchlist_delete_${item.id}")) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from watchlist",
                    tint = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun WatchlistEmptyView(
    onExploreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFF222222),
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Watchlist is Empty",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Bookmark films from search results and main reels to plan movie nights offline.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onExploreClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Explore Popular Releases", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
