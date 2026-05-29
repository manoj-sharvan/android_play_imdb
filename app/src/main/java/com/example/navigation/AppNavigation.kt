package com.example.navigation

import android.net.Uri
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.*
import com.example.ui.viewmodel.MovieViewModel

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAILS = "details/{movieId}"
    const val PLAYER = "player/{movieId}/{title}"
    const val WATCHLIST = "watchlist"
    const val SETTINGS = "settings"

    fun movieDetails(movieId: String): String = "details/$movieId"
    fun player(movieId: String, title: String): String {
        val encodedTitle = Uri.encode(title)
        return "player/$movieId/$encodedTitle"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MovieViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onMovieSelected = { movieId ->
                    navController.navigate(Routes.movieDetails(movieId))
                },
                onSearchClicked = {
                    navController.navigate(Routes.SEARCH)
                },
                onWatchlistClicked = {
                    navController.navigate(Routes.WATCHLIST)
                },
                onSettingsClicked = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = viewModel,
                onMovieClick = { movieId ->
                    navController.navigate(Routes.movieDetails(movieId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.DETAILS) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val useInternalPlayer by viewModel.useInternalPlayer.collectAsStateWithLifecycle()
            val streamServerUrl by viewModel.streamServerUrl.collectAsStateWithLifecycle()
            val context = LocalContext.current
            
            MovieDetailScreen(
                movieId = movieId,
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onPlayClick = { id, title ->
                    if (useInternalPlayer) {
                        navController.navigate(Routes.player(id, title))
                    } else {
                        val playUrl = streamServerUrl.replace("{id}", id)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No web browser found to run this stream.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onRecommendedClick = { recommendedId ->
                    navController.navigate(Routes.movieDetails(recommendedId))
                }
            )
        }

        composable(Routes.PLAYER) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
            val movieTitle = Uri.decode(rawTitle)
            val useInternalPlayer by viewModel.useInternalPlayer.collectAsStateWithLifecycle()
            val streamServerUrl by viewModel.streamServerUrl.collectAsStateWithLifecycle()
            val playUrl = remember(streamServerUrl, movieId) {
                streamServerUrl.replace("{id}", movieId)
            }

            PlayerScreen(
                imdbId = movieId,
                movieTitle = movieTitle,
                useInternalPlayer = useInternalPlayer,
                playUrl = playUrl,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.WATCHLIST) {
            WatchlistScreen(
                viewModel = viewModel,
                onMovieClick = { movieId ->
                    navController.navigate(Routes.movieDetails(movieId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
