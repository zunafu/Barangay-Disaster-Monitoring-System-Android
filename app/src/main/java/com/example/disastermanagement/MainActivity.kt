package com.example.disastermanagement

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.disastermanagement.data.database.AppDatabase
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.database.User
import com.example.disastermanagement.service.LocationService
import com.example.disastermanagement.ui.screens.IncidentReportDialog
import com.example.disastermanagement.ui.screens.utils.getAddressFromLocation
import com.example.disastermanagement.ui.theme.DisastermanagementTheme
import com.example.disastermanagement.utils.AuditLogHelper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    // Track whether location permission was granted so Compose can react immediately
    val locationPermissionGranted = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            // Permission granted, inform Compose/UI
            locationPermissionGranted.value = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        requestLocationPermission()

        enableEdgeToEdge()
        setContent {
            DisastermanagementTheme {
                val navController = rememberNavController()
                val db = AppDatabase.getDatabase(this)
                val userDao = db.userDao()
                val auditLogDao = db.auditLogDao()
                val incidentDao = db.incidentDao()
                val chatMessageDao = db.chatMessageDao()
                val incidents by incidentDao.getAllIncidents().collectAsState(initial = emptyList())
                val users by userDao.getAllUsers().collectAsState(initial = emptyList())
                var userRole by remember { mutableStateOf("guest") }
                var userId by remember { mutableStateOf("guest") }
                var userEmail by remember { mutableStateOf("guest") }
                var userJoinDate by remember { mutableStateOf("") }
                var userAccountStatus by remember { mutableStateOf("") }
                var userFullName by remember { mutableStateOf("Guest") }
                var userPfpUrl by remember { mutableStateOf("") }
                val userBarangay = "Barangay 1" // Placeholder for the current user's barangay
                var searchQuery by remember { mutableStateOf("") }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                var showIncidentReportDialog by remember { mutableStateOf(false) }
                var isReportMode by remember { mutableStateOf(false) }
                var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
                var mapCenter by remember { mutableStateOf<GeoPoint?>(null) }
                var isSubmittingIncident by remember { mutableStateOf(false) }
                var isLoggingIn by remember { mutableStateOf(false) }
                var isRegistering by remember { mutableStateOf(false) }
                var isUpdatingProfile by remember { mutableStateOf(false) }
                var processingIncidentId by remember { mutableStateOf<Int?>(null) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                if (isReportMode && selectedLocation != null) {
                    AlertDialog(
                        onDismissRequest = {
                            selectedLocation = null
                            isReportMode = false
                        },
                        title = { Text("Confirm Location") },
                        text = { Text("Use this location for the incident report?") },
                        confirmButton = {
                            Button(onClick = {
                                showIncidentReportDialog = true
                            }) { Text("Confirm") }
                        },
                        dismissButton = {
                            Button(onClick = {
                                selectedLocation = null
                                isReportMode = false
                            }) { Text("Cancel") }
                        }
                    )
                }

                if (showIncidentReportDialog && selectedLocation != null) {
                    IncidentReportDialog(
                        userId = userId,
                        reportedBy = userFullName,
                        location = selectedLocation!!,
                        onDismiss = {
                            if (!isSubmittingIncident) {
                                showIncidentReportDialog = false
                                selectedLocation = null
                                isReportMode = false
                            }
                        },
                        onSubmit = { incident ->
                            scope.launch {
                                isSubmittingIncident = true
                                try {
                                    val incidentToSave = incident.copy(confirmedBy = listOf(userId))
                                    incidentDao.insertIncident(incidentToSave)
                                    // Audit: incident created with detailed logging
                                    auditLogDao.insertLog(
                                        AuditLogHelper.createIncidentCreationLog(
                                            incident = incidentToSave,
                                            actorId = userId,
                                            actorEmail = userEmail
                                        )
                                    )
                                    snackbarHostState.showSnackbar("Incident reported successfully.")
                                    showIncidentReportDialog = false
                                    selectedLocation = null
                                    isReportMode = false
                                } finally {
                                    isSubmittingIncident = false
                                }
                            }
                        },
                        isSubmitting = isSubmittingIncident
                    )
                }

                val filteredIncidents by produceState<List<Incident>>(initialValue = emptyList(), incidents, searchQuery, currentRoute) {
                    value = if (searchQuery.isBlank()) {
                        incidents
                    } else {
                        incidents.filter {
                            val address = getAddressFromLocation(this@MainActivity, it.location)
                            val searchFilter = (it.type.contains(searchQuery, true) || it.description.contains(searchQuery, true) || it.title.contains(searchQuery, true) || address.contains(searchQuery, true))
                            val barangayFilter = if (currentRoute == "voting") address.contains(userBarangay, true) else true
                            searchFilter && barangayFilter
                        }
                    }
                }

                DismissibleNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = drawerState.isOpen,
                    drawerContent = {
                        ModalDrawerSheet {
                            if (userRole != "guest") {
                                Text("Logged in as: $userFullName ($userRole)", modifier = Modifier.padding(16.dp))
                                HorizontalDivider()
                                NavigationDrawerItem(
                                    label = { Text("Account Settings") },
                                    selected = currentRoute == "account_settings",
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("account_settings")
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Account Settings") }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Logout") },
                                    selected = false,
                                    onClick = {
                                        userRole = "guest"
                                        userId = "guest"
                                        userEmail = "guest"
                                        userJoinDate = ""
                                        userAccountStatus = ""
                                        userFullName = "Guest"
                                        userPfpUrl = ""
                                        scope.launch { drawerState.close() }
                                        navController.navigate("main")
                                    },
                                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") }
                                )
                            } else {
                                NavigationDrawerItem(
                                    label = { Text("Login / Sign Up") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("login")
                                    },
                                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Login") }
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(
                        snackbarHost = {
                            Box(
                                modifier = Modifier.zIndex(Float.MAX_VALUE)
                            ) {
                                SnackbarHost(
                                    snackbarHostState,
                                    modifier = Modifier
                                        .imePadding()
                                        .zIndex(Float.MAX_VALUE)
                                )
                            }
                        },
                        floatingActionButton = {
                            if (currentRoute == "main" && userRole == "user") {
                                Column(horizontalAlignment = Alignment.End) {
                                    AnimatedVisibility(visible = isReportMode) {
                                        ExtendedFloatingActionButton(
                                            onClick = {
                                                val locationService = LocationService(this@MainActivity)
                                                locationService.requestSingleLocationUpdate { geoPoint ->
                                                    selectedLocation = geoPoint
                                                    showIncidentReportDialog = true
                                                }
                                            },
                                            modifier = Modifier.padding(bottom = 8.dp),
                                            icon = { Icon(Icons.Default.MyLocation, contentDescription = "Use My Current Location") },
                                            text = { Text("Use My Location") }
                                        )
                                    }
                                    FloatingActionButton(
                                        onClick = {
                                            isReportMode = !isReportMode
                                            if (isReportMode) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Long press on the map to pin a location for the incident.")
                                                }
                                            } else {
                                                selectedLocation = null
                                            }
                                        },
                                        containerColor = if (isReportMode) Color.Red else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Icon(if(isReportMode) Icons.Default.Close else Icons.Default.Add, contentDescription = "Report an Incident")
                                    }
                                }
                            }
                        },
                        topBar = {
                            TopAppBar(
                                title = { Text(if (userRole == "guest") "Disaster Management" else "Incidents") },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (userRole == "admin") {
                                BottomAppBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.AdminPanelSettings, "Admin Panel") }, label = { Text("Admin Panel") },
                                        selected = currentRoute == "admin",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("admin")
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Map, "Map") }, label = { Text("Map") },
                                        selected = currentRoute == "main",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("main")
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, "Community Chat") }, label = { Text("Community Chat") },
                                        selected = currentRoute == "community_chat",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("community_chat")
                                        }
                                    )
                                }
                            } else if (userRole == "barangay") {
                                BottomAppBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.NotificationsActive, "Barangay Panel") }, label = { Text("Barangay") },
                                        selected = currentRoute == "barangay",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("barangay")
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Map, "Map") }, label = { Text("Map") },
                                        selected = currentRoute == "main",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("main")
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, "Community Chat") }, label = { Text("Community Chat") },
                                        selected = currentRoute == "community_chat",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("community_chat")
                                        }
                                    )
                                }
                            } else {
                                BottomAppBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.HowToVote, "Voting") }, label = { Text("Voting") },
                                        selected = currentRoute == "voting",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("voting")
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Map, "Map") }, label = { Text("Map") },
                                        selected = currentRoute == "main",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("main")
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, "Community Chat") }, label = { Text("Community Chat") },
                                        selected = currentRoute == "community_chat",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("community_chat")
                                        }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Column(modifier = Modifier.padding(paddingValues)) {
                            if (userRole == "barangay" && currentRoute == "voting") {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Search...") },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                )
                            }
                            NavGraph(
                                navController = navController,
                                userDao = userDao,
                                auditLogDao = auditLogDao,
                                incidentDao = incidentDao,
                                chatMessageDao = chatMessageDao,
                                locationPermissionGranted = locationPermissionGranted.value,
                                onLogin = { email, password ->
                                    scope.launch {
                                        isLoggingIn = true
                                        try {
                                            val user = userDao.getUserByEmail(email)
                                            if (user != null && user.password == password) {
                                                if (user.accountStatus == "Deactivated") {
                                                    // Don't log in deactivated users
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Account deactivated. Contact the admin.")
                                                    }
                                                } else {
                                                    userId = user.id.toString()
                                                    userEmail = user.email
                                                    userRole = user.role
                                                    userJoinDate = user.joinDate
                                                    userAccountStatus = user.accountStatus
                                                    userFullName = user.fullName
                                                    userPfpUrl = user.pfpUrl
                                                    if (user.role == "admin") {
                                                        navController.navigate("admin")
                                                    } else {
                                                        navController.navigate("main")
                                                    }
                                                }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Invalid email or password.")
                                                }
                                            }
                                        } finally {
                                            isLoggingIn = false
                                        }
                                    }
                                },
                                onGuestLogin = {
                                    userId = "guest"
                                    userEmail = "guest"
                                    userRole = "guest"
                                    userJoinDate = ""
                                    userAccountStatus = ""
                                    userFullName = "Guest"
                                    userPfpUrl = ""
                                    navController.navigate("main")
                                },
                                onRegister = { user ->
                                    scope.launch {
                                        isRegistering = true
                                        try {
                                            val existingUser = userDao.getUserByEmail(user.email)
                                            if (existingUser == null) {
                                                userDao.insertUser(user)
                                                // Audit: user registered (self-registration) with detailed logging
                                                auditLogDao.insertLog(
                                                    AuditLogHelper.createUserCreationLog(
                                                        user = user,
                                                        actorId = "self",
                                                        actorEmail = user.email
                                                    )
                                                )
                                                snackbarHostState.showSnackbar("Registration successful! Please login.")
                                                navController.navigate("login")
                                            } else {
                                                snackbarHostState.showSnackbar("User with this email already exists.")
                                            }
                                        } finally {
                                            isRegistering = false
                                        }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToLogin = { navController.navigate("login") },
                                onUpdateProfile = { fullName, password, pfpUri ->
                                    scope.launch {
                                        isUpdatingProfile = true
                                        try {
                                            val currentUser = userDao.getUserByEmail(userEmail)
                                            if (currentUser != null) {
                                                val pfpUrl = if (pfpUri != null) {
                                                    saveImageToInternalStorage(pfpUri)
                                                } else {
                                                    currentUser.pfpUrl
                                                }
                                                val updatedUser = currentUser.copy(
                                                    fullName = fullName,
                                                    password = if (password.isNotEmpty()) password else currentUser.password,
                                                    pfpUrl = pfpUrl
                                                )
                                                userDao.updateUser(updatedUser)
                                                chatMessageDao.updateUserPfp(currentUser.id.toString(), pfpUrl)
                                                // Audit: profile updated with detailed logging
                                                auditLogDao.insertLog(
                                                    AuditLogHelper.createUserUpdateLog(
                                                        oldUser = currentUser,
                                                        newUser = updatedUser,
                                                        actorId = currentUser.id.toString(),
                                                        actorEmail = currentUser.email
                                                    )
                                                )
                                                userFullName = fullName
                                                userPfpUrl = pfpUrl
                                                snackbarHostState.showSnackbar("Profile updated successfully.")
                                                navController.popBackStack()
                                            }
                                        } finally {
                                            isUpdatingProfile = false
                                        }
                                    }
                                },
                                onDeactivateAccount = {
                                    // Deactivate current user's account and log them out
                                    scope.launch {
                                        try {
                                            val currentUser = userDao.getUserByEmail(userEmail)
                                            if (currentUser != null) {
                                                // Prevent deactivating admin accounts from settings
                                                if (currentUser.role == "admin") {
                                                    snackbarHostState.showSnackbar("Cannot deactivate admin accounts.")
                                                } else {
                                                    userDao.updateAccountStatus(currentUser.id, "Deactivated")
                                                    // Audit: account deactivated by self with detailed logging
                                                    auditLogDao.insertLog(
                                                        AuditLogHelper.createUserDeactivationLog(
                                                            user = currentUser,
                                                            actorId = currentUser.id.toString(),
                                                            actorEmail = currentUser.email,
                                                            reason = "Self-deactivated by user"
                                                        )
                                                    )
                                                    // clear session and navigate to main
                                                    userRole = "guest"
                                                    userId = "guest"
                                                    userEmail = "guest"
                                                    userJoinDate = ""
                                                    userAccountStatus = ""
                                                    userFullName = "Guest"
                                                    userPfpUrl = ""
                                                    snackbarHostState.showSnackbar("Account deactivated.")
                                                    navController.navigate("main")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Failed to deactivate account.")
                                        }
                                    }
                                },
                                incidents = filteredIncidents,
                                users = users,
                                isReportMode = isReportMode,
                                selectedLocation = selectedLocation,
                                onLocationSelected = { location -> selectedLocation = location },
                                onConfirmIncident = { incident ->
                                    scope.launch {
                                        processingIncidentId = incident.id
                                        try {
                                            if (userRole != "guest" && !incident.confirmedBy.contains(userId)) {
                                                val updatedIncident = incident.copy(confirmedBy = incident.confirmedBy + userId)
                                                incidentDao.updateIncident(updatedIncident)
                                                // Audit: incident confirmed with detailed logging
                                                auditLogDao.insertLog(
                                                    AuditLogHelper.createIncidentConfirmationLog(
                                                        incident = incident,
                                                        confirmedBy = userId,
                                                        actorId = userId,
                                                        actorEmail = userEmail
                                                    )
                                                )
                                                snackbarHostState.showSnackbar("Incident confirmed.")
                                            }
                                        } finally {
                                            processingIncidentId = null
                                        }
                                    }
                                },
                                userId = userId,
                                userEmail = userEmail,
                                userRole = userRole,
                                userJoinDate = userJoinDate,
                                userAccountStatus = userAccountStatus,
                                userFullName = userFullName,
                                userPfpUrl = userPfpUrl,
                                mapCenter = mapCenter,
                                onMapCenterChange = { mapCenter = it },
                                onUpdateIncident = { incident, status, severity ->
                                    scope.launch {
                                        processingIncidentId = incident.id
                                        try {
                                            val updatedIncident = incident.copy(status = status, severity = severity)
                                            incidentDao.updateIncident(updatedIncident)
                                            // Audit: incident updated with detailed logging
                                            auditLogDao.insertLog(
                                                AuditLogHelper.createIncidentUpdateLog(
                                                    oldIncident = incident,
                                                    newIncident = updatedIncident,
                                                    actorId = userId,
                                                    actorEmail = userEmail
                                                )
                                            )
                                            snackbarHostState.showSnackbar("Incident updated.")
                                        } finally {
                                            processingIncidentId = null
                                        }
                                    }
                                },
                                onResolveIncident = { incident ->
                                    scope.launch {
                                        processingIncidentId = incident.id
                                        try {
                                            incidentDao.deleteIncident(incident)
                                            // Audit: incident deleted / resolved with detailed logging
                                            auditLogDao.insertLog(
                                                AuditLogHelper.createIncidentResolutionLog(
                                                    incident = incident,
                                                    actorId = userId,
                                                    actorEmail = userEmail,
                                                    reason = "Resolved"
                                                )
                                            )
                                            snackbarHostState.showSnackbar("Incident resolved.")
                                        } finally {
                                            processingIncidentId = null
                                        }
                                    }
                                },
                                onUnsendMessage = { message ->
                                    scope.launch {
                                        chatMessageDao.deleteMessage(message)
                                        // Audit: chat message deleted with detailed logging
                                        auditLogDao.insertLog(
                                            AuditLogHelper.createChatMessageDeletionLog(
                                                message = message,
                                                actorId = userId,
                                                actorEmail = userEmail
                                            )
                                        )
                                        snackbarHostState.showSnackbar("Message unsent.")
                                    }
                                },
                                updateUserPfp = { pfpUrl -> userPfpUrl = pfpUrl },
                                processingIncidentId = processingIncidentId,
                                isLoggingIn = isLoggingIn,
                                isRegistering = isRegistering,
                                isUpdatingProfile = isUpdatingProfile
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                // Inform Compose that permission is already granted so UI can enable location immediately
                locationPermissionGranted.value = true
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(filesDir, "${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        return file.absolutePath
    }
}