package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.disastermanagement.data.database.User
import com.example.disastermanagement.utils.PasswordValidator
import com.example.disastermanagement.utils.PasswordStrength

@Composable
fun RegisterScreen(
    onRegister: (User) -> Unit,
    onNavigateToLogin: () -> Unit,
    isRegistering: Boolean
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordValidation by remember { mutableStateOf(PasswordValidator.validatePassword("")) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordValidation = PasswordValidator.validatePassword(it)
                passwordError = null
            },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, "toggle password visibility")
                }
            },
            isError = passwordError != null || (password.isNotEmpty() && !passwordValidation.isValid),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )

        // Password Strength Indicator
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordStrengthIndicator(validation = passwordValidation)
        }

        // Password Requirements
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordRequirements(validation = passwordValidation)
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it 
                passwordError = null
            },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(image, "toggle password visibility")
                }
            },
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )
        if (passwordError != null) {
            Text(
                text = passwordError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                when {
                    password.isEmpty() || confirmPassword.isEmpty() -> {
                        passwordError = "Password fields cannot be empty."
                    }
                    !passwordValidation.isValid -> {
                        passwordError = "Password does not meet requirements."
                    }
                    password != confirmPassword -> {
                        passwordError = "Passwords do not match."
                    }
                    else -> {
                        val newUser = User(email = email, fullName = fullName, password = password, role = "user")
                        onRegister(newUser)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        ) {
            if (isRegistering) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin, enabled = !isRegistering) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun PasswordStrengthIndicator(validation: com.example.disastermanagement.utils.PasswordValidationResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Password Strength:", style = MaterialTheme.typography.labelSmall)
            Text(
                validation.strength.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = when (validation.strength) {
                    PasswordStrength.WEAK -> Color.Red
                    PasswordStrength.FAIR -> Color(0xFFFF9800)
                    PasswordStrength.GOOD -> Color(0xFF4CAF50)
                    PasswordStrength.STRONG -> Color(0xFF2196F3)
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = validation.strength.percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = when (validation.strength) {
                PasswordStrength.WEAK -> Color.Red
                PasswordStrength.FAIR -> Color(0xFFFF9800)
                PasswordStrength.GOOD -> Color(0xFF4CAF50)
                PasswordStrength.STRONG -> Color(0xFF2196F3)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun PasswordRequirements(validation: com.example.disastermanagement.utils.PasswordValidationResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp)
    ) {
        RequirementItem("At least 6 characters", validation.hasMinLength)
        RequirementItem("One uppercase letter", validation.hasUpperCase)
        RequirementItem("One lowercase letter", validation.hasLowerCase)
        RequirementItem("One number", validation.hasNumber)
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val color = if (isMet) Color(0xFF4CAF50) else Color.Gray
        Text(
            text = if (isMet) "✓" else "○",
            color = color,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) Color.Black else Color.Gray
        )
    }
}
