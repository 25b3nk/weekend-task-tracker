package com.weekendtasks.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.weekendtasks.app.WeekendTaskApp
import com.weekendtasks.app.data.repository.TaskRepository
import com.weekendtasks.app.domain.nlp.NaturalLanguageProcessor
import com.weekendtasks.app.domain.usecase.*
import com.weekendtasks.app.ui.screens.addtask.AddTaskViewModel
import com.weekendtasks.app.ui.screens.main.MainViewModel

/**
 * Factory for creating ViewModels with dependencies.
 * Simple dependency injection pattern without using Hilt.
 */
class ViewModelFactory(
    private val app: WeekendTaskApp
) : ViewModelProvider.Factory {

    private val repository: TaskRepository by lazy {
        TaskRepository(app.database.taskDao())
    }

    private val getTasksUseCase: GetTasksUseCase by lazy {
        GetTasksUseCase(repository)
    }

    private val addTaskUseCase: AddTaskUseCase by lazy {
        AddTaskUseCase(repository)
    }

    private val completeTaskUseCase: CompleteTaskUseCase by lazy {
        CompleteTaskUseCase(repository)
    }

    private val moveTaskUseCase: MoveTaskUseCase by lazy {
        MoveTaskUseCase(repository)
    }

    private val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(repository)
    }

    private val updateTaskUseCase: UpdateTaskUseCase by lazy {
        UpdateTaskUseCase(repository)
    }

    private val nlpProcessor: NaturalLanguageProcessor by lazy {
        NaturalLanguageProcessor()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(
                    getTasksUseCase,
                    completeTaskUseCase,
                    moveTaskUseCase,
                    deleteTaskUseCase
                ) as T
            }
            modelClass.isAssignableFrom(AddTaskViewModel::class.java) -> {
                AddTaskViewModel(
                    addTaskUseCase,
                    updateTaskUseCase,
                    nlpProcessor,
                    repository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
