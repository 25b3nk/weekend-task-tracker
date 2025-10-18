package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.repository.TaskRepository
import com.weekendtasks.app.notifications.ReminderScheduler

/**
 * Use case for deleting a task.
 */
class DeleteTaskUseCase(
    private val repository: TaskRepository,
    private val reminderScheduler: ReminderScheduler
) {

    suspend operator fun invoke(taskId: String): Result<Unit> {
        return try {
            // Cancel any scheduled reminder
            reminderScheduler.cancelReminder(taskId)

            // Delete the task
            repository.deleteTaskById(taskId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
