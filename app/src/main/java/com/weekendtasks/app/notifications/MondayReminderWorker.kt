package com.weekendtasks.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weekendtasks.app.data.local.TaskDatabase
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.data.repository.TaskRepository
import java.util.Calendar

/**
 * Worker that runs on Monday to automatically move uncompleted weekend tasks
 * to the master list and notify the user about the moved tasks
 */
class MondayReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "MondayReminderWorker starting")

        // Check if today is Monday
        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        if (dayOfWeek != Calendar.MONDAY) {
            Log.d(TAG, "Today is not Monday, skipping")
            return Result.success()
        }

        Log.d(TAG, "Today is Monday, checking for uncompleted weekend tasks")

        // Get all uncompleted weekend tasks
        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        val uncompletedTasks = repository.getUncompletedWeekendTasks()

        if (uncompletedTasks.isEmpty()) {
            Log.d(TAG, "No uncompleted weekend tasks found")
            return Result.success()
        }

        Log.d(TAG, "Found ${uncompletedTasks.size} uncompleted weekend tasks")

        // Move all uncompleted weekend tasks to master list
        uncompletedTasks.forEach { task ->
            Log.d(TAG, "Moving task to master list: ${task.title}")
            repository.moveTask(task.id, TaskStatus.MASTER)

            // Cancel any scheduled reminders for these tasks
            ReminderScheduler(applicationContext).cancelReminder(task.id)
        }

        // Show notification with the list of moved tasks
        NotificationHelper.showMondayReminder(
            context = applicationContext,
            movedTasks = uncompletedTasks.map { it.title }
        )

        Log.d(TAG, "Successfully moved ${uncompletedTasks.size} tasks to master list")
        return Result.success()
    }

    companion object {
        private const val TAG = "MondayReminderWorker"
        const val WORK_NAME = "monday_reminder"
    }
}
