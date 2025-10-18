package com.weekendtasks.app.notifications

import android.content.Context
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
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()

        // Get task from database
        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        val task = repository.getTaskById(taskId)

        if (task != null && task.completedDate == null) {
            // Only show notification if task is not completed
            NotificationHelper.showTaskReminder(
                context = applicationContext,
                taskId = task.id,
                taskTitle = task.title,
                dueTime = task.dueTime
            )
        }

        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val WORK_NAME_PREFIX = "task_reminder_"
    }
}
