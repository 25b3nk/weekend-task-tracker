package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving tasks by status.
 * Provides convenient methods for getting tasks from different lists.
 */
class GetTasksUseCase(private val repository: TaskRepository) {

    /**
     * Get all weekend tasks
     */
    fun getWeekendTasks(): Flow<List<Task>> {
        return repository.getWeekendTasks()
    }

    /**
     * Get all master list tasks
     */
    fun getMasterTasks(): Flow<List<Task>> {
        return repository.getMasterTasks()
    }

    /**
     * Get all completed tasks
     */
    fun getCompletedTasks(): Flow<List<Task>> {
        return repository.getCompletedTasks()
    }

    /**
     * Get tasks by specific status
     */
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> {
        return repository.getTasksByStatus(status)
    }

    /**
     * Get a single task by ID
     */
    suspend fun getTaskById(taskId: String): Task? {
        return repository.getTaskById(taskId)
    }
}
