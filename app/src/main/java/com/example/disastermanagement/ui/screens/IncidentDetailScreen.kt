package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.database.User
import com.example.disastermanagement.ui.screens.shared.ConfirmationDialog
import com.example.disastermanagement.ui.screens.utils.DisasterType
import com.example.disastermanagement.ui.screens.utils.getAddressFromLocation
import com.example.disastermanagement.ui.screens.utils.getSeverityColor
import com.example.disastermanagement.ui.screens.utils.getStatusColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncidentDetailScreen(
    incident: Incident,
    userId: String,
    userRole: String,
    reporter: User?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onUpdateIncident: (status: String, severity: String) -> Unit,
    onResolve: () -> Unit,
    isProcessing: Boolean
) {
    val context = LocalContext.current
    val address by produceState(initialValue = "Loading address...", incident.location) {
        value = getAddressFromLocation(context, incident.location)
    }
    val hasConfirmed = incident.confirmedBy.contains(userId)
    var showConfirmIncidentDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var statusToUpdate by remember { mutableStateOf(incident.status) }
    var severityToUpdate by remember { mutableStateOf(incident.severity) }

    if (showConfirmIncidentDialog) {
        ConfirmationDialog(
            title = "Confirm Incident",
            message = "Are you sure you want to confirm this incident?",
            onConfirm = {
                onConfirm()
                showConfirmIncidentDialog = false
            },
            onDismiss = { showConfirmIncidentDialog = false }
        )
    }

    if (showResolveDialog) {
        ConfirmationDialog(
            title = "Resolve Incident",
            message = "Are you sure you want to resolve this incident?",
            onConfirm = {
                onResolve()
                showResolveDialog = false
                onDismiss()
            },
            onDismiss = { showResolveDialog = false }
        )
    }

    if (showUpdateDialog) {
        ConfirmationDialog(
            title = "Update Incident",
            message = "Are you sure you want to update this incident?",
            onConfirm = {
                onUpdateIncident(statusToUpdate, severityToUpdate)
                showUpdateDialog = false
            },
            onDismiss = { showUpdateDialog = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Incident Details",
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = DisasterType.getDisasterEmoji(incident.type),
                        fontSize = 80.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(text = incident.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Chip(label = incident.status.uppercase(), color = getStatusColor(incident.status))
                        if (incident.severity.lowercase() != "none") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Chip(label = incident.severity.uppercase(), color = getSeverityColor(incident.severity))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (incident.imageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(incident.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Incident Scene",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(label = "Type:", value = incident.type)
                    DetailRow(label = "Severity:", value = incident.severity)
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Reported by: ", fontWeight = FontWeight.Bold)
                        if (reporter != null) {
                            if (reporter.pfpUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(reporter.pfpUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reporter.fullName.first().toString(),
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = reporter.fullName)
                        } else {
                            Text(text = incident.reporterId) // fallback to reporterId
                        }
                    }
                    DetailRow(label = "Location:", value = address)
                    DetailRow(label = "Date:", value = SimpleDateFormat("M/dd/yyyy, hh:mm a", Locale.getDefault()).format(Date(incident.timestamp)))
                    DetailRow(label = "Confirmations:", value = incident.confirmedBy.size.toString())
                    DetailRow(label = "Description:", value = incident.description)

                    if (userRole == "barangay" || userRole == "admin") {
                        ManageIncidentSection(
                            incident = incident, 
                            userRole = userRole, 
                            onUpdateIncident = { status, severity ->
                                statusToUpdate = status
                                severityToUpdate = severity
                                showUpdateDialog = true
                            }, 
                            onResolve = { showResolveDialog = true }, 
                            isProcessing = isProcessing
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                            Button(
                                onClick = { showConfirmIncidentDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !hasConfirmed && !isProcessing
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (hasConfirmed) "Confirmed (${incident.confirmedBy.size})" else "Confirm (${incident.confirmedBy.size})")
                            }
                            if(hasConfirmed){
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF006400), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("You have confirmed this incident", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageIncidentSection(
    incident: Incident,
    userRole: String,
    onUpdateIncident: (status: String, severity: String) -> Unit,
    onResolve: () -> Unit,
    isProcessing: Boolean
) {
    var status by remember { mutableStateOf(incident.status) }
    var severity by remember { mutableStateOf(incident.severity) }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Manage Incident", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (userRole == "barangay" || userRole == "admin") {
            ExposedDropdown(label = "Status", options = listOf("Reported", "Responding", "In Area"), selectedOption = status, onOptionSelected = { status = it })
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdown(label = "Severity", options = listOf("None", "Low", "Medium", "High", "Critical"), selectedOption = severity, onOptionSelected = { severity = it })
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onUpdateIncident(status, severity) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                enabled = !isProcessing && (status != incident.status || severity != incident.severity)
            ) {
                Text("Update Incident")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onResolve,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Resolve Incident")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label ", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}

@Composable
fun Chip(label: String, color: Color = MaterialTheme.colorScheme.secondaryContainer) {
    Surface(
        modifier = Modifier.padding(vertical = 4.dp),
        shape = CircleShape,
        color = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
