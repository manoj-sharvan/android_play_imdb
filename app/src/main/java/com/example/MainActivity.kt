package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        val viewModel: com.example.ui.viewmodel.MovieViewModel = viewModel(
            factory = com.example.ui.viewmodel.MovieViewModel.provideFactory(application)
        )
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AppNavigation(
              navController = navController,
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
