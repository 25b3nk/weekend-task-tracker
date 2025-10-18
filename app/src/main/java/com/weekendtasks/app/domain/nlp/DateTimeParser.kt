package com.weekendtasks.app.domain.nlp

import com.joestelmach.natty.DateGroup
import com.joestelmach.natty.Parser
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for parsing dates and times from text.
 * Uses Natty date parser as a fallback to ML Kit.
 */
class DateTimeParser {

    private val nattyParser = Parser()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    /**
     * Parse date and time from text using Natty parser
     */
    fun parseDateTime(text: String): ParsedDateTime? {
        try {
            val groups: List<DateGroup> = nattyParser.parse(text)

            if (groups.isEmpty() || groups[0].dates.isEmpty()) {
                return null
            }

            val date = groups[0].dates[0]
            val parsedText = groups[0].text

            return ParsedDateTime(
                dateTimestamp = date.time,
                timeString = if (hasTime(parsedText)) {
                    timeFormat.format(date)
                } else {
                    null
                },
                matchedText = parsedText
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Check if the parsed text contains time information
     */
    private fun hasTime(text: String): Boolean {
        val timePatterns = listOf(
            "\\d{1,2}:\\d{2}",  // 14:30
            "\\d{1,2}\\s*(am|pm)",  // 2pm, 2 pm
            "at\\s+\\d+",  // at 2
            "(morning|afternoon|evening|night)"
        )

        return timePatterns.any { pattern ->
            text.lowercase().contains(Regex(pattern))
        }
    }

    /**
     * Get the next occurrence of a specific day of week
     */
    fun getNextDayOfWeek(dayOfWeek: Int): Long {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        var daysToAdd = dayOfWeek - currentDayOfWeek
        if (daysToAdd <= 0) {
            daysToAdd += 7
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * Get upcoming weekend (Saturday)
     */
    fun getUpcomingWeekend(): Long {
        return getNextDayOfWeek(Calendar.SATURDAY)
    }

    /**
     * Parse relative time words (morning, afternoon, etc.) to time strings
     */
    fun parseRelativeTime(text: String): String? {
        return when {
            text.contains("morning", ignoreCase = true) -> "09:00"
            text.contains("afternoon", ignoreCase = true) -> "14:00"
            text.contains("evening", ignoreCase = true) -> "18:00"
            text.contains("night", ignoreCase = true) -> "20:00"
            text.contains("noon", ignoreCase = true) -> "12:00"
            else -> null
        }
    }
}

/**
 * Data class representing parsed date and time
 */
data class ParsedDateTime(
    val dateTimestamp: Long,
    val timeString: String?,
    val matchedText: String
)
