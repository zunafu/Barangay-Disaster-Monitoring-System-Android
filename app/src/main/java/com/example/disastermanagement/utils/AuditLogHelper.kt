package com.example.disastermanagement.utils

import com.example.disastermanagement.data.database.AuditLog
import com.example.disastermanagement.data.database.User
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.ChatMessage
import org.json.JSONObject

/**
 * Helper utility for creating detailed audit logs with comprehensive change tracking
 */
object AuditLogHelper {

    /**
     * Create an audit log for user creation with full details
     */
    fun createUserCreationLog(
        user: User,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        val afterValues = mapOf(
            "email" to user.email,
            "fullName" to user.fullName,
            "role" to user.role,
            "joinDate" to user.joinDate,
            "accountStatus" to user.accountStatus
        )

        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "CREATE",
            targetType = "User",
            targetId = "${user.id}",
            details = "New user created: ${user.fullName} (${user.email})",
            changesSummary = "Created new user with role=${user.role}",
            afterValues = mapToJsonString(afterValues)
        )
    }

    /**
     * Create an audit log for user update with before and after values
     */
    fun createUserUpdateLog(
        oldUser: User,
        newUser: User,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        val changes = mutableListOf<String>()
        val beforeValues = mutableMapOf<String, String>()
        val afterValues = mutableMapOf<String, String>()

        if (oldUser.fullName != newUser.fullName) {
            changes.add("fullName: '${oldUser.fullName}' → '${newUser.fullName}'")
            beforeValues["fullName"] = oldUser.fullName
            afterValues["fullName"] = newUser.fullName
        }
        if (oldUser.role != newUser.role) {
            changes.add("role: '${oldUser.role}' → '${newUser.role}'")
            beforeValues["role"] = oldUser.role
            afterValues["role"] = newUser.role
        }
        if (oldUser.password != newUser.password) {
            changes.add("password: [CHANGED]")
            beforeValues["password"] = "[REDACTED]"
            afterValues["password"] = "[REDACTED]"
        }
        if (oldUser.pfpUrl != newUser.pfpUrl) {
            changes.add("profilePicture: [UPDATED]")
            beforeValues["pfpUrl"] = oldUser.pfpUrl.take(50) // Limit length
            afterValues["pfpUrl"] = newUser.pfpUrl.take(50)
        }
        if (oldUser.accountStatus != newUser.accountStatus) {
            changes.add("accountStatus: '${oldUser.accountStatus}' → '${newUser.accountStatus}'")
            beforeValues["accountStatus"] = oldUser.accountStatus
            afterValues["accountStatus"] = newUser.accountStatus
        }

        val changesSummary = if (changes.isEmpty()) "No changes" else changes.joinToString(", ")

        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "UPDATE",
            targetType = "User",
            targetId = "${newUser.id}",
            details = "Updated user ${newUser.fullName} (${newUser.email})",
            changesSummary = changesSummary,
            beforeValues = mapToJsonString(beforeValues),
            afterValues = mapToJsonString(afterValues)
        )
    }

    /**
     * Create an audit log for user deactivation
     */
    fun createUserDeactivationLog(
        user: User,
        actorId: String,
        actorEmail: String,
        reason: String = "Manual deactivation"
    ): AuditLog {
        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "DEACTIVATE",
            targetType = "User",
            targetId = "${user.id}",
            details = "User account deactivated: ${user.fullName} (${user.email})",
            changesSummary = "accountStatus: 'Verified' → 'Deactivated'",
            beforeValues = mapToJsonString(mapOf("accountStatus" to "Verified")),
            afterValues = mapToJsonString(mapOf("accountStatus" to "Deactivated", "reason" to reason))
        )
    }

    /**
     * Create an audit log for user reactivation
     */
    fun createUserReactivationLog(
        user: User,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "REACTIVATE",
            targetType = "User",
            targetId = "${user.id}",
            details = "User account reactivated: ${user.fullName} (${user.email})",
            changesSummary = "accountStatus: 'Deactivated' → 'Verified'",
            beforeValues = mapToJsonString(mapOf("accountStatus" to "Deactivated")),
            afterValues = mapToJsonString(mapOf("accountStatus" to "Verified"))
        )
    }

    /**
     * Create an audit log for user deletion
     */
    fun createUserDeletionLog(
        user: User,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "DELETE",
            targetType = "User",
            targetId = "${user.id}",
            details = "User account deleted: ${user.fullName} (${user.email})",
            changesSummary = "User permanently removed from system",
            beforeValues = mapToJsonString(mapOf(
                "email" to user.email,
                "fullName" to user.fullName,
                "role" to user.role
            ))
        )
    }

    /**
     * Create an audit log for incident creation
     */
    fun createIncidentCreationLog(
        incident: Incident,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        val afterValues = mapOf(
            "type" to incident.type,
            "title" to incident.title,
            "description" to incident.description.take(100),
            "severity" to incident.severity,
            "status" to incident.status,
            "location" to "${incident.location.latitude},${incident.location.longitude}"
        )

        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "CREATE",
            targetType = "Incident",
            targetId = "${incident.id}",
            details = "New incident reported: ${incident.title}",
            changesSummary = "Created incident type=${incident.type}, severity=${incident.severity}",
            afterValues = mapToJsonString(afterValues)
        )
    }

    /**
     * Create an audit log for incident update with before and after values
     */
    fun createIncidentUpdateLog(
        oldIncident: Incident,
        newIncident: Incident,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        val changes = mutableListOf<String>()
        val beforeValues = mutableMapOf<String, String>()
        val afterValues = mutableMapOf<String, String>()

        if (oldIncident.status != newIncident.status) {
            changes.add("status: '${oldIncident.status}' → '${newIncident.status}'")
            beforeValues["status"] = oldIncident.status
            afterValues["status"] = newIncident.status
        }
        if (oldIncident.severity != newIncident.severity) {
            changes.add("severity: '${oldIncident.severity}' → '${newIncident.severity}'")
            beforeValues["severity"] = oldIncident.severity
            afterValues["severity"] = newIncident.severity
        }
        if (oldIncident.description != newIncident.description) {
            changes.add("description: [UPDATED]")
            beforeValues["description"] = oldIncident.description.take(100)
            afterValues["description"] = newIncident.description.take(100)
        }
        if (oldIncident.confirmedBy.size != newIncident.confirmedBy.size) {
            changes.add("confirmations: ${oldIncident.confirmedBy.size} → ${newIncident.confirmedBy.size}")
            beforeValues["confirmationCount"] = "${oldIncident.confirmedBy.size}"
            afterValues["confirmationCount"] = "${newIncident.confirmedBy.size}"
        }

        val changesSummary = if (changes.isEmpty()) "No changes" else changes.joinToString(", ")

        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "UPDATE",
            targetType = "Incident",
            targetId = "${newIncident.id}",
            details = "Updated incident: ${newIncident.title}",
            changesSummary = changesSummary,
            beforeValues = mapToJsonString(beforeValues),
            afterValues = mapToJsonString(afterValues)
        )
    }

    /**
     * Create an audit log for incident confirmation
     */
    fun createIncidentConfirmationLog(
        incident: Incident,
        confirmedBy: String,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "CONFIRM",
            targetType = "Incident",
            targetId = "${incident.id}",
            details = "User confirmed incident: ${incident.title}",
            changesSummary = "Added confirmation from user $confirmedBy, total confirmations: ${incident.confirmedBy.size + 1}",
            beforeValues = mapToJsonString(mapOf("confirmationCount" to "${incident.confirmedBy.size}")),
            afterValues = mapToJsonString(mapOf("confirmationCount" to "${incident.confirmedBy.size + 1}"))
        )
    }

    /**
     * Create an audit log for incident resolution/deletion
     */
    fun createIncidentResolutionLog(
        incident: Incident,
        actorId: String,
        actorEmail: String,
        reason: String = "Resolved"
    ): AuditLog {
        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "DELETE",
            targetType = "Incident",
            targetId = "${incident.id}",
            details = "Incident resolved and removed: ${incident.title}",
            changesSummary = "Incident status changed to resolved - $reason",
            beforeValues = mapToJsonString(mapOf(
                "status" to incident.status,
                "severity" to incident.severity,
                "type" to incident.type
            )),
            afterValues = mapToJsonString(mapOf("status" to "Resolved", "reason" to reason))
        )
    }

    /**
     * Create an audit log for chat message deletion
     */
    fun createChatMessageDeletionLog(
        message: ChatMessage,
        actorId: String,
        actorEmail: String
    ): AuditLog {
        return AuditLog(
            timestamp = System.currentTimeMillis(),
            actorId = actorId,
            actorEmail = actorEmail,
            actionType = "DELETE",
            targetType = "ChatMessage",
            targetId = "${message.messageId}",
            details = "Chat message deleted by ${message.senderName}",
            changesSummary = "Message content removed from community chat",
            beforeValues = mapToJsonString(mapOf(
                "message" to message.message.take(100),
                "sender" to message.senderName,
                "timestamp" to "${message.timestamp}"
            ))
        )
    }

    /**
     * Convert a map to JSON string
     */
    private fun mapToJsonString(map: Map<String, String>): String {
        return if (map.isEmpty()) {
            ""
        } else {
            try {
                JSONObject(map).toString()
            } catch (e: Exception) {
                map.entries.joinToString(", ") { "${it.key}=${it.value}" }
            }
        }
    }
}




