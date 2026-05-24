package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disastermanagement.data.database.AuditLogDao
import com.example.disastermanagement.data.database.User
import com.example.disastermanagement.data.database.UserDao
import com.example.disastermanagement.utils.AuditLogHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserManagementViewModel(private val userDao: UserDao, private val auditLogDao: AuditLogDao) : ViewModel() {
    val users = userDao.getAllUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun addUser(user: User, actorId: String = "admin", actorEmail: String = "admin") {
        viewModelScope.launch {
            val existingUser = userDao.getUserByEmail(user.email)
            if (existingUser == null) {
                userDao.insertUser(user)
                // Audit: user created with detailed logging
                auditLogDao.insertLog(
                    AuditLogHelper.createUserCreationLog(
                        user = user,
                        actorId = actorId,
                        actorEmail = actorEmail
                    )
                )
                _snackbarMessage.emit("User added successfully.")
            } else {
                _snackbarMessage.emit("User with this email already exists.")
            }
        }
    }

    fun updateUser(user: User, actorId: String = "admin", actorEmail: String = "admin") {
        viewModelScope.launch {
            // Get the old user data for comparison
            val oldUser = userDao.getUserById(user.id)
            if (oldUser != null) {
                userDao.updateUser(user)
                // Audit: user updated with detailed logging showing what changed
                auditLogDao.insertLog(
                    AuditLogHelper.createUserUpdateLog(
                        oldUser = oldUser,
                        newUser = user,
                        actorId = actorId,
                        actorEmail = actorEmail
                    )
                )
            }
        }
    }

    fun deleteUser(user: User, actorId: String = "admin", actorEmail: String = "admin") {
        // Keep for compatibility but prefer deactivateUser
        viewModelScope.launch {
            userDao.deleteUser(user)
            // Audit: user deleted with detailed logging
            auditLogDao.insertLog(
                AuditLogHelper.createUserDeletionLog(
                    user = user,
                    actorId = actorId,
                    actorEmail = actorEmail
                )
            )
        }
    }

    fun deactivateUser(user: User, actorId: String = "admin", actorEmail: String = "admin") {
        viewModelScope.launch {
            // Prevent deactivating admin accounts
            if (user.role == "admin") {
                _snackbarMessage.emit("Cannot deactivate admin accounts.")
                return@launch
            }
            userDao.updateAccountStatus(user.id, "Deactivated")
            // Audit: user deactivated with detailed logging
            auditLogDao.insertLog(
                AuditLogHelper.createUserDeactivationLog(
                    user = user,
                    actorId = actorId,
                    actorEmail = actorEmail,
                    reason = "Deactivated by admin"
                )
            )
            _snackbarMessage.emit("User deactivated.")
        }
    }

    fun reactivateUser(user: User, actorId: String = "admin", actorEmail: String = "admin") {
        viewModelScope.launch {
            userDao.updateAccountStatus(user.id, "Verified")
            // Audit: user reactivated with detailed logging
            auditLogDao.insertLog(
                AuditLogHelper.createUserReactivationLog(
                    user = user,
                    actorId = actorId,
                    actorEmail = actorEmail
                )
            )
            _snackbarMessage.emit("User reactivated.")
        }
    }
}

@Composable
fun UserManagementScreen(userDao: UserDao, auditLogDao: AuditLogDao) {
    val viewModel: UserManagementViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserManagementViewModel(userDao, auditLogDao) as T
        }
    })
    val users by viewModel.users.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddUserDialog = true }) {
                Text("+")
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(users) {
                UserListItem(user = it, onEdit = { userToEdit = it }, onToggleStatus = { user ->
                    if (user.accountStatus == "Deactivated") {
                        viewModel.reactivateUser(user)
                    } else {
                        viewModel.deactivateUser(user)
                    }
                })
            }
        }

        if (showAddUserDialog) {
            AddUserDialog(
                onDismiss = { showAddUserDialog = false },
                onAddUser = { email, fullName, password, role ->
                    val newUser = User(email = email, fullName = fullName, password = password, role = role)
                    viewModel.addUser(newUser)
                    showAddUserDialog = false
                }
            )
        }

        userToEdit?.let { user ->
            EditUserDialog(
                user = user,
                onDismiss = { userToEdit = null },
                onUpdateUser = { updatedUser ->
                    viewModel.updateUser(updatedUser)
                    userToEdit = null
                }
            )
        }
    }
}

@Composable
fun UserListItem(user: User, onEdit: () -> Unit, onToggleStatus: (User) -> Unit) {
    ListItem(
        headlineContent = { Text(user.fullName) },
        supportingContent = { Text(user.email) },
        trailingContent = {
            Row {
                Button(onClick = onEdit) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                val buttonLabel = if (user.accountStatus == "Deactivated") "Reactivate" else "Deactivate"
                Button(onClick = { onToggleStatus(user) }) {
                    Text(buttonLabel)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(onDismiss: () -> Unit, onAddUser: (String, String, String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("user") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle password visibility")
                        }
                    }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                            Icon(if (confirmPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle confirm password visibility")
                        }
                    }
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("user") }, onClick = { role = "user"; expanded = false })
                        DropdownMenuItem(text = { Text("barangay") }, onClick = { role = "barangay"; expanded = false })
                        DropdownMenuItem(text = { Text("admin") }, onClick = { role = "admin"; expanded = false })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddUser(email, fullName, password, role) },
                enabled = password.isNotEmpty() && password == confirmPassword
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(user: User, onDismiss: () -> Unit, onUpdateUser: (User) -> Unit) {
    var email by remember { mutableStateOf(user.email) }
    var fullName by remember { mutableStateOf(user.fullName) }
    var role by remember { mutableStateOf(user.role) }
    var expanded by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle password visibility")
                        }
                    }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                            Icon(if (confirmPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle confirm password visibility")
                        }
                    }
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("user") }, onClick = { role = "user"; expanded = false })
                        DropdownMenuItem(text = { Text("barangay") }, onClick = { role = "barangay"; expanded = false })
                        DropdownMenuItem(text = { Text("admin") }, onClick = { role = "admin"; expanded = false })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedUser = if (password.isNotEmpty()) {
                        user.copy(email = email, fullName = fullName, role = role, password = password)
                    } else {
                        user.copy(email = email, fullName = fullName, role = role)
                    }
                    onUpdateUser(updatedUser)
                },
                enabled = password == confirmPassword
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
