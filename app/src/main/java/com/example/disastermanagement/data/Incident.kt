package com.example.disastermanagement.data

import org.osmdroid.util.GeoPoint

data class Incident(
    val type: String,
    val severity: String,
    val description: String,
    val location: GeoPoint,
    val photos: List<String> = emptyList() // List of photo URLs
)