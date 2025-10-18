package com.weekendtasks.app.data.local

import androidx.room.TypeConverter
import com.weekendtasks.app.data.model.TaskPriority
import com.weekendtasks.app.data.model.TaskStatus

/**
 * Room type converters for custom types.
 * These allow Room to store enum types in the database.
 */
class Converters {

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus {
        return TaskStatus.valueOf(status)
    }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority {
        return TaskPriority.valueOf(priority)
    }
}
