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
import com.example.disastermanagement.data.database.UserDao

@Composable
fun AdminScreen(navController: NavController, userDao: UserDao) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { navController.navigate("user_management") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Manage Users")
        }
        Button(
            onClick = { navController.navigate("manage_incidents") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Manage Incidents")
        }
        Button(
            onClick = { navController.navigate("statistics") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Manage Statistics")
        }
        Button(
            onClick = { navController.navigate("audit_logs") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Audit Logs")
        }
    }
}
