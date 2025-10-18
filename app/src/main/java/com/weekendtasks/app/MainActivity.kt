package com.weekendtasks.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.weekendtasks.app.di.ViewModelFactory
import com.weekendtasks.app.ui.navigation.NavGraph
import com.weekendtasks.app.ui.screens.addtask.AddTaskViewModel
import com.weekendtasks.app.ui.screens.main.MainViewModel
import com.weekendtasks.app.ui.theme.WeekendTaskTrackerTheme

/**
 * Main Activity for the Weekend Task Tracker app.
 * Sets up the Compose UI with navigation and dependency injection.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeekendTaskTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModelFactory = ViewModelFactory(application as WeekendTaskApp)

                    // Create ViewModels
                    val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)
                    val addTaskViewModel: AddTaskViewModel = viewModel(factory = viewModelFactory)

                    // Set up navigation
                    NavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        addTaskViewModel = addTaskViewModel
                    )
                }
            }
        }
    }
}
