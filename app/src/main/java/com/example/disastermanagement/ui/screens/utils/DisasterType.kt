package com.example.disastermanagement.ui.screens.utils

object DisasterType {
    val disasterTypes = mapOf(
        "Fire" to "🔥",
        "Flood" to "🌊",
        "Earthquake" to "🏚️",
        "Typhoon" to "🌀",
        "Landslide" to "⛰️",
        "Accident" to "🚗",
        "Other" to "❓"
    )

    fun getDisasterEmoji(type: String): String {
        return disasterTypes[type] ?: "❓"
    }
}
