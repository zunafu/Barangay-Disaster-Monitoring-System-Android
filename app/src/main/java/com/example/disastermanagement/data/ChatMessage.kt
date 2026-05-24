package com.example.disastermanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "chat_messages")
@TypeConverters(StringListConverter::class)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val messageId: Long = 0,
    val parentId: Long? = null,
    val message: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPfpUrl: String = "",
    val senderRole: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList(),
    val isAnnouncement: Boolean = false
)

class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }
}

data class ChatMessageThread(
    val parent: ChatMessage,
    val replies: List<ChatMessage>
)
