package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.disastermanagement.data.database.Incident

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageIncidentDialog(
    incident: Incident,
    onDismiss: () -> Unit,
    onUpdate: (status: String, severity: String) -> Unit,
    isUpdating: Boolean
) {
    var status by remember { mutableStateOf(incident.status) }
    var severity by remember { mutableStateOf(incident.severity) }
    val statusOptions = listOf("Reported", "Responding", "In Area")
    val severityOptions = listOf("None", "Low", "Medium", "High", "Critical")
    var statusExpanded by remember { mutableStateOf(false) }
    var severityExpanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdating) showConfirmationDialog = false },
            title = { Text(text = "Update Incident") },
            text = { Text("Are you sure you want to update this incident?") },
            confirmButton = {
                Button(
                    enabled = !isUpdating,
                    onClick = { onUpdate(status, severity) }
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Update")
                    }
                }
            },
            dismissButton = {
                Button(
                    enabled = !isUpdating,
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Manage Incident") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    status = selectionOption
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = severityExpanded,
                    onExpandedChange = { severityExpanded = !severityExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = severity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Severity") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = severityExpanded,
                        onDismissRequest = { severityExpanded = false }
                    ) {
                        severityOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    severity = selectionOption
                                    severityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { showConfirmationDialog = true }) {
                Text(text = "Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
