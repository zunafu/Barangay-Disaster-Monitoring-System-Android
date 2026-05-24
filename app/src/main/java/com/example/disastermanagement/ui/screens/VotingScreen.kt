package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.database.User
import com.example.disastermanagement.ui.screens.utils.DisasterType
import com.example.disastermanagement.ui.screens.utils.getAddressFromLocation
import com.example.disastermanagement.ui.screens.utils.getSeverityColor
import com.example.disastermanagement.ui.screens.utils.getStatusColor

@Composable
fun VotingScreen(
    navController: NavHostController,
    incidents: List<Incident>,
    users: List<User>
) {
    val sortedIncidents = incidents.sortedByDescending { it.confirmedBy.size }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(sortedIncidents) { index, incident ->
            val reporter = users.find { user -> user.id.toString() == incident.reporterId }
            IncidentCard(incident = incident, reporter = reporter, index = index + 1) {
                navController.navigate("incident_detail/${incident.id}")
            }
        }
    }
}

@Composable
fun IncidentCard(incident: Incident, reporter: User?, index: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    val address by produceState(initialValue = "Loading address...", incident.location) {
        value = getAddressFromLocation(context, incident.location)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = DisasterType.getDisasterEmoji(incident.type),
                fontSize = 48.sp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = incident.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${incident.type} • $address", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reporter?.pfpUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = reporter.pfpUrl,
                            contentDescription = "Reporter's profile picture",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    } else if (reporter != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = reporter.fullName.first().toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Reported by: ${reporter?.fullName ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    StatusChip(label = incident.status.uppercase(), color = getStatusColor(incident.status))
                    Spacer(modifier = Modifier.height(4.dp))
                    if (incident.severity.lowercase() != "none") {
                        StatusChip(label = incident.severity.uppercase(), color = getSeverityColor(incident.severity))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "#$index", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${incident.confirmedBy.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Confirms", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color = color, shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}