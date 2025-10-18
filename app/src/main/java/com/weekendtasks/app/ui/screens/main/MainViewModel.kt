package com.weekendtasks.app.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen.
 * Manages task lists for all three tabs: Weekend, Master, and Completed.
 */
class MainViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    // Weekend tasks state
    val weekendTasks: StateFlow<List<Task>> = getTasksUseCase.getWeekendTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Master list tasks state
    val masterTasks: StateFlow<List<Task>> = getTasksUseCase.getMasterTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Completed tasks state
    val completedTasks: StateFlow<List<Task>> = getTasksUseCase.getCompletedTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Complete a task (mark as done)
     */
    fun completeTask(task: Task) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            completeTaskUseCase(task.id)
                .onSuccess {
                    _uiState.value = MainUiState.Success("Task completed")
                }
                .onFailure { error ->
                    _uiState.value = MainUiState.Error(error.message ?: "Failed to complete task")
                }
        }
    }

    /**
     * Move task to Weekend list
     */
    fun moveToWeekend(task: Task) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            moveTaskUseCase(task.id, TaskStatus.WEEKEND)
                .onSuccess {
                    _uiState.value = MainUiState.Success("Moved to Weekend")
                }
                .onFailure { error ->
                    _uiState.value = MainUiState.Error(error.message ?: "Failed to move task")
                }
        }
    }

    /**
     * Move task to Master list
     */
    fun moveToMaster(task: Task) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            moveTaskUseCase(task.id, TaskStatus.MASTER)
                .onSuccess {
                    _uiState.value = MainUiState.Success("Moved to Master List")
                }
                .onFailure { error ->
                    _uiState.value = MainUiState.Error(error.message ?: "Failed to move task")
                }
        }
    }

    /**
     * Delete a task
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            deleteTaskUseCase(task.id)
                .onSuccess {
                    _uiState.value = MainUiState.Success("Task deleted")
                }
                .onFailure { error ->
                    _uiState.value = MainUiState.Error(error.message ?: "Failed to delete task")
                }
        }
    }

    /**
     * Clear UI state
     */
    fun clearUiState() {
        _uiState.value = MainUiState.Idle
    }
}

/**
 * UI state for the main screen
 */
sealed class MainUiState {
    data object Idle : MainUiState()
    data object Loading : MainUiState()
    data class Success(val message: String) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
