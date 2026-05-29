package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.data.remote.CinemetaMeta
import com.example.ui.components.ShimmerCard
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.viewmodel.SearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MovieViewModel,
    onMovieClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchUiState.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.testTag("search_screen"),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF121212))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.White
                        )
                    }

                    TextField(
                        value = query,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search title, actors, directors...", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .heightIn(max = 50.dp)
                            .testTag("search_text_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            disabledContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )
                }
                Divider(color = Color(0xFF1E1E1E), thickness = 1.dp)
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (searchState) {
                is SearchUiState.Idle -> {
                    if (recentSearches.isEmpty()) {
                        SearchIdleView()
                    } else {
                        RecentSearchesView(
                            queries = recentSearches,
                            onQuerySelect = { 
                                viewModel.onSearchQueryChanged(it)
                                viewModel.performSearch(it)
                                focusManager.clearFocus()
                            },
                            onQueryDelete = { viewModel.deleteRecentSearch(it) },
                            onClearAll = { viewModel.clearRecentSearches() }
                        )
                    }
                }
                is SearchUiState.Loading -> {
                    SearchLoadingShimmerList()
                }
                is SearchUiState.Success -> {
                    val movies = (searchState as SearchUiState.Success).results
                    if (movies.isEmpty()) {
                        SearchEmptyView(query)
                    } else {
                        SearchSuccessList(
                            movies = movies,
                            onMovieClick = { id ->
                                focusManager.clearFocus()
                                onMovieClick(id)
                            }
                        )
                    }
                }
                is SearchUiState.Error -> {
                    SearchErrorView(
                        message = (searchState as SearchUiState.Error).message,
                        onRetry = { viewModel.performSearch(query) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchIdleView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFF333333),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover IMDb Hits",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Type movie titles above to search streaming sources and metadata.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun RecentSearchesView(
    queries: List<com.example.data.local.RecentSearch>,
    onQuerySelect: (String) -> Unit,
    onQueryDelete: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Searches",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearAll) {
                Text("Clear All", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(queries) { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onQuerySelect(search.query) }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = search.query,
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { onQueryDelete(search.query) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete from history",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchLoadingShimmerList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(65.dp)
                        .fillMaxHeight()
                ) {
                    ShimmerCard(modifier = Modifier.fillMaxSize())
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(16.dp)
                    ) {
                        ShimmerCard(modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(12.dp)
                    ) {
                        ShimmerCard(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
fun SearchEmptyView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFF333333),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Results Found",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Could not find any movies for \"$query\". Check spelling or try popular titles.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun SearchSuccessList(
    movies: List<CinemetaMeta>,
    onMovieClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(movies, key = { it.id }) { movie ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_result_item_${movie.id}")
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onMovieClick(movie.id) }
                    .background(Color(0xFF1E1E1E))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    model = movie.poster,
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
                        text = movie.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = movie.year ?: "N/A",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "IMDb ID: ${movie.id}",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Star rating badge if available on item
                movie.imdbRating?.let { rating ->
                    if (rating.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = rating,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchErrorView(
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
            text = "Search Error Occurred",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Retry", color = Color.Black)
        }
    }
}
