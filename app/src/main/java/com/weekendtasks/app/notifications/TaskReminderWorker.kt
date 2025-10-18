package com.weekendtasks.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weekendtasks.app.data.local.TaskDatabase
import com.weekendtasks.app.data.repository.TaskRepository

/**
 * Worker that shows a notification for a task reminder
 */
class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "TaskReminderWorker starting")

        val taskId = inputData.getString(KEY_TASK_ID)
        if (taskId == null) {
            Log.e(TAG, "No task ID provided")
            return Result.failure()
        }

        Log.d(TAG, "Processing reminder for task ID: $taskId")

        // Get task from database
        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        val task = repository.getTaskById(taskId)

        if (task == null) {
            Log.w(TAG, "Task not found: $taskId")
            return Result.success() // Task was deleted
        }

        Log.d(TAG, "Task found: ${task.title}")

        if (task.completedDate != null) {
            Log.d(TAG, "Task already completed, skipping notification")
            return Result.success()
        }

        Log.d(TAG, "Showing notification for task: ${task.title}")

        // Only show notification if task is not completed
        NotificationHelper.showTaskReminder(
            context = applicationContext,
            taskId = task.id,
            taskTitle = task.title,
            dueTime = task.dueTime
        )

        Log.d(TAG, "Notification shown successfully")
        return Result.success()
    }

    companion object {
        private const val TAG = "TaskReminderWorker"
        const val KEY_TASK_ID = "task_id"
        const val WORK_NAME_PREFIX = "task_reminder_"
    }
}
