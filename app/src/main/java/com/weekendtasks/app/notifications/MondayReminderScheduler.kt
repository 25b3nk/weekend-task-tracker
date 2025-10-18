package com.weekendtasks.app.notifications

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules and manages the Monday reminder check
 */
object MondayReminderScheduler {
    private const val TAG = "MondayReminderScheduler"
    private const val PREFS_NAME = "monday_reminder_prefs"
    private const val KEY_LAST_CHECK = "last_check_date"

    /**
     * Check if we should run the Monday reminder today
     * Only runs once per day
     */
    fun checkAndScheduleIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getTodayDateString()
        val lastCheck = prefs.getString(KEY_LAST_CHECK, null)

        // Check if we've already run today
        if (lastCheck == today) {
            Log.d(TAG, "Monday reminder already checked today, skipping")
            return
        }

        // Check if today is Monday
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            Log.d(TAG, "Today is not Monday, skipping")
            return
        }

        Log.d(TAG, "Today is Monday and not yet checked, scheduling worker")

        // Schedule the worker to run immediately
        val workRequest = OneTimeWorkRequestBuilder<MondayReminderWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MondayReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        // Mark that we've checked today
        prefs.edit().putString(KEY_LAST_CHECK, today).apply()
        Log.d(TAG, "Monday reminder scheduled")
    }

    /**
     * Schedule a weekly recurring check for Monday reminders
     * This ensures the check runs even if user doesn't open app on Monday morning
     */
    fun scheduleWeeklyCheck(context: Context) {
        // Calculate delay until next Monday at 9 AM
        val now = Calendar.getInstance()
        val nextMonday = Calendar.getInstance().apply {
            // Set to next Monday
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            // If it's already past Monday 9 AM this week, schedule for next week
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val delay = nextMonday.timeInMillis - now.timeInMillis

        Log.d(TAG, "Scheduling weekly Monday check, next run: ${nextMonday.time}")

        val workRequest = PeriodicWorkRequestBuilder<MondayReminderWorker>(
            7, TimeUnit.DAYS // Repeat every 7 days
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "${MondayReminderWorker.WORK_NAME}_weekly",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun getTodayDateString(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
}
