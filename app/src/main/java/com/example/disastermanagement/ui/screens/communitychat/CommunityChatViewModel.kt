package com.example.disastermanagement.ui.screens.communitychat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disastermanagement.data.ChatMessage
import com.example.disastermanagement.data.ChatMessageThread
import com.example.disastermanagement.data.database.ChatMessageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CommunityChatViewModel(private val chatMessageDao: ChatMessageDao) : ViewModel() {

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    val messages: Flow<List<ChatMessageThread>> = chatMessageDao.getAllMessages().map {
        it.filter { message -> message.parentId == null }
            .map { parent ->
                ChatMessageThread(
                    parent = parent,
                    replies = it.filter { reply -> reply.parentId == parent.messageId }
                )
            }
    }

    fun sendMessage(
        message: String, 
        senderId: String, 
        senderName: String, 
        senderPfpUrl: String, 
        senderRole: String,
        isAnnouncement: Boolean
    ) {
        viewModelScope.launch {
            _isSendingMessage.value = true
            try {
                val chatMessage = ChatMessage(
                    message = message,
                    senderId = senderId,
                    senderName = senderName,
                    senderPfpUrl = senderPfpUrl,
                    senderRole = senderRole,
                    isAnnouncement = isAnnouncement
                )
                chatMessageDao.insertMessage(chatMessage)
            } finally {
                _isSendingMessage.value = false
            }
        }
    }

    fun sendReply(message: String, parentId: Long, senderId: String, senderName: String, senderPfpUrl: String, senderRole: String) {
        viewModelScope.launch {
            val chatMessage = ChatMessage(
                message = message,
                parentId = parentId,
                senderId = senderId,
                senderName = senderName,
                senderPfpUrl = senderPfpUrl,
                senderRole = senderRole
            )
            chatMessageDao.insertMessage(chatMessage)
        }
    }

    fun likeMessage(message: ChatMessage, userId: String) {
        viewModelScope.launch {
            val likedBy = message.likedBy.toMutableList()
            val dislikedBy = message.dislikedBy.toMutableList()

            if (likedBy.contains(userId)) {
                likedBy.remove(userId)
            } else {
                likedBy.add(userId)
                dislikedBy.remove(userId)
            }

            val updatedMessage = message.copy(
                likes = likedBy.size,
                dislikes = dislikedBy.size,
                likedBy = likedBy,
                dislikedBy = dislikedBy
            )
            chatMessageDao.updateMessage(updatedMessage)
        }
    }

    fun dislikeMessage(message: ChatMessage, userId: String) {
        viewModelScope.launch {
            val likedBy = message.likedBy.toMutableList()
            val dislikedBy = message.dislikedBy.toMutableList()

            if (dislikedBy.contains(userId)) {
                dislikedBy.remove(userId)
            } else {
                dislikedBy.add(userId)
                likedBy.remove(userId)
            }

            val updatedMessage = message.copy(
                likes = likedBy.size,
                dislikes = dislikedBy.size,
                likedBy = likedBy,
                dislikedBy = dislikedBy
            )
            chatMessageDao.updateMessage(updatedMessage)
        }
    }

    fun unsendMessage(message: ChatMessage) {
        viewModelScope.launch {
            chatMessageDao.deleteMessage(message)
        }
    }
}
