package com.weekendtasks.app.data.model

import com.weekendtasks.app.data.local.TaskEntity

/**
 * Domain model for a task.
 * This is the model used throughout the app's business logic.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: TaskStatus,
    val createdDate: Long,
    val completedDate: Long? = null,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM
)

/**
 * Task status enum representing which list the task belongs to
 */
enum class TaskStatus {
    WEEKEND,    // Active tasks for current/upcoming weekend
    MASTER,     // Backlog of uncompleted tasks
    COMPLETED   // Archive of completed tasks
}

/**
 * Task priority levels
 */
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Extension function to convert TaskEntity to Task domain model
 */
fun TaskEntity.toDomainModel(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        status = status,
        createdDate = createdDate,
        completedDate = completedDate,
        dueDate = dueDate,
        dueTime = dueTime,
        priority = priority
    )
}

/**
 * Extension function to convert Task domain model to TaskEntity
 */
fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        status = status,
        createdDate = createdDate,
        completedDate = completedDate,
        dueDate = dueDate,
        dueTime = dueTime,
        priority = priority
    )
}
