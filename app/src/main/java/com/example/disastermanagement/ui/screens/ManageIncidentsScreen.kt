package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.database.User

@Composable
fun ManageIncidentsScreen(
    navController: NavHostController,
    incidents: List<Incident>,
    users: List<User>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(incidents) { index, incident ->
            val reporter = users.find { user -> user.id.toString() == incident.reporterId }
            IncidentCard(incident = incident, reporter = reporter, index = index + 1) {
                navController.navigate("incident_detail/${incident.id}")
            }
        }
    }
}
