package com.weekendtasks.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.weekendtasks.app.data.model.Task
import com.weekendtasks.app.data.model.TaskPriority
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.ui.theme.PriorityHigh
import com.weekendtasks.app.ui.theme.PriorityLow
import com.weekendtasks.app.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component for displaying a task.
 * Supports:
 * - Checkbox to mark complete
 * - Long-press for options menu
 * - Priority color indicator
 * - Due date and time display
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    onComplete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onMoveToWeekend: ((Task) -> Unit)? = null,
    onMoveToMaster: ((Task) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { showMenu = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp),
                color = getPriorityColor(task.priority)
            ) {}

            Spacer(modifier = Modifier.width(12.dp))

            // Checkbox for completion
            if (task.status != TaskStatus.COMPLETED) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { onComplete(task) }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Due date and time
                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(task.dueDate) +
                                if (task.dueTime != null) " at ${task.dueTime}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Options menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit(task)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )

                    if (task.status != TaskStatus.WEEKEND && onMoveToWeekend != null) {
                        DropdownMenuItem(
                            text = { Text("Move to Weekend") },
                            onClick = {
                                showMenu = false
                                onMoveToWeekend(task)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Weekend, contentDescription = null)
                            }
                        )
                    }

                    if (task.status != TaskStatus.MASTER && onMoveToMaster != null) {
                        DropdownMenuItem(
                            text = { Text("Move to Master List") },
                            onClick = {
                                showMenu = false
                                onMoveToMaster(task)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.List, contentDescription = null)
                            }
                        )
                    }

                    Divider()

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete(task)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getPriorityColor(priority: TaskPriority) = when (priority) {
    TaskPriority.HIGH -> PriorityHigh
    TaskPriority.MEDIUM -> PriorityMedium
    TaskPriority.LOW -> PriorityLow
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
