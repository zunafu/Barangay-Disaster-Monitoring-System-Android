package com.example.disastermanagement.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.disastermanagement.util.FileUtil
import com.example.disastermanagement.utils.PasswordValidator

@Composable
fun AccountSettingsScreen(
    userEmail: String,
    userRole: String,
    userJoinDate: String,
    userAccountStatus: String,
    userFullName: String,
    userPfpUrl: String,
    onUpdateProfile: (fullName: String, password: String, pfpUri: Uri?) -> Unit,
    onDeactivateAccount: () -> Unit,
    isUpdatingProfile: Boolean
) {
    var fullName by remember { mutableStateOf(userFullName) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirmDeactivate by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordValidation by remember { mutableStateOf(PasswordValidator.validatePassword("")) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedImageUri = uri
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                selectedImageUri = tempCameraUri
            }
        }
    )

    if (showConfirmDeactivate) {
        AlertDialog(
            onDismissRequest = { showConfirmDeactivate = false },
            title = { Text("Deactivate Account") },
            text = { Text("Are you sure you want to deactivate your account? You will be logged out. To reactivate, contact an admin.") },
            confirmButton = {
                Button(onClick = {
                    showConfirmDeactivate = false
                    onDeactivateAccount()
                }) { Text("Yes, Deactivate") }
            },
            dismissButton = {
                Button(onClick = { showConfirmDeactivate = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (userPfpUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userPfpUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (fullName.isNotEmpty()) {
                Text(
                    text = fullName.first().toString(),
                    fontSize = 48.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "From Gallery")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { 
                val uri = FileUtil.createImageFile(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "From Camera")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Camera")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdatingProfile,
            isError = fullName.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userEmail,
            onValueChange = {},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordValidation = PasswordValidator.validatePassword(it)
                passwordError = null
            },
            label = { Text("New Password (Leave blank to keep current)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdatingProfile,
            isError = passwordError != null || (password.isNotEmpty() && !passwordValidation.isValid)
        )

        // Password Strength Indicator for AccountSettings
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordStrengthIndicator(validation = passwordValidation)
        }

        // Password Requirements
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordRequirements(validation = passwordValidation)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdatingProfile,
            isError = passwordError != null
        )

        if (passwordError != null) {
            Text(
                text = passwordError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                when {
                    password.isNotEmpty() && !passwordValidation.isValid -> {
                        passwordError = "Password does not meet requirements."
                    }
                    password.isNotEmpty() && password != confirmPassword -> {
                        passwordError = "Passwords do not match."
                    }
                    else -> {
                        onUpdateProfile(fullName, password, selectedImageUri)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = (password.isEmpty() || (passwordValidation.isValid && password == confirmPassword)) && !isUpdatingProfile && fullName.isNotEmpty()
        ) {
            if (isUpdatingProfile) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Save Changes")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        AccountInformation(
            role = userRole,
            joinDate = userJoinDate,
            accountStatus = userAccountStatus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Deactivate account button - only if not already deactivated
        if (userAccountStatus != "Deactivated") {
            Button(
                onClick = { showConfirmDeactivate = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Deactivate Account")
            }
        }
    }
}

@Composable
fun AccountInformation(
    role: String,
    joinDate: String,
    accountStatus: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Account Information",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(text = "Role: $role", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Member Since: $joinDate", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Account Status: $accountStatus", style = MaterialTheme.typography.bodyMedium)
    }
}
