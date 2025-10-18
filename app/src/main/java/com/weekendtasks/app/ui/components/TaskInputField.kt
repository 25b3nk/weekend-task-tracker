package com.weekendtasks.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Text field component for task input with NLP support.
 */
@Composable
fun TaskInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Task",
    placeholder: String = "e.g., Clean garage Saturday afternoon",
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 3
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines,
        singleLine = minLines == 1 && maxLines == 1
    )
}
