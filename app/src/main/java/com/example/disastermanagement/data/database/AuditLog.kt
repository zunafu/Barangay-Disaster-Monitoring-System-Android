package com.example.disastermanagement.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val actorId: String,
    val actorEmail: String,
    val actionType: String, // CREATE, UPDATE, DELETE, DEACTIVATE, REACTIVATE, RESOLVE, etc.
    val targetType: String, // User, Incident, ChatMessage, etc.
    val targetId: String,
    val details: String = "", // General details
    val changesSummary: String = "", // Summary of specific changes made
    val beforeValues: String = "", // JSON serialized map of before values for updates
    val afterValues: String = "" // JSON serialized map of after values for updates
)

