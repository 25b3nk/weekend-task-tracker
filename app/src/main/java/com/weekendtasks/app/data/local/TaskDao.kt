package com.weekendtasks.app.data.local

import androidx.room.*
import com.weekendtasks.app.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task operations.
 * Provides methods for CRUD operations on tasks.
 */
@Dao
interface TaskDao {

    /**
     * Get all tasks with a specific status as a Flow for reactive updates
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC, createdDate DESC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>>

    /**
     * Get all weekend tasks
     */
    @Query("SELECT * FROM tasks WHERE status = 'WEEKEND' ORDER BY dueDate ASC, createdDate DESC")
    fun getWeekendTasks(): Flow<List<TaskEntity>>

    /**
     * Get all master list tasks
     */
    @Query("SELECT * FROM tasks WHERE status = 'MASTER' ORDER BY priority DESC, createdDate DESC")
    fun getMasterTasks(): Flow<List<TaskEntity>>

    /**
     * Get all completed tasks
     */
    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completedDate DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    /**
     * Get a single task by ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    /**
     * Insert a new task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    /**
     * Insert multiple tasks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    /**
     * Update an existing task
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Delete a task
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Delete a task by ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    /**
     * Update task status (for moving between lists)
     */
    @Query("UPDATE tasks SET status = :newStatus WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, newStatus: TaskStatus)

    /**
     * Mark task as completed
     */
    @Query("UPDATE tasks SET status = 'COMPLETED', completedDate = :completedDate WHERE id = :taskId")
    suspend fun completeTask(taskId: String, completedDate: Long)

    /**
     * Get count of tasks by status
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status")
    suspend fun getTaskCountByStatus(status: TaskStatus): Int

    /**
     * Get all uncompleted weekend tasks (for Monday reminder)
     */
    @Query("SELECT * FROM tasks WHERE status = 'WEEKEND' ORDER BY dueDate ASC")
    suspend fun getUncompletedWeekendTasks(): List<TaskEntity>

    /**
     * Delete all completed tasks (clear archive)
     */
    @Query("DELETE FROM tasks WHERE status = 'COMPLETED'")
    suspend fun deleteAllCompletedTasks()

    /**
     * Get all tasks for statistics
     */
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    /**
     * Get tasks created within a time range
     */
    @Query("SELECT * FROM tasks WHERE createdDate >= :startTime AND createdDate <= :endTime")
    suspend fun getTasksCreatedBetween(startTime: Long, endTime: Long): List<TaskEntity>

    /**
     * Get tasks completed within a time range
     */
    @Query("SELECT * FROM tasks WHERE completedDate >= :startTime AND completedDate <= :endTime AND completedDate IS NOT NULL")
    suspend fun getTasksCompletedBetween(startTime: Long, endTime: Long): List<TaskEntity>

    /**
     * Get count of tasks by priority
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE priority = :priority")
    suspend fun getTaskCountByPriority(priority: com.weekendtasks.app.data.model.TaskPriority): Int
}
