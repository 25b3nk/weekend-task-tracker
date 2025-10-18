package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.data.model.TaskPriority
import com.weekendtasks.app.data.repository.TaskRepository

/**
 * Use case for updating an existing task.
 */
class UpdateTaskUseCase(private val repository: TaskRepository) {

    suspend operator fun invoke(
        taskId: String,
        title: String,
        description: String? = null,
        dueDate: Long? = null,
        dueTime: String? = null,
        priority: TaskPriority = TaskPriority.MEDIUM
    ): Result<Task> {
        return try {
            // Validate task title
            if (title.isBlank()) {
                return Result.failure(IllegalArgumentException("Task title cannot be empty"))
            }

            // Get existing task
            val existingTask = repository.getTaskById(taskId)
                ?: return Result.failure(IllegalArgumentException("Task not found"))

            // Create updated task
            val updatedTask = existingTask.copy(
                title = title.trim(),
                description = description?.trim(),
                dueDate = dueDate,
                dueTime = dueTime,
                priority = priority
            )

            // Update in repository
            repository.updateTask(updatedTask)
            Result.success(updatedTask)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
