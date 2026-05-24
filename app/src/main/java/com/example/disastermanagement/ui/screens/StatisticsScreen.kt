package com.example.disastermanagement.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.database.IncidentDao
import com.example.disastermanagement.data.database.UserDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsViewModel(incidentDao: IncidentDao, userDao: UserDao) : ViewModel() {
    val users = userDao.getAllUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val incidents = incidentDao.getAllIncidents().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentIncidents = incidentDao.getRecentIncidents(getSevenDaysAgoTimestamp()).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getSevenDaysAgoTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        return calendar.timeInMillis
    }
}

@Composable
fun StatisticsScreen(incidentDao: IncidentDao, userDao: UserDao) {
    val viewModel: StatisticsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatisticsViewModel(incidentDao, userDao) as T
        }
    })
    val users by viewModel.users.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val recentIncidents by viewModel.recentIncidents.collectAsState()

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            StatisticsSummary(users.size, incidents.size, incidents.count { !it.isResolved })
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Incidents (Last 7 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(recentIncidents) { incident ->
            RecentIncidentItem(incident)
        }
    }
}

@Composable
fun StatisticsSummary(totalUsers: Int, totalIncidents: Int, activeIncidents: Int) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatisticCard("Total Users", totalUsers.toString(), Modifier.weight(1f))
        StatisticCard("Total Incidents", totalIncidents.toString(), Modifier.weight(1f))
        StatisticCard("Active Incidents", activeIncidents.toString(), Modifier.weight(1f))
    }
}

@Composable
fun StatisticCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentIncidentItem(incident: Incident) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ID: ${incident.id}", style = MaterialTheme.typography.bodySmall)
            Text(incident.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Type: ${incident.type}", style = MaterialTheme.typography.bodyMedium)
            Text("Reporter: ${incident.reportedBy}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${incident.status}", style = MaterialTheme.typography.bodyMedium)
            Text("Severity: ${incident.severity}", style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${SimpleDateFormat("M/dd/yyyy, hh:mm a", Locale.getDefault()).format(incident.timestamp)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}