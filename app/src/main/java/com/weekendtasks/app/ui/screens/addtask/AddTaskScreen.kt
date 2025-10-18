package com.weekendtasks.app.ui.screens.addtask

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weekendtasks.app.data.model.TaskPriority
import com.weekendtasks.app.data.model.TaskStatus
import com.weekendtasks.app.ui.components.NLPParsePreview
import com.weekendtasks.app.ui.components.TaskInputField
import com.weekendtasks.app.ui.components.VoiceInputButton

/**
 * Screen for adding a new task with NLP support.
 * Displays real-time parsing preview and allows manual overrides.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: AddTaskViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inputText by viewModel.inputText.collectAsState()
    val parsedTask by viewModel.parsedTask.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    // Handle UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is AddTaskUiState.Success -> {
                onNavigateBack()
                viewModel.reset()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "Add Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Task input field with voice input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                TaskInputField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    label = "Task",
                    placeholder = "e.g., Clean garage Saturday afternoon or tap mic",
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.weight(1f)
                )

                // Voice input button
                VoiceInputButton(
                    onTextRecognized = { recognizedText ->
                        viewModel.updateInputText(recognizedText)
                    },
                    enabled = uiState !is AddTaskUiState.Loading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Processing indicator
            if (isProcessing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // NLP Parse Preview
            NLPParsePreview(parsedTask = parsedTask)

            Spacer(modifier = Modifier.height(24.dp))

            // Priority selector
            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { viewModel.setPriority(priority) },
                        label = { Text(priority.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status selector (which list to add to) - only show in add mode
            if (!isEditMode) {
                Text(
                    text = "Add to",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(TaskStatus.WEEKEND, TaskStatus.MASTER).forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { viewModel.setStatus(status) },
                            label = { Text(if (status == TaskStatus.WEEKEND) "Weekend" else "Master List") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            if (uiState is AddTaskUiState.Error) {
                Text(
                    text = (uiState as AddTaskUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Save button
            Button(
                onClick = { viewModel.saveTask() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddTaskUiState.Loading
            ) {
                if (uiState is AddTaskUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Update Task" else "Save Task")
                }
            }
        }
    }
}
