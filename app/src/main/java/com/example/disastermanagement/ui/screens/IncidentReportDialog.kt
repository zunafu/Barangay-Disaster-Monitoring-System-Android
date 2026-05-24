package com.example.disastermanagement.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.ui.screens.utils.DisasterType
import com.example.disastermanagement.ui.screens.utils.getAddressFromLocation
import com.example.disastermanagement.util.FileUtil
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportDialog(
    userId: String,
    reportedBy: String,
    location: GeoPoint,
    onDismiss: () -> Unit,
    onSubmit: (Incident) -> Unit,
    isSubmitting: Boolean // Add this
) {
    var incidentType by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var address by remember { mutableStateOf("Loading address...") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(location) {
        scope.launch {
            address = getAddressFromLocation(context, location)
        }
    }

    val disasterTypes = DisasterType.disasterTypes
    var expanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                    imageUri = it
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempCameraUri
            }
        }
    )

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showConfirmationDialog = false },
            title = { Text("Submit Report") },
            text = { Text("Are you sure about that?") },
            confirmButton = {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        val newIncident = Incident(
                            reporterId = userId,
                            reportedBy = reportedBy,
                            type = incidentType!!,
                            title = title,
                            description = description,
                            location = location,
                            imageUri = imageUri?.toString()
                        )
                        onSubmit(newIncident)
                    }
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Yes")
                    }
                }
            },
            dismissButton = {
                Button(
                    enabled = !isSubmitting,
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Incident", style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = incidentType?.let { "${disasterTypes[it]} $it" } ?: "Select Type",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Disaster Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        disasterTypes.forEach { (type, emoji) ->
                            DropdownMenuItem(
                                text = { Text("$emoji $type") },
                                onClick = {
                                    incidentType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("Brief title of the incident") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    placeholder = { Text("Describe what happened...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Upload Image (Optional)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload Image")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val uri = FileUtil.createImageFile(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Take Photo")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank() && incidentType != null) {
                            showConfirmationDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Submit Report")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submit Report")
                }
            }
        }
    }
}
