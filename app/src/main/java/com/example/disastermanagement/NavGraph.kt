package com.example.disastermanagement

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.disastermanagement.data.ChatMessage
import com.example.disastermanagement.data.database.AuditLogDao
import com.example.disastermanagement.data.database.ChatMessageDao
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.database.IncidentDao
import com.example.disastermanagement.data.database.User
import com.example.disastermanagement.data.database.UserDao
import com.example.disastermanagement.ui.screens.AccountSettingsScreen
import com.example.disastermanagement.ui.screens.AdminScreen
import com.example.disastermanagement.ui.screens.AuditLogsScreen
import com.example.disastermanagement.ui.screens.BarangayScreen
import com.example.disastermanagement.ui.screens.IncidentDetailScreen
import com.example.disastermanagement.ui.screens.IncidentLogsScreen
import com.example.disastermanagement.ui.screens.IncidentLogsScreen
import com.example.disastermanagement.ui.screens.LoginScreen
import com.example.disastermanagement.ui.screens.MainScreen
import com.example.disastermanagement.ui.screens.ManageIncidentsScreen
import com.example.disastermanagement.ui.screens.RegisterScreen
import com.example.disastermanagement.ui.screens.StatisticsScreen
import com.example.disastermanagement.ui.screens.UserManagementScreen
import com.example.disastermanagement.ui.screens.VotingScreen
import com.example.disastermanagement.ui.screens.communitychat.CommunityChatScreen
import com.example.disastermanagement.ui.screens.communitychat.CommunityChatViewModel
import com.example.disastermanagement.ui.screens.communitychat.CommunityChatViewModelFactory
import org.osmdroid.util.GeoPoint
import androidx.compose.ui.platform.LocalContext

@Composable
fun NavGraph(
    navController: NavHostController,
    userDao: UserDao,
    auditLogDao: AuditLogDao,
    incidentDao: IncidentDao,
    chatMessageDao: ChatMessageDao,
    locationPermissionGranted: Boolean,
    onLogin: (String, String) -> Unit,
    onGuestLogin: () -> Unit,
    onRegister: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onUpdateProfile: (fullName: String, password: String, pfpUri: Uri?) -> Unit,
    onDeactivateAccount: () -> Unit,
    incidents: List<Incident>,
    users: List<User>,
    isReportMode: Boolean,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    onConfirmIncident: (Incident) -> Unit,
    userId: String,
    userEmail: String,
    userRole: String,
    userJoinDate: String,
    userAccountStatus: String,
    userFullName: String,
    userPfpUrl: String,
    mapCenter: GeoPoint?,
    onMapCenterChange: (GeoPoint?) -> Unit,
    onUpdateIncident: (Incident, String, String) -> Unit,
    onResolveIncident: (Incident) -> Unit,
    onUnsendMessage: (ChatMessage) -> Unit,
    updateUserPfp: (pfpUrl: String) -> Unit,
    processingIncidentId: Int?,
    isLoggingIn: Boolean,
    isRegistering: Boolean,
    isUpdatingProfile: Boolean
) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                navController = navController,
                incidents = incidents,
                users = users,
                isReportMode = isReportMode,
                selectedLocation = selectedLocation,
                onLocationSelected = onLocationSelected,
                mapCenter = mapCenter,
                onMapCenterChange = onMapCenterChange,
                locationPermissionGranted = locationPermissionGranted,
                userId = userId,
                userRole = userRole,
                onConfirmIncident = onConfirmIncident,
                onUpdateIncident = { incident, status, severity ->
                    onUpdateIncident(incident, status, severity)
                },
                onResolveIncident = {
                    onResolveIncident(it)
                },
                processingIncidentId = processingIncidentId
            )
        }
        composable("login") { 
            LoginScreen(
                onLogin = onLogin, 
                onGuestLogin = onGuestLogin,
                onNavigateToRegister = onNavigateToRegister,
                isLoggingIn = isLoggingIn
            ) 
        }
        composable("register") { 
            RegisterScreen(
                onRegister = onRegister, 
                onNavigateToLogin = onNavigateToLogin,
                isRegistering = isRegistering
            ) 
        }
        composable("admin") { AdminScreen(navController, userDao) }
        composable("barangay") {
            val ctx = LocalContext.current
            BarangayScreen(navController = navController, userDao = userDao, auditLogDao = auditLogDao, context = ctx)
        }
        composable("user_management") { UserManagementScreen(userDao = userDao, auditLogDao = auditLogDao) }
        composable("voting") {
            VotingScreen(
                navController = navController,
                incidents = incidents,
                users = users
            )
        }
        composable("community_chat") { 
            val viewModel: CommunityChatViewModel = viewModel(factory = CommunityChatViewModelFactory(chatMessageDao))
            val messages by viewModel.messages.collectAsState(initial = emptyList())
            val isSendingMessage by viewModel.isSendingMessage.collectAsState()
            CommunityChatScreen(
                messages = messages,
                onSendMessage = { message, isAnnouncement ->
                    viewModel.sendMessage(message, userId, userFullName, userPfpUrl, userRole, isAnnouncement)
                },
                onSendReply = { message, parentId ->
                    viewModel.sendReply(message, parentId, userId, userFullName, userPfpUrl, userRole)
                },
                onLike = { message -> viewModel.likeMessage(message, userId) },
                onDislike = { message -> viewModel.dislikeMessage(message, userId) },
                userRole = userRole,
                onUnsendMessage = onUnsendMessage,
                userId = userId,
                isSendingMessage = isSendingMessage,
                userFullName = userFullName
            ) 
        }
        composable("manage_incidents") {
            ManageIncidentsScreen(
                navController = navController,
                incidents = incidents,
                users = users
            )
        }
        composable("audit_logs") {
            val ctx = LocalContext.current
            AuditLogsScreen(auditLogDao = auditLogDao, context = ctx)
        }
        composable("incident_logs") {
            val ctx = LocalContext.current
            IncidentLogsScreen(auditLogDao = auditLogDao, context = ctx)
        }
        composable("statistics") {
            StatisticsScreen(incidentDao = incidentDao, userDao = userDao)
        }
        composable(
            "incident_detail/{incidentId}",
            arguments = listOf(navArgument("incidentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getInt("incidentId")
            val incident = incidents.find { it.id == incidentId }
            if (incident != null) {
                val reporter = users.find { it.id.toString() == incident.reporterId }

                IncidentDetailScreen(
                    incident = incident, 
                    userId = userId, 
                    userRole = userRole,
                    reporter = reporter,
                    onConfirm = { onConfirmIncident(incident) },
                    onDismiss = { navController.popBackStack() },
                    onUpdateIncident = { status, severity ->
                        onUpdateIncident(incident, status, severity)
                    },
                    onResolve = { 
                        onResolveIncident(incident)
                    },
                    isProcessing = processingIncidentId == incident.id
                )
            }
        }
        composable("account_settings") {
            AccountSettingsScreen(
                userEmail = userEmail,
                userRole = userRole,
                userJoinDate = userJoinDate,
                userAccountStatus = userAccountStatus,
                userFullName = userFullName,
                userPfpUrl = userPfpUrl,
                onUpdateProfile = {
                    fullName, password, pfpUri -> onUpdateProfile(fullName, password, pfpUri)
                    pfpUri?.let { updateUserPfp(it.toString()) } 
                },
                onDeactivateAccount = onDeactivateAccount,
                isUpdatingProfile = isUpdatingProfile
            )
        }
    }
}