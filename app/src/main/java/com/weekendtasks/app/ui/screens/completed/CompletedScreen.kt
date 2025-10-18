package com.weekendtasks.app.ui.screens.completed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.ui.components.TaskCard

/**
 * Screen displaying completed tasks (archive).
 */
@Composable
fun CompletedScreen(
    tasks: List<Task>,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onMoveToWeekend: (Task) -> Unit,
    onMoveToMaster: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        // Empty state
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No completed tasks yet.\nCompleted tasks will appear here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onComplete = {}, // No completion action for already completed tasks
                    onEdit = onEditTask,
                    onDelete = onDeleteTask,
                    onMoveToWeekend = onMoveToWeekend,
                    onMoveToMaster = onMoveToMaster
                )
            }
        }
    }
}
