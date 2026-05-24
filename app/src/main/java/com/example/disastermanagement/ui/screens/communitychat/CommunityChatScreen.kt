package com.example.disastermanagement.ui.screens.communitychat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.disastermanagement.data.ChatMessage
import com.example.disastermanagement.data.ChatMessageThread
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityChatScreen(
    messages: List<ChatMessageThread>,
    onSendMessage: (String, Boolean) -> Unit,
    onSendReply: (String, Long) -> Unit,
    onLike: (ChatMessage) -> Unit,
    onDislike: (ChatMessage) -> Unit,
    userRole: String,
    onUnsendMessage: (ChatMessage) -> Unit,
    userId: String,
    isSendingMessage: Boolean,
    userFullName: String
) {
    var messageText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Long?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Public Chat", "Announcements")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Show informational note when Announcements tab is active
        if (selectedTabIndex == 1) {
            Text(
                text = "Note: Only Admin and Barangay users can post announcements.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            val filteredMessages = messages.filter { 
                if (selectedTabIndex == 0) !it.parent.isAnnouncement else it.parent.isAnnouncement
            }
            items(filteredMessages) {
                ChatMessageThreadItem(
                    thread = it, 
                    onLike = onLike, 
                    onDislike = onDislike, 
                    userRole = userRole, 
                    onSendReply = onSendReply, 
                    onUnsendMessage = onUnsendMessage, 
                    userId = userId,
                    replyingTo = replyingTo,
                    onReplyingToChange = { replyingTo = it },
                    userFullName = userFullName
                )
            }
        }

        if (userRole != "guest" && (selectedTabIndex == 0 || (userRole == "barangay" || userRole == "admin"))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") },
                    enabled = !isSendingMessage
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSendMessage(messageText, selectedTabIndex == 1)
                        messageText = ""
                    },
                    enabled = !isSendingMessage && messageText.isNotEmpty()
                ) {
                    if (isSendingMessage) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageThreadItem(
    thread: ChatMessageThread,
    onLike: (ChatMessage) -> Unit,
    onDislike: (ChatMessage) -> Unit,
    userRole: String,
    onSendReply: (String, Long) -> Unit,
    onUnsendMessage: (ChatMessage) -> Unit,
    userId: String,
    replyingTo: Long?,
    onReplyingToChange: (Long?) -> Unit,
    userFullName: String
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column {
            ChatMessageItem(thread.parent, onLike, onDislike, userRole, onSendReply, onUnsendMessage, userId, replyingTo, onReplyingToChange, userFullName)
            Column(modifier = Modifier.padding(start = 32.dp)) {
                thread.replies.forEach { reply ->
                    ChatMessageItem(reply, onLike, onDislike, userRole, onSendReply, onUnsendMessage, userId, replyingTo, onReplyingToChange, userFullName)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onLike: (ChatMessage) -> Unit,
    onDislike: (ChatMessage) -> Unit,
    userRole: String,
    onSendReply: (String, Long) -> Unit,
    onUnsendMessage: (ChatMessage) -> Unit,
    userId: String,
    replyingTo: Long?,
    onReplyingToChange: (Long?) -> Unit,
    userFullName: String
) {
    var replyText by remember { mutableStateOf("") }
    var showUnsendDialog by remember { mutableStateOf(false) }

    if (showUnsendDialog) {
        AlertDialog(
            onDismissRequest = { showUnsendDialog = false },
            title = { Text("Unsend Message") },
            text = { Text("Are you sure you want to unsend this message?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onUnsendMessage(message)
                        showUnsendDialog = false 
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnsendDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (message.senderId == userId) {
                        showUnsendDialog = true
                    }
                }
            )
    ) {
        if (message.senderPfpUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(message.senderPfpUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.first().toString(),
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (message.senderId == userId) userFullName else message.senderName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )
                if (message.isAnnouncement) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text("Announcement")
                    }
                } else if (message.senderRole == "barangay" || message.senderRole == "admin") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Badge(
                        containerColor = if (message.senderRole == "admin") Color(0xFF8B0000) else MaterialTheme.colorScheme.primary
                    ) {
                        Text(message.senderRole)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.message)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onLike(message) }, enabled = userRole != "guest") {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Like"
                    )
                }
                Text(text = message.likes.toString())
                IconButton(onClick = { onDislike(message) }, enabled = userRole != "guest") {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Dislike"
                    )
                }
                Text(text = message.dislikes.toString())
                if (userRole != "guest") {
                    IconButton(onClick = { 
                        if (replyingTo == message.messageId) {
                            onReplyingToChange(null)
                        } else {
                            onReplyingToChange(message.messageId)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = "Reply"
                        )
                    }
                }
            }
            if (replyingTo == message.messageId) {
                Row {
                    TextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Reply to ${message.senderName}") }
                    )
                    Button(onClick = {
                        onSendReply(replyText, message.messageId)
                        replyText = ""
                        onReplyingToChange(null)
                    }) {
                        Text("Send")
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date = Date(timestamp)
    return sdf.format(date)
}