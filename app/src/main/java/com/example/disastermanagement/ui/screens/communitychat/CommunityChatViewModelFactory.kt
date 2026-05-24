package com.example.disastermanagement.ui.screens.communitychat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.disastermanagement.data.database.ChatMessageDao

class CommunityChatViewModelFactory(private val chatMessageDao: ChatMessageDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityChatViewModel(chatMessageDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
