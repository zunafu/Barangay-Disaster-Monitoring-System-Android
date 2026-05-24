package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.disastermanagement.data.database.AuditLogDao
import com.example.disastermanagement.data.database.UserDao
import android.content.Context

@Composable
fun BarangayScreen(
    navController: NavController,
    userDao: UserDao,
    auditLogDao: AuditLogDao,
    context: Context
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Manage Incidents button for barangay users
        Button(
            onClick = { navController.navigate("manage_incidents") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Manage Incidents")
        }

        // Incident Logs button below Manage Incidents
        Button(
            onClick = { navController.navigate("incident_logs") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Incident Logs")
        }

        // Note: Audit Logs button intentionally removed for barangay role
    }
}