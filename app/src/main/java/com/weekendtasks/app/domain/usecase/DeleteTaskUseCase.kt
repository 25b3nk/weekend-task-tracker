package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.repository.TaskRepository

/**
 * Use case for deleting a task.
 */
class DeleteTaskUseCase(private val repository: TaskRepository) {

    suspend operator fun invoke(taskId: String): Result<Unit> {
        return try {
            repository.deleteTaskById(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
