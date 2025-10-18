package com.weekendtasks.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import com.weekendtasks.app.data.model.Task
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Handles scheduling and canceling task reminder notifications
 */
class ReminderScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule a reminder notification for a task
     * Notification will appear 15 minutes before the due time
     */
    fun scheduleReminder(task: Task) {
        Log.d(TAG, "scheduleReminder called for task: ${task.title}")

        // Only schedule if task has both date and time
        if (task.dueDate == null || task.dueTime == null) {
            Log.d(TAG, "Task ${task.id} has no date or time, skipping notification")
            return
        }

        // Cancel any existing reminder for this task
        cancelReminder(task.id)

        // Calculate when to show the notification (15 minutes before due time)
        val dueTime = calculateDueTime(task.dueDate, task.dueTime)
        val notificationTime = dueTime - (15 * 60 * 1000) // 15 minutes before
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "Task: ${task.title}")
        Log.d(TAG, "Due time: ${task.dueTime}")
        Log.d(TAG, "Actual due time: ${java.util.Date(dueTime)}")
        Log.d(TAG, "Notification time (15 min before): ${java.util.Date(notificationTime)}")
        Log.d(TAG, "Current time: ${java.util.Date(currentTime)}")

        // If the due time has already passed, skip
        if (dueTime <= currentTime) {
            Log.d(TAG, "Task due time is in the past, skipping")
            return
        }

        // Calculate delay: use 15-min-before time if it's in the future,
        // otherwise notify immediately
        val delay = if (notificationTime > currentTime) {
            notificationTime - currentTime
        } else {
            // Less than 15 minutes remain, notify immediately
            Log.d(TAG, "Less than 15 minutes until due time, scheduling immediate notification")
            0L
        }

        Log.d(TAG, "Scheduling notification in ${delay / 1000 / 60} minutes")

        // Create work request
        val inputData = workDataOf(
            TaskReminderWorker.KEY_TASK_ID to task.id
        )

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(task.id)
            .build()

        // Schedule the work
        workManager.enqueueUniqueWork(
            "${TaskReminderWorker.WORK_NAME_PREFIX}${task.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.d(TAG, "Reminder scheduled successfully for task: ${task.title}")
    }

    companion object {
        private const val TAG = "ReminderScheduler"
    }

    /**
     * Cancel a scheduled reminder for a task
     */
    fun cancelReminder(taskId: String) {
        workManager.cancelUniqueWork("${TaskReminderWorker.WORK_NAME_PREFIX}$taskId")
        NotificationHelper.cancelTaskReminder(context, taskId)
    }

    /**
     * Calculate the actual due time from date and time
     */
    private fun calculateDueTime(dueDate: Long, dueTime: String): Long {
        // Parse time string (format: "HH:mm")
        val timeParts = dueTime.split(":")
        if (timeParts.size != 2) {
            return dueDate // Fallback to due date if time parsing fails
        }

        val hour = timeParts[0].toIntOrNull() ?: return dueDate
        val minute = timeParts[1].toIntOrNull() ?: return dueDate

        // Create calendar with due date and time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dueDate
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

    /**
     * Reschedule all pending task reminders (useful after device reboot)
     */
    suspend fun rescheduleAllReminders(tasks: List<Task>) {
        tasks.forEach { task ->
            if (task.completedDate == null) {
                scheduleReminder(task)
            }
        }
    }
}
