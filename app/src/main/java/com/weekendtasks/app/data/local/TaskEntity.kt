package com.weekendtasks.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.data.model.TaskPriority
import java.util.UUID

/**
 * Room entity representing a task in the database.
 * This is the local data model used for offline storage.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,

    val description: String? = null,

    val status: TaskStatus,

    val createdDate: Long = System.currentTimeMillis(),

    val completedDate: Long? = null,

    val dueDate: Long? = null,

    val dueTime: String? = null, // Stored as HH:mm format

    val priority: TaskPriority = TaskPriority.MEDIUM
)
