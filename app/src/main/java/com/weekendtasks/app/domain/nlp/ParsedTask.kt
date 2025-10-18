package com.weekendtasks.app.domain.nlp

/**
 * Data class representing a task parsed from natural language input.
 * Contains the extracted task title, due date, and due time.
 */
data class ParsedTask(
    val title: String,
    val dueDate: Long? = null,
    val dueTime: String? = null, // Format: HH:mm
    val rawInput: String = "",
    val confidence: Float = 1.0f // Confidence level of the parsing (0.0 to 1.0)
)

/**
 * Sealed class representing the result of NLP parsing
 */
sealed class NLPResult {
    data class Success(val parsedTask: ParsedTask) : NLPResult()
    data class Error(val message: String) : NLPResult()
    data object Loading : NLPResult()
}
