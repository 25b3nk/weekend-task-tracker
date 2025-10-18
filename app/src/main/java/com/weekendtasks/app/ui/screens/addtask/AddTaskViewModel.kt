package com.weekendtasks.app.ui.screens.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weekendtasks.app.data.model.TaskPriority
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.data.repository.TaskRepository
import com.weekendtasks.app.domain.nlp.NaturalLanguageProcessor
import com.weekendtasks.app.domain.nlp.ParsedTask
import com.weekendtasks.app.domain.usecase.AddTaskUseCase
import com.weekendtasks.app.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Add/Edit Task screen.
 * Handles NLP parsing and task creation/update.
 */
class AddTaskViewModel(
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val nlpProcessor: NaturalLanguageProcessor,
    private val repository: TaskRepository
) : ViewModel() {

    // Edit mode state
    private val _editingTaskId = MutableStateFlow<String?>(null)
    val editingTaskId: StateFlow<String?> = _editingTaskId.asStateFlow()

    val isEditMode: StateFlow<Boolean> = _editingTaskId.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    // Input text state
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Parsed task state (from NLP)
    private val _parsedTask = MutableStateFlow(ParsedTask(title = ""))
    val parsedTask: StateFlow<ParsedTask> = _parsedTask.asStateFlow()

    // Manual overrides
    private val _manualDate = MutableStateFlow<Long?>(null)
    val manualDate: StateFlow<Long?> = _manualDate.asStateFlow()

    private val _manualTime = MutableStateFlow<String?>(null)
    val manualTime: StateFlow<String?> = _manualTime.asStateFlow()

    private val _selectedPriority = MutableStateFlow(TaskPriority.MEDIUM)
    val selectedPriority: StateFlow<TaskPriority> = _selectedPriority.asStateFlow()

    private val _selectedStatus = MutableStateFlow(TaskStatus.WEEKEND)
    val selectedStatus: StateFlow<TaskStatus> = _selectedStatus.asStateFlow()

    // UI state
    private val _uiState = MutableStateFlow<AddTaskUiState>(AddTaskUiState.Idle)
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    // NLP processing state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var parseJob: Job? = null

    init {
        // Initialize NLP processor
        viewModelScope.launch {
            nlpProcessor.initialize()
        }
    }

    /**
     * Update input text and trigger NLP parsing
     */
    fun updateInputText(text: String) {
        _inputText.value = text

        // Debounce NLP parsing (wait 500ms after user stops typing)
        parseJob?.cancel()
        parseJob = viewModelScope.launch {
            delay(500)
            parseInput(text)
        }
    }

    /**
     * Parse input using NLP
     */
    private suspend fun parseInput(text: String) {
        if (text.isBlank()) {
            _parsedTask.value = ParsedTask(title = "")
            return
        }

        _isProcessing.value = true
        val result = nlpProcessor.parseTaskInput(text)
        _parsedTask.value = result
        _isProcessing.value = false
    }

    /**
     * Set manual date override
     */
    fun setManualDate(date: Long?) {
        _manualDate.value = date
    }

    /**
     * Set manual time override
     */
    fun setManualTime(time: String?) {
        _manualTime.value = time
    }

    /**
     * Set task priority
     */
    fun setPriority(priority: TaskPriority) {
        _selectedPriority.value = priority
    }

    /**
     * Set task status (which list it belongs to)
     */
    fun setStatus(status: TaskStatus) {
        _selectedStatus.value = status
    }

    /**
     * Load task for editing
     */
    fun loadTaskForEdit(taskId: String) {
        viewModelScope.launch {
            _editingTaskId.value = taskId
            val task = repository.getTaskById(taskId)

            if (task != null) {
                // Populate form with existing task data
                _inputText.value = task.title
                _selectedPriority.value = task.priority
                _selectedStatus.value = task.status
                _manualDate.value = task.dueDate
                _manualTime.value = task.dueTime

                // Set parsed task to match current values
                _parsedTask.value = ParsedTask(
                    title = task.title,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    rawInput = task.title,
                    confidence = 1.0f
                )
            }
        }
    }

    /**
     * Save the task (create or update)
     */
    fun saveTask() {
        viewModelScope.launch {
            val title = _parsedTask.value.title.ifBlank { _inputText.value }

            if (title.isBlank()) {
                _uiState.value = AddTaskUiState.Error("Task title cannot be empty")
                return@launch
            }

            _uiState.value = AddTaskUiState.Loading

            // Use manual overrides if set, otherwise use parsed values
            val dueDate = _manualDate.value ?: _parsedTask.value.dueDate
            val dueTime = _manualTime.value ?: _parsedTask.value.dueTime

            val result = if (_editingTaskId.value != null) {
                // Update existing task
                updateTaskUseCase(
                    taskId = _editingTaskId.value!!,
                    title = title,
                    description = null,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = _selectedPriority.value
                )
            } else {
                // Create new task
                addTaskUseCase(
                    title = title,
                    description = null,
                    status = _selectedStatus.value,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = _selectedPriority.value
                )
            }

            result.onSuccess {
                _uiState.value = AddTaskUiState.Success
            }.onFailure { error ->
                _uiState.value = AddTaskUiState.Error(error.message ?: "Failed to save task")
            }
        }
    }

    /**
     * Clear UI state
     */
    fun clearUiState() {
        _uiState.value = AddTaskUiState.Idle
    }

    /**
     * Reset the form
     */
    fun reset() {
        _inputText.value = ""
        _parsedTask.value = ParsedTask(title = "")
        _manualDate.value = null
        _manualTime.value = null
        _selectedPriority.value = TaskPriority.MEDIUM
        _selectedStatus.value = TaskStatus.WEEKEND
        _uiState.value = AddTaskUiState.Idle
        _editingTaskId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        nlpProcessor.cleanup()
    }
}

/**
 * UI state for the Add Task screen
 */
sealed class AddTaskUiState {
    data object Idle : AddTaskUiState()
    data object Loading : AddTaskUiState()
    data object Success : AddTaskUiState()
    data class Error(val message: String) : AddTaskUiState()
}
