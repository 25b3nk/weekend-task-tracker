package com.weekendtasks.app

import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.data.model.TaskPriority
import com.weekendtasks.app.data.model.TaskStatus
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Unit tests for Task domain model and conversions.
 */
class TaskModelTest {

    @Test
    fun `test create task with all fields`() {
        val task = Task(
            id = "test-id",
            title = "Test Task",
            description = "Test Description",
            status = TaskStatus.WEEKEND,
            createdDate = System.currentTimeMillis(),
            completedDate = null,
            dueDate = System.currentTimeMillis() + 86400000, // Tomorrow
            dueTime = "14:00",
            priority = TaskPriority.HIGH
        )

        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertEquals(TaskStatus.WEEKEND, task.status)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals("14:00", task.dueTime)
    }

    @Test
    fun `test create task with minimal fields`() {
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = "Minimal Task",
            description = null,
            status = TaskStatus.WEEKEND,
            createdDate = System.currentTimeMillis(),
            completedDate = null,
            dueDate = null,
            dueTime = null,
            priority = TaskPriority.MEDIUM
        )

        assertEquals("Minimal Task", task.title)
        assertNull(task.description)
        assertNull(task.dueDate)
        assertNull(task.dueTime)
        assertEquals(TaskPriority.MEDIUM, task.priority)
    }

    @Test
    fun `test task status enum values`() {
        val statuses = TaskStatus.entries

        assertTrue(statuses.contains(TaskStatus.WEEKEND))
        assertTrue(statuses.contains(TaskStatus.MASTER))
        assertTrue(statuses.contains(TaskStatus.COMPLETED))
        assertEquals(3, statuses.size)
    }

    @Test
    fun `test task priority enum values`() {
        val priorities = TaskPriority.entries

        assertTrue(priorities.contains(TaskPriority.LOW))
        assertTrue(priorities.contains(TaskPriority.MEDIUM))
        assertTrue(priorities.contains(TaskPriority.HIGH))
        assertEquals(3, priorities.size)
    }

    @Test
    fun `test task copy with changes`() {
        val original = Task(
            id = "test-id",
            title = "Original",
            description = null,
            status = TaskStatus.WEEKEND,
            createdDate = System.currentTimeMillis(),
            completedDate = null,
            dueDate = null,
            dueTime = null,
            priority = TaskPriority.LOW
        )

        val modified = original.copy(
            title = "Modified",
            priority = TaskPriority.HIGH
        )

        assertEquals("Modified", modified.title)
        assertEquals(TaskPriority.HIGH, modified.priority)
        assertEquals(original.id, modified.id)
        assertEquals(original.status, modified.status)
    }
}
