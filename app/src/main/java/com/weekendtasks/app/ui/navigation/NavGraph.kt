package com.weekendtasks.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weekendtasks.app.ui.screens.addtask.AddTaskScreen
import com.weekendtasks.app.ui.screens.addtask.AddTaskViewModel
import com.weekendtasks.app.ui.screens.main.MainScreen
import com.weekendtasks.app.ui.screens.main.MainViewModel
import com.weekendtasks.app.ui.screens.statistics.StatisticsScreen
import com.weekendtasks.app.ui.screens.statistics.StatisticsViewModel

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object AddTask : Screen("add_task")
    data object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }
    data object Statistics : Screen("statistics")
}

/**
 * Navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    addTaskViewModel: AddTaskViewModel,
    statisticsViewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        // Main screen with tabs
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = mainViewModel,
                onAddTaskClick = {
                    navController.navigate(Screen.AddTask.route)
                },
                onEditTask = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }

        // Add task screen
        composable(Screen.AddTask.route) {
            // Reset form when entering add mode
            LaunchedEffect(Unit) {
                addTaskViewModel.reset()
            }

            AddTaskScreen(
                viewModel = addTaskViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit task screen (uses same screen as add task)
        composable(Screen.EditTask.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")

            // Load task data when entering edit mode
            LaunchedEffect(taskId) {
                taskId?.let {
                    addTaskViewModel.loadTaskForEdit(it)
                }
            }

            AddTaskScreen(
                viewModel = addTaskViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Statistics screen
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
