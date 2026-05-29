package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.example.ui.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MovieViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val useInternalPlayer by viewModel.useInternalPlayer.collectAsStateWithLifecycle()
    val streamServerUrl by viewModel.streamServerUrl.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text("App Settings", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // General Stream Playback settings
            item {
                SettingsCategorySection(title = "Stream Preferences") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setPlaybackMode(!useInternalPlayer)
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "In-App Video Player",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (useInternalPlayer) "Streams play inside the app's advanced video player." else "Streams will open in your external default browser.",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = useInternalPlayer,
                            onCheckedChange = { viewModel.setPlaybackMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Divider(color = Color(0xFF262626), thickness = 0.5.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Active Stream Provider",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        Text(
                            text = "This application uses a premium, highly stable stream host server for IMDb video playback.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "StreamIMDb Video Player",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = streamServerUrl,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Cache & History clearing operations
            item {
                SettingsCategorySection(title = "History & Memory") {
                    // Item 1: Clear Search history
                    SettingsActionRow(
                        icon = Icons.Default.Delete,
                        title = "Clear Search Queries",
                        description = "Erase all auto-fill history pills from memory.",
                        onClick = {
                            viewModel.clearRecentSearches()
                            Toast.makeText(context, "Search history emptied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("settings_clear_search")
                    )
                    Divider(color = Color(0xFF262626), thickness = 0.5.dp)

                    // Item 2: Clear Watch history from database
                    SettingsActionRow(
                        icon = Icons.Default.Delete,
                        title = "Wipe Viewed Histories",
                        description = "Delete logs of past clicking loops.",
                        onClick = {
                            viewModel.clearWatchHistory()
                            Toast.makeText(context, "Play logs wiped!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("settings_clear_history")
                    )
                    Divider(color = Color(0xFF262626), thickness = 0.5.dp)

                    // Item 3: Cache Management Coil clearder
                    SettingsActionRow(
                        icon = Icons.Default.Settings,
                        title = "Empty Coil Poster Cache",
                        description = "Flush local temporary disk files to reclaim space.",
                        onClick = {
                            ImageLoader(context).memoryCache?.clear()
                            ImageLoader(context).diskCache?.clear()
                            Toast.makeText(context, "Image posters cache flushed!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("settings_clear_cache")
                    )
                }
            }

            // Diagnostic Software parameters info card
            item {
                SettingsCategorySection(title = "General Diagnostic") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Build Version",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "PlayIMDb Premium App v1.0.0 (Production Live)",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategorySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFF5252), // Eye-catching warning red
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}
