package com.example.disastermanagement.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Insert
    suspend fun insertIncident(incident: Incident)

    @Update
    suspend fun updateIncident(incident: Incident)

    @Delete
    suspend fun deleteIncident(incident: Incident)

    @Query("SELECT * FROM incidents")
    fun getAllIncidents(): Flow<List<Incident>>

    @Query("SELECT * FROM incidents WHERE timestamp >= :sevenDaysAgo")
    fun getRecentIncidents(sevenDaysAgo: Long): Flow<List<Incident>>
}