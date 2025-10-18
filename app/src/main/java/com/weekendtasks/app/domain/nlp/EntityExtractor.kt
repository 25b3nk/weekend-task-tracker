package com.weekendtasks.app.domain.nlp

import android.util.Log
import com.google.mlkit.nl.entityextraction.*
import kotlinx.coroutines.tasks.await

/**
 * Wrapper class for Google ML Kit Entity Extraction.
 * Handles model download, entity extraction, and parsing of dates/times.
 */
class EntityExtractor {

    private val entityExtractor: com.google.mlkit.nl.entityextraction.EntityExtractor
    private val dateTimeParser = DateTimeParser()

    init {
        // Initialize ML Kit Entity Extractor for English
        val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
            .build()
        entityExtractor = EntityExtraction.getClient(options)
    }

    /**
     * Download the ML Kit model if needed.
     * Should be called on app startup or before first use.
     */
    suspend fun downloadModelIfNeeded(): Boolean {
        return try {
            entityExtractor.downloadModelIfNeeded().await()
            Log.d(TAG, "ML Kit entity extraction model ready")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download ML Kit model", e)
            false
        }
    }

    /**
     * Extract entities from text input.
     * Returns a list of entity annotations found in the text.
     */
    suspend fun extractEntities(text: String): List<EntityAnnotation> {
        return try {
            val params = EntityExtractionParams.Builder(text)
                .build()
            entityExtractor.annotate(params).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract entities", e)
            emptyList()
        }
    }

    /**
     * Extract date and time entities from the annotations
     */
    fun extractDateTime(annotations: List<EntityAnnotation>): ParsedDateTime? {
        var dateTime: Long? = null
        var timeString: String? = null
        var matchedText = ""

        for (annotation in annotations) {
            for (entity in annotation.entities) {
                when (entity) {
                    is DateTimeEntity -> {
                        // Extract date and time from ML Kit entity
                        dateTime = entity.timestampMillis
                        matchedText = annotation.annotatedText

                        // Try to extract time if present
                        timeString = extractTimeFromEntity(entity)

                        Log.d(TAG, "Found date/time entity: ${annotation.annotatedText} -> $dateTime, $timeString")
                    }
                }
            }
        }

        return if (dateTime != null) {
            ParsedDateTime(dateTime, timeString, matchedText)
        } else {
            null
        }
    }

    /**
     * Extract time string from DateTimeEntity
     */
    private fun extractTimeFromEntity(entity: DateTimeEntity): String? {
        return try {
            // ML Kit provides granularity information
            when (entity.dateTimeGranularity) {
                DateTimeEntity.GRANULARITY_HOUR,
                DateTimeEntity.GRANULARITY_MINUTE,
                DateTimeEntity.GRANULARITY_SECOND -> {
                    // Extract time from timestamp
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = entity.timestampMillis
                    String.format(
                        "%02d:%02d",
                        calendar.get(java.util.Calendar.HOUR_OF_DAY),
                        calendar.get(java.util.Calendar.MINUTE)
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting time from entity", e)
            null
        }
    }

    /**
     * Clean up resources
     */
    fun close() {
        try {
            entityExtractor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing entity extractor", e)
        }
    }

    companion object {
        private const val TAG = "EntityExtractor"
    }
}
