package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.repository.TaskRepository

/**
 * Use case for marking a task as completed.
 * Moves the task to the COMPLETED status and sets the completion timestamp.
 */
class CompleteTaskUseCase(private val repository: TaskRepository) {

    suspend operator fun invoke(taskId: String): Result<Unit> {
        return try {
            // Verify task exists
            val task = repository.getTaskById(taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            // Complete the task
            repository.completeTask(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
