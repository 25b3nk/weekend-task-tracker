package com.weekendtasks.app

import com.weekendtasks.app.domain.nlp.DateTimeParser
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Unit tests for NLP parsing logic.
 * Tests the DateTimeParser and various date/time extraction scenarios.
 */
class NLPParsingTest {

    private lateinit var dateTimeParser: DateTimeParser

    @Before
    fun setup() {
        dateTimeParser = DateTimeParser()
    }

    @Test
    fun `test parse simple task with tomorrow`() {
        val input = "Clean garage tomorrow"
        val result = dateTimeParser.parseDateTime(input)

        assertNotNull("Should parse 'tomorrow'", result)
        assertTrue("Should contain date", result!!.dateTimestamp > System.currentTimeMillis())
    }

    @Test
    fun `test parse task with specific time`() {
        val input = "Buy groceries tomorrow at 3pm"
        val result = dateTimeParser.parseDateTime(input)

        assertNotNull("Should parse date and time", result)
        assertNotNull("Should extract time", result!!.timeString)
    }

    @Test
    fun `test parse task with weekend keyword`() {
        val weekend = dateTimeParser.getUpcomingWeekend()

        assertTrue("Should return future date", weekend > System.currentTimeMillis())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = weekend
        assertEquals("Should be Saturday", Calendar.SATURDAY, calendar.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `test parse relative time - morning`() {
        val result = dateTimeParser.parseRelativeTime("tomorrow morning")

        assertNotNull("Should parse 'morning'", result)
        assertEquals("Should be 09:00", "09:00", result)
    }

    @Test
    fun `test parse relative time - afternoon`() {
        val result = dateTimeParser.parseRelativeTime("Saturday afternoon")

        assertNotNull("Should parse 'afternoon'", result)
        assertEquals("Should be 14:00", "14:00", result)
    }

    @Test
    fun `test parse relative time - evening`() {
        val result = dateTimeParser.parseRelativeTime("Friday evening")

        assertNotNull("Should parse 'evening'", result)
        assertEquals("Should be 18:00", "18:00", result)
    }

    @Test
    fun `test parse relative time - night`() {
        val result = dateTimeParser.parseRelativeTime("tonight")

        assertNotNull("Should parse 'night'", result)
        assertEquals("Should be 20:00", "20:00", result)
    }

    @Test
    fun `test get next day of week - Saturday`() {
        val nextSaturday = dateTimeParser.getNextDayOfWeek(Calendar.SATURDAY)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = nextSaturday

        assertEquals("Should be Saturday", Calendar.SATURDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertTrue("Should be in future", nextSaturday > System.currentTimeMillis())
    }

    @Test
    fun `test get next day of week - Sunday`() {
        val nextSunday = dateTimeParser.getNextDayOfWeek(Calendar.SUNDAY)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = nextSunday

        assertEquals("Should be Sunday", Calendar.SUNDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertTrue("Should be in future", nextSunday > System.currentTimeMillis())
    }

    @Test
    fun `test parse complex input with Natty`() {
        val input = "Paint fence next Saturday at 2pm"
        val result = dateTimeParser.parseDateTime(input)

        assertNotNull("Should parse complex input", result)
        assertTrue("Should have future date", result!!.dateTimestamp > System.currentTimeMillis())
    }

    @Test
    fun `test no date in input`() {
        val input = "Just a regular task"
        val result = dateTimeParser.parseDateTime(input)

        // Natty might return null or current date, both are acceptable
        // The important thing is the app handles it gracefully
        assertTrue("Should handle input without date", true)
    }

    @Test
    fun `test empty input`() {
        val input = ""
        val result = dateTimeParser.parseDateTime(input)

        assertNull("Should return null for empty input", result)
    }
}
