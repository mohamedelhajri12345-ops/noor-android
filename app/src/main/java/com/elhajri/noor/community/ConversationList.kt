package com.elhajri.noor.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CurrentUser(
    val handle: String,
    val name: String,
    val avatar: String
)

@Composable
fun ConversationList(
    conversations: List<Conversation>,
    currentUser: CurrentUser,
    onSelectConv: (Conversation) -> Unit,
    onNewChat: () -> Unit
) {
    val sorted = conversations.sortedByDescending { conv ->
        conv.lastMessageTime.ifEmpty { conv.createdDate }
    }

    fun getConvName(conv: Conversation): String {
        if (conv.type == "group") return conv.name.ifEmpty { "مجموعة" }
        val otherIdx = conv.memberHandles.indexOfFirst { it != currentUser.handle }
        if (otherIdx >= 0) {
            val n = conv.memberNames.getOrNull(otherIdx)
            if (!n.isNullOrBlank()) return n
            val h = conv.memberHandles.getOrNull(otherIdx)
            if (!h.isNullOrBlank()) return h
        }
        return conv.name.ifEmpty { "محادثة خاصة" }
    }

    fun getConvAvatar(conv: Conversation): String {
        if (conv.type == "group") return ""
        val otherIdx = conv.memberHandles.indexOfFirst { it != currentUser.handle }
        if (otherIdx >= 0) {
            return conv.memberAvatars.getOrNull(otherIdx) ?: ""
        }
        return ""
    }

    fun formatTime(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        return try {
            val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = isoParser.parse(dateStr.take(19)) ?: return ""
            val todayParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            if (todayParser.format(date) == todayParser.format(Date())) {
                SimpleDateFormat("HH:mm", Locale("ar")).format(date)
            } else {
                SimpleDateFormat("d MMM", Locale("ar")).format(date)
            }
        } catch (_: Exception) {
            ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مجتمع القرآن الكريم",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser.avatar.isNotEmpty()) {
                            AsyncImage(
                                model = currentUser.avatar,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = currentUser.name.take(1),
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = currentUser.name,
                            color = TextMain,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${currentUser.handle}",
                            color = Gold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (sorted.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Gold.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد محادثات بعد",
                        color = TextMain.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "ابدأ محادثة جديدة بمعرف صديقك",
                        color = TextMain.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sorted, key = { it.id }) { conv ->
                        val convName = getConvName(conv)
                        val avatarUrl = getConvAvatar(conv)
                        val timeStr = formatTime(conv.lastMessageTime.ifEmpty { conv.createdDate })

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectConv(conv) },
                            shape = RoundedCornerShape(16.dp),
                            color = NavyCard,
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Gold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = avatarUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (conv.type == "group") {
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = null,
                                            tint = Gold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        Text(
                                            text = convName.take(1),
                                            color = Gold,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = convName,
                                        color = TextMain,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val subtitle = buildString {
                                        if (conv.lastSender.isNotEmpty() && conv.lastSender != currentUser.name) {
                                            append("${conv.lastSender}: ")
                                        }
                                        append(conv.lastMessage.ifEmpty { "ابدأ المحادثة" })
                                    }
                                    Text(
                                        text = subtitle,
                                        color = TextMain.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (timeStr.isNotEmpty()) {
                                    Text(
                                        text = timeStr,
                                        color = TextMain.copy(alpha = 0.4f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNewChat,
            containerColor = Gold,
            contentColor = Navy,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(56.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "محادثة جديدة", modifier = Modifier.size(28.dp))
        }
    }
}
