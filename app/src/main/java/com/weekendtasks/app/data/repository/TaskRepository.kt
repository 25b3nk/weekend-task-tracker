package com.weekendtasks.app.data.repository

import com.weekendtasks.app.data.local.TaskDao
import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.data.model.toDomainModel
import com.weekendtasks.app.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository class that provides a clean API for data access.
 * This class abstracts the data layer from the rest of the app.
 *
 * Following the Repository pattern, this class:
 * - Provides a single source of truth for task data
 * - Handles data mapping between entity and domain models
 * - Manages offline-first data storage using Room
 */
class TaskRepository(private val taskDao: TaskDao) {

    /**
     * Get all weekend tasks as a Flow
     */
    fun getWeekendTasks(): Flow<List<Task>> {
        return taskDao.getWeekendTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Get all master list tasks as a Flow
     */
    fun getMasterTasks(): Flow<List<Task>> {
        return taskDao.getMasterTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Get all completed tasks as a Flow
     */
    fun getCompletedTasks(): Flow<List<Task>> {
        return taskDao.getCompletedTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Get tasks by status
     */
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTasksByStatus(status).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Get a single task by ID
     */
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toDomainModel()
    }

    /**
     * Insert a new task
     */
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    /**
     * Insert multiple tasks
     */
    suspend fun insertTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks.map { it.toEntity() })
    }

    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    /**
     * Delete a task
     */
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }

    /**
     * Delete a task by ID
     */
    suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    /**
     * Move a task to a different list (change status)
     */
    suspend fun moveTask(taskId: String, newStatus: TaskStatus) {
        taskDao.updateTaskStatus(taskId, newStatus)
    }

    /**
     * Mark a task as completed
     */
    suspend fun completeTask(taskId: String) {
        taskDao.completeTask(taskId, System.currentTimeMillis())
    }

    /**
     * Get count of tasks by status
     */
    suspend fun getTaskCountByStatus(status: TaskStatus): Int {
        return taskDao.getTaskCountByStatus(status)
    }

    /**
     * Get all uncompleted weekend tasks (for Monday reminder feature)
     */
    suspend fun getUncompletedWeekendTasks(): List<Task> {
        return taskDao.getUncompletedWeekendTasks().map { it.toDomainModel() }
    }

    /**
     * Clear all completed tasks (archive cleanup)
     */
    suspend fun clearCompletedTasks() {
        taskDao.deleteAllCompletedTasks()
    }

    /**
     * Get all tasks for statistics calculation
     */
    suspend fun getAllTasks(): List<Task> {
        return taskDao.getAllTasks().map { it.toDomainModel() }
    }
}
