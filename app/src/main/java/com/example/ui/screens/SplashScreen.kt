package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .testTag("splash_screen")
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)), // Immersive movie-theater black
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)) +
                    slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(durationMillis = 1000)
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Customized IMDb-style tickets brand emblem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Play",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "IMDb",
                            color = Color.Black,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "CINEMATIC STREAMING SEARCH",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
