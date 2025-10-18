package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.data.repository.TaskRepository

/**
 * Use case for moving a task between lists (changing status).
 * Handles moving tasks between WEEKEND, MASTER, and COMPLETED lists.
 */
class MoveTaskUseCase(private val repository: TaskRepository) {

    suspend operator fun invoke(taskId: String, newStatus: TaskStatus): Result<Unit> {
        return try {
            // Verify task exists
            val task = repository.getTaskById(taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            // Prevent moving already completed tasks back to other lists
            // (use uncomplete operation for that if needed)
            if (task.status == TaskStatus.COMPLETED && newStatus != TaskStatus.COMPLETED) {
                // Allow uncompleting tasks
                repository.moveTask(taskId, newStatus)
                return Result.success(Unit)
            }

            // Move the task
            repository.moveTask(taskId, newStatus)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
