package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.data.model.TaskPriority
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.data.repository.TaskRepository
import com.weekendtasks.app.notifications.ReminderScheduler
import java.util.UUID

/**
 * Use case for adding a new task.
 * Encapsulates business logic for task creation.
 */
class AddTaskUseCase(
    private val repository: TaskRepository,
    private val reminderScheduler: ReminderScheduler
) {

    suspend operator fun invoke(
        title: String,
        description: String? = null,
        status: TaskStatus = TaskStatus.WEEKEND,
        dueDate: Long? = null,
        dueTime: String? = null,
        priority: TaskPriority = TaskPriority.MEDIUM
    ): Result<Task> {
        return try {
            // Validate task title
            if (title.isBlank()) {
                return Result.failure(IllegalArgumentException("Task title cannot be empty"))
            }

            // Create new task
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                description = description?.trim(),
                status = status,
                createdDate = System.currentTimeMillis(),
                completedDate = null,
                dueDate = dueDate,
                dueTime = dueTime,
                priority = priority
            )

            // Save to repository
            repository.insertTask(task)

            // Schedule reminder if task has due date and time
            if (task.dueDate != null && task.dueTime != null) {
                reminderScheduler.scheduleReminder(task)
            }

            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
