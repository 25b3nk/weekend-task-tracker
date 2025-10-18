package com.weekendtasks.app

import android.app.Application
import com.weekendtasks.app.data.local.TaskDatabase
import com.weekendtasks.app.notifications.MondayReminderScheduler
import com.weekendtasks.app.notifications.NotificationHelper

/**
 * Application class for Weekend Task Tracker.
 * Initializes the database and provides application-wide dependencies.
 */
class WeekendTaskApp : Application() {

    // Lazy initialization of database
    val database: TaskDatabase by lazy {
        TaskDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        // Schedule weekly Monday reminder check
        MondayReminderScheduler.scheduleWeeklyCheck(this)

        // Check if we should run Monday reminder today
        MondayReminderScheduler.checkAndScheduleIfNeeded(this)
    }

    companion object {
        lateinit var instance: WeekendTaskApp
            private set
    }
}
