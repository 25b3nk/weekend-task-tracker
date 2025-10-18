package com.weekendtasks.app.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.weekendtasks.app.ui.screens.completed.CompletedScreen
import com.weekendtasks.app.ui.screens.master.MasterListScreen
import com.weekendtasks.app.ui.screens.weekend.WeekendScreen

/**
 * Main screen with three tabs: Weekend, Master List, and Completed.
 * Displays tasks based on the selected tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddTaskClick: () -> Unit,
    onEditTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekendTasks by viewModel.weekendTasks.collectAsState()
    val masterTasks by viewModel.masterTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is MainUiState.Success -> {
                snackbarHostState.showSnackbar((uiState as MainUiState.Success).message)
                viewModel.clearUiState()
            }
            is MainUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as MainUiState.Error).message)
                viewModel.clearUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekend Task Tracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab row
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Weekend") },
                        icon = { Icon(Icons.Default.Weekend, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Master List") },
                        icon = { Icon(Icons.Default.List, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Completed") },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                    )
                }

                // Tab content
                when (selectedTabIndex) {
                    0 -> WeekendScreen(
                        tasks = weekendTasks,
                        onCompleteTask = viewModel::completeTask,
                        onEditTask = { onEditTask(it.id) },
                        onDeleteTask = viewModel::deleteTask,
                        onMoveToMaster = viewModel::moveToMaster
                    )
                    1 -> MasterListScreen(
                        tasks = masterTasks,
                        onCompleteTask = viewModel::completeTask,
                        onEditTask = { onEditTask(it.id) },
                        onDeleteTask = viewModel::deleteTask,
                        onMoveToWeekend = viewModel::moveToWeekend
                    )
                    2 -> CompletedScreen(
                        tasks = completedTasks,
                        onEditTask = { onEditTask(it.id) },
                        onDeleteTask = viewModel::deleteTask,
                        onMoveToWeekend = viewModel::moveToWeekend,
                        onMoveToMaster = viewModel::moveToMaster
                    )
                }
            }
        }
    }
}

// Import for Column
import androidx.compose.foundation.layout.Column
