package com.weekendtasks.app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weekendtasks.app.data.model.TaskStatistics
import com.weekendtasks.app.domain.usecase.GetStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Statistics screen.
 * Manages task statistics and calculation of various metrics.
 */
class StatisticsViewModel(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    // Statistics state
    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    /**
     * Load statistics from the repository
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            getStatisticsUseCase()
                .onSuccess { statistics ->
                    _uiState.value = StatisticsUiState.Success(statistics)
                }
                .onFailure { error ->
                    _uiState.value = StatisticsUiState.Error(
                        error.message ?: "Failed to load statistics"
                    )
                }
        }
    }

    /**
     * Refresh statistics manually
     */
    fun refresh() {
        loadStatistics()
    }
}

/**
 * UI state for statistics screen
 */
sealed class StatisticsUiState {
    data object Loading : StatisticsUiState()
    data class Success(val statistics: TaskStatistics) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}
