package com.weekendtasks.app.data.model

/**
 * Statistics data model containing all aggregated task metrics
 */
data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val weekendTasks: Int,
    val masterTasks: Int,
    val completionRate: Float,
    val weeklyTrends: List<WeeklyTrend>,
    val priorityDistribution: PriorityDistribution
)

/**
 * Weekly trend data showing tasks created and completed in a specific week
 */
data class WeeklyTrend(
    val weekLabel: String, // e.g., "This Week", "Last Week", "2 Weeks Ago"
    val tasksCreated: Int,
    val tasksCompleted: Int,
    val startDate: Long,
    val endDate: Long
)

/**
 * Distribution of tasks by priority level
 */
data class PriorityDistribution(
    val highPriority: Int,
    val mediumPriority: Int,
    val lowPriority: Int
) {
    val total: Int
        get() = highPriority + mediumPriority + lowPriority

    val highPercentage: Float
        get() = if (total > 0) (highPriority.toFloat() / total) * 100 else 0f

    val mediumPercentage: Float
        get() = if (total > 0) (mediumPriority.toFloat() / total) * 100 else 0f

    val lowPercentage: Float
        get() = if (total > 0) (lowPriority.toFloat() / total) * 100 else 0f
}
