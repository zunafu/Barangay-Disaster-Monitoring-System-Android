package com.example.disastermanagement.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val fullName: String,
    val role: String, // "user", "barangay", or "admin"
    val password: String,
    val pfpUrl: String = "",
    val joinDate: String = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(Date()),
    val accountStatus: String = "Verified"
)
