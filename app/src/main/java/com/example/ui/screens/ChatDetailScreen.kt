package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MessageEntity
import com.example.ui.components.ConversationStarters
import com.example.ui.components.SubscriptionBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.MatchesViewModel
import com.example.ui.viewmodel.SafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    matchesViewModel: MatchesViewModel,
    safetyViewModel: SafetyViewModel,
    onBack: () -> Unit
) {
    val chatMatchWithProfile by matchesViewModel.currentChatMatchWithProfile.collectAsState()
    val messages by matchesViewModel.currentChatMessages.collectAsState()
    val typedText by matchesViewModel.typedMessage.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    val otherProfile = chatMatchWithProfile?.otherProfile

    Scaffold(
        containerColor = Color(0xFF090A0E),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090A0E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    if (otherProfile != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = otherProfile.primaryPhoto,
                                contentDescription = otherProfile.fullName,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFF282A3A), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = otherProfile.fullName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (otherProfile.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerificationBadge()
                                    }
                                    if (otherProfile.isPremium) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        SubscriptionBadge(text = "PRO")
                                    }
                                }
                                Text(
                                    text = "Active in ${otherProfile.localChurch}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    } else {
                        Text("AdventHearts Chat", color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Safety menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF161822))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report User", color = Color.White) },
                            onClick = {
                                showMenu = false
                                if (otherProfile != null) {
                                    safetyViewModel.reportUser(
                                        reportedUserId = otherProfile.userId,
                                        reason = "Inappropriate behavior",
                                        details = "Reported from chat detail"
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Unmatch", color = Color.White) },
                            onClick = {
                                showMenu = false
                                chatMatchWithProfile?.match?.matchId?.let { matchesViewModel.unmatch(it) }
                                onBack()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Block & Unmatch User", color = Color(0xFFFF3366)) },
                            onClick = {
                                showMenu = false
                                if (otherProfile != null) {
                                    safetyViewModel.blockUser(otherProfile.userId)
                                    chatMatchWithProfile?.match?.matchId?.let { matchesViewModel.unmatch(it) }
                                    onBack()
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF090A0E))
                .testTag("chat_detail_screen")
        ) {
            // Safety Tip Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161822)
                ),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(0.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = VerifiedBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Safety Tip: Keep conversations on AdventHearts until trust is established.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Chat Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    if (chatMatchWithProfile?.match?.conversationStarter?.isNotBlank() == true) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF161822)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = "💡 Conversation Insight: ${chatMatchWithProfile!!.match.conversationStarter}",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    val isMe = msg.senderId == matchesViewModel.currentUserId()
                    MessageBubble(message = msg, isFromMe = isMe)
                }
            }

            // Adventist Conversation Starters Chip Bar
            ConversationStarters(
                onSelectPrompt = { promptText ->
                    matchesViewModel.sendPromptAsMessage(promptText)
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { matchesViewModel.updateTypedMessage(it) },
                    placeholder = { Text("Type a respectful message...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VerifiedBlue,
                        unfocusedBorderColor = Color(0xFF282A3A),
                        focusedContainerColor = Color(0xFF161822),
                        unfocusedContainerColor = Color(0xFF161822),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { matchesViewModel.sendMessage() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF3366), Color(0xFFE11D48))
                            )
                        )
                        .testTag("button_send_message")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageEntity, isFromMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromMe) 16.dp else 4.dp,
                        bottomEnd = if (isFromMe) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isFromMe) Color(0xFFFF3366) else Color(0xFF161822)
                )
                .border(
                    1.dp,
                    if (isFromMe) Color(0xFFFF3366) else Color(0xFF282A3A),
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromMe) 16.dp else 4.dp,
                        bottomEnd = if (isFromMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
