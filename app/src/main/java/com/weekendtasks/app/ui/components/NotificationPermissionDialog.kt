package com.weekendtasks.app.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Dialog that requests notification permission from the user.
 * Only shown on Android 13+ when permission is not granted.
 */
@Composable
fun NotificationPermissionDialog() {
    // Only needed for Android 13+
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    var permissionChecked by remember { mutableStateOf(false) }

    // Check permission status
    LaunchedEffect(Unit) {
        if (!permissionChecked) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                showDialog = true
            }
            permissionChecked = true
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Check if we should show rationale (user denied but didn't select "Don't ask again")
            val activity = context as? Activity
            if (activity != null) {
                val shouldShowRationale = activity.shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (!shouldShowRationale) {
                    // User selected "Don't ask again", show dialog to go to settings
                    showRationale = true
                }
            }
        }
        showDialog = false
    }

    // Initial permission request dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enable Notifications") },
            text = {
                Text(
                    "Get reminded about your tasks 15 minutes before they're due. " +
                    "Enable notifications to stay on track with your weekend plans!"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // Rationale dialog (when user denied with "Don't ask again")
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Notifications Disabled") },
            text = {
                Text(
                    "Notifications are currently disabled. To receive task reminders, " +
                    "please enable notifications in the app settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Open app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        showRationale = false
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
