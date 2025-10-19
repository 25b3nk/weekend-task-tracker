package com.weekendtasks.app.domain.usecase

import com.weekendtasks.app.data.model.*
import com.weekendtasks.app.data.repository.TaskRepository
import java.util.Calendar

/**
 * Use case for retrieving and calculating task statistics
 */
class GetStatisticsUseCase(
    private val repository: TaskRepository
) {
    /**
     * Get comprehensive task statistics including completion rate,
     * weekly trends, and priority distribution
     */
    suspend operator fun invoke(): Result<TaskStatistics> {
        return try {
            val allTasks = repository.getAllTasks()

            // Calculate basic counts
            val totalTasks = allTasks.size
            val completedTasks = allTasks.count { it.status == TaskStatus.COMPLETED }
            val weekendTasks = allTasks.count { it.status == TaskStatus.WEEKEND }
            val masterTasks = allTasks.count { it.status == TaskStatus.MASTER }

            // Calculate completion rate
            val completionRate = if (totalTasks > 0) {
                (completedTasks.toFloat() / totalTasks) * 100
            } else {
                0f
            }

            // Calculate weekly trends (last 4 weeks)
            val weeklyTrends = calculateWeeklyTrends(allTasks)

            // Calculate priority distribution
            val priorityDistribution = PriorityDistribution(
                highPriority = allTasks.count { it.priority == TaskPriority.HIGH },
                mediumPriority = allTasks.count { it.priority == TaskPriority.MEDIUM },
                lowPriority = allTasks.count { it.priority == TaskPriority.LOW }
            )

            val statistics = TaskStatistics(
                totalTasks = totalTasks,
                completedTasks = completedTasks,
                weekendTasks = weekendTasks,
                masterTasks = masterTasks,
                completionRate = completionRate,
                weeklyTrends = weeklyTrends,
                priorityDistribution = priorityDistribution
            )

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate weekly trends for the last 4 weeks
     */
    private fun calculateWeeklyTrends(tasks: List<Task>): List<WeeklyTrend> {
        val trends = mutableListOf<WeeklyTrend>()
        val calendar = Calendar.getInstance()

        for (weeksAgo in 0..3) {
            // Calculate start of week (Monday)
            val weekStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.WEEK_OF_YEAR, -weeksAgo)
            }

            // Calculate end of week (Sunday)
            val weekEnd = Calendar.getInstance().apply {
                timeInMillis = weekStart.timeInMillis
                add(Calendar.DAY_OF_YEAR, 6)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }

            val startTime = weekStart.timeInMillis
            val endTime = weekEnd.timeInMillis

            // Count tasks created this week
            val tasksCreated = tasks.count { task ->
                task.createdDate in startTime..endTime
            }

            // Count tasks completed this week
            val tasksCompleted = tasks.count { task ->
                task.completedDate != null && task.completedDate in startTime..endTime
            }

            // Generate label
            val label = when (weeksAgo) {
                0 -> "This Week"
                1 -> "Last Week"
                2 -> "2 Weeks Ago"
                3 -> "3 Weeks Ago"
                else -> "$weeksAgo Weeks Ago"
            }

            trends.add(
                WeeklyTrend(
                    weekLabel = label,
                    tasksCreated = tasksCreated,
                    tasksCompleted = tasksCompleted,
                    startDate = startTime,
                    endDate = endTime
                )
            )
        }

        return trends.reversed() // Oldest to newest
    }
}
