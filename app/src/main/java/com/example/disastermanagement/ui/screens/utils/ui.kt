package com.example.disastermanagement.ui.screens.utils

import androidx.compose.ui.graphics.Color

fun getSeverityColor(severity: String): Color {
    return when (severity.lowercase()) {
        "low" -> Color.Green
        "medium" -> Color.Yellow
        "high" -> Color(0xFFFFA500) // Orange
        "critical" -> Color.Red
        else -> Color.Gray
    }
}

fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "reported" -> Color.Gray
        "responding" ->  Color(0xFFFFA500)
        "in area" -> Color.Blue // Orange
        "resolved" -> Color.Green
        else -> Color.DarkGray
    }
}
