package com.weekendtasks.app.domain.nlp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main NLP processor that combines ML Kit and Natty parser.
 * Handles natural language processing for task input.
 *
 * This class:
 * - Uses ML Kit for entity extraction (dates, times)
 * - Falls back to Natty parser if ML Kit fails
 * - Extracts task title by removing date/time text
 * - Handles various input patterns
 */
class NaturalLanguageProcessor {

    private val entityExtractor = EntityExtractor()
    private val dateTimeParser = DateTimeParser()

    /**
     * Initialize the NLP processor by downloading ML Kit models
     */
    suspend fun initialize(): Boolean {
        return entityExtractor.downloadModelIfNeeded()
    }

    /**
     * Parse natural language input into a structured task.
     *
     * Examples:
     * - "Clean garage next Saturday at 2pm"
     * - "Buy groceries tomorrow morning"
     * - "Paint fence this weekend"
     */
    suspend fun parseTaskInput(input: String): ParsedTask = withContext(Dispatchers.Default) {
        try {
            if (input.isBlank()) {
                return@withContext ParsedTask(
                    title = "",
                    rawInput = input,
                    confidence = 0.0f
                )
            }

            Log.d(TAG, "Parsing input: $input")

            // Try ML Kit entity extraction first
            val annotations = entityExtractor.extractEntities(input)
            var parsedDateTime = entityExtractor.extractDateTime(annotations)

            // Fallback to Natty parser if ML Kit didn't find anything
            if (parsedDateTime == null) {
                Log.d(TAG, "ML Kit found no entities, trying Natty parser")
                parsedDateTime = dateTimeParser.parseDateTime(input)
            }

            // Handle special keywords
            if (parsedDateTime == null) {
                parsedDateTime = handleSpecialKeywords(input)
            }

            // Extract task title by removing date/time text
            val taskTitle = extractTaskTitle(input, parsedDateTime?.matchedText)

            // Parse relative time expressions if no specific time was found
            val finalTimeString = parsedDateTime?.timeString
                ?: dateTimeParser.parseRelativeTime(input)

            ParsedTask(
                title = taskTitle.trim(),
                dueDate = parsedDateTime?.dateTimestamp,
                dueTime = finalTimeString,
                rawInput = input,
                confidence = calculateConfidence(taskTitle, parsedDateTime)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing task input", e)
            ParsedTask(
                title = input.trim(),
                rawInput = input,
                confidence = 0.3f
            )
        }
    }

    /**
     * Extract task title by removing date/time related text
     */
    private fun extractTaskTitle(input: String, matchedDateTime: String?): String {
        var title = input

        // Remove matched date/time text
        if (!matchedDateTime.isNullOrBlank()) {
            title = title.replace(matchedDateTime, "", ignoreCase = true)
        }

        // Remove common date/time words
        val dateTimeWords = listOf(
            "tomorrow", "today", "tonight", "this weekend", "next weekend",
            "this saturday", "next saturday", "this sunday", "next sunday",
            "friday", "saturday", "sunday", "monday", "tuesday", "wednesday", "thursday",
            "morning", "afternoon", "evening", "night", "noon",
            "at", "on", "next", "this", "pm", "am"
        )

        var cleanedTitle = title
        for (word in dateTimeWords) {
            cleanedTitle = cleanedTitle.replace(Regex("\\b$word\\b", RegexOption.IGNORE_CASE), "")
        }

        // Remove time patterns (e.g., "2pm", "14:30", "2:30pm")
        cleanedTitle = cleanedTitle.replace(Regex("\\d{1,2}:\\d{2}\\s*(am|pm)?", RegexOption.IGNORE_CASE), "")
        cleanedTitle = cleanedTitle.replace(Regex("\\d{1,2}\\s*(am|pm)", RegexOption.IGNORE_CASE), "")

        // Clean up extra whitespace
        cleanedTitle = cleanedTitle.replace(Regex("\\s+"), " ").trim()

        return if (cleanedTitle.isBlank()) input else cleanedTitle
    }

    /**
     * Handle special keywords like "this weekend", "next weekend"
     */
    private fun handleSpecialKeywords(input: String): ParsedDateTime? {
        val lowerInput = input.lowercase()

        return when {
            lowerInput.contains("this weekend") || lowerInput.contains("next weekend") -> {
                ParsedDateTime(
                    dateTimestamp = dateTimeParser.getUpcomingWeekend(),
                    timeString = null,
                    matchedText = if (lowerInput.contains("this weekend")) "this weekend" else "next weekend"
                )
            }
            lowerInput.contains("next saturday") -> {
                ParsedDateTime(
                    dateTimestamp = dateTimeParser.getNextDayOfWeek(java.util.Calendar.SATURDAY),
                    timeString = null,
                    matchedText = "next saturday"
                )
            }
            lowerInput.contains("next sunday") -> {
                ParsedDateTime(
                    dateTimestamp = dateTimeParser.getNextDayOfWeek(java.util.Calendar.SUNDAY),
                    timeString = null,
                    matchedText = "next sunday"
                )
            }
            lowerInput.contains("next friday") -> {
                ParsedDateTime(
                    dateTimestamp = dateTimeParser.getNextDayOfWeek(java.util.Calendar.FRIDAY),
                    timeString = null,
                    matchedText = "next friday"
                )
            }
            else -> null
        }
    }

    /**
     * Calculate confidence score for the parsing
     */
    private fun calculateConfidence(title: String, dateTime: ParsedDateTime?): Float {
        return when {
            title.isBlank() -> 0.0f
            dateTime == null -> 0.7f  // Task without date/time is still valid
            else -> 1.0f  // Successfully parsed everything
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        entityExtractor.close()
    }

    companion object {
        private const val TAG = "NaturalLanguageProcessor"
    }
}
