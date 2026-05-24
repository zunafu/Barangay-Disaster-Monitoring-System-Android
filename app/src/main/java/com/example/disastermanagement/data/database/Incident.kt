package com.example.disastermanagement.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.disastermanagement.data.database.converters.GeoPointConverter
import com.example.disastermanagement.data.database.converters.StringListConverter
import org.osmdroid.util.GeoPoint

@Entity(tableName = "incidents")
@TypeConverters(GeoPointConverter::class, StringListConverter::class)
data class Incident(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val reporterId: String,
    val reportedBy: String,
    val type: String,
    val title: String,
    val description: String,
    val location: GeoPoint,
    val imageUri: String? = null,
    val confirmedBy: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Reported",
    val severity: String = "None",
    val isResolved: Boolean = false
)
