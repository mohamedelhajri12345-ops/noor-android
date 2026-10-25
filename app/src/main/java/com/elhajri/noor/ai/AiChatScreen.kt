package com.elhajri.noor.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.settings.t
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.launch

private data class ChatMsg(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fromUser: Boolean,
    val text: String
)

/**
 * شاشة المساعد الذكي "نور" — متصلة بـ Free LLM API.
 */
@Composable
fun AiChatScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf("") }
    var thinking by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMsg>() }

    val listState = rememberLazyListState()

    // التمرير التلقائي لأسفل القائمة عند وصول رسالة جديدة أو أثناء التفكير
    LaunchedEffect(messages.size, thinking) {
        val totalCount = messages.size + (if (thinking) 1 else 0)
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    fun sendQuestion(queryText: String) {
        val q = queryText.trim()
        if (q.isEmpty() || thinking) return
        input = ""
        messages.add(ChatMsg(fromUser = true, text = q))
        thinking = true

        scope.launch {
            try {
                val answer = FreeLlmApiEngine.ask(q)
                messages.add(ChatMsg(fromUser = false, text = answer))
            } catch (e: Exception) {
                messages.add(
                    ChatMsg(
                        fromUser = false,
                        text = FreeLlmApiEngine.FALLBACK_ERROR_MESSAGE
                    )
                )
            } finally {
                thinking = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .statusBarsPadding()
    ) {
        // ============ الشريط العلوي (Top Bar) ============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = t("رجوع", "Back"),
                    tint = Gold
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    t("المساعد الذكي", "Smart Assistant"),
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
            }

            if (messages.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = t("مسح المحادثة", "Clear Chat"),
                        tint = TextMain.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }

        // ============ جسم الشاشة: إما حالة فارغة أو قائمة المحادثة ============
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !thinking) {
                // الشاشة الابتدائية عند عدم وجود محادثات
                EmptyAssistantView(onSelectSuggestion = { question ->
                    sendQuestion(question)
                })
            } else {
                // قائمة الرسائل
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleItem(msg = msg)
                    }

                    if (thinking) {
                        item(key = "typing_indicator") {
                            TypingIndicatorItem()
                        }
                    }
                }
            }
        }

        // ============ حقل الإدخال وزر الإرسال السفلي ============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(NavyCard)
                .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextMain,
                    fontSize = 14.sp
                ),
                maxLines = 4,
                decorationBox = { innerTextField ->
                    if (input.isEmpty()) {
                        Text(
                            t("اكتب سؤالك هنا…", "Type your question here…"),
                            color = TextMain.copy(alpha = 0.35f),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(Modifier.width(8.dp))

            val canSend = input.trim().isNotEmpty() && !thinking
            IconButton(
                onClick = { sendQuestion(input) },
                enabled = canSend,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (canSend) Gold else NavyLight)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = t("إرسال", "Send"),
                    tint = if (canSend) Navy else TextMain.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // ============ حوار تأكيد مسح المحادثة ============
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = NavyCard,
            title = {
                Text(
                    t("مسح المحادثة", "Clear Chat"),
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    t("هل تريد مسح جميع الرسائل في المحادثة الحالية؟", "Do you want to clear all messages in current chat?"),
                    color = TextMain,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        messages.clear()
                        FreeLlmApiEngine.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text(t("مسح", "Clear"), color = Gold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(t("إلغاء", "Cancel"), color = TextMain.copy(alpha = 0.7f))
                }
            }
        )
    }
}

/**
 * الشاشة الابتدائية عند بداية المحادثة.
 */
@Composable
private fun EmptyAssistantView(onSelectSuggestion: (String) -> Unit) {
    val suggestions = listOf(
        t("ما هي أفضل الأذكار بعد صلاة الفجر؟", "What are the best adhkar after Fajr prayer?"),
        t("كيف أحافظ على الخشوع في الصلاة؟", "How can I maintain focus in prayer?"),
        t("أخبرني عن فضل قيام الليل والأدعية المستجابة", "Tell me about the virtue of Night Prayer"),
        t("ما هي أركان الإسلام الخمسة وشروطها؟", "What are the 5 pillars of Islam?")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Gold.copy(alpha = 0.12f))
                .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            t("مساعد نور الذكي", "NOOR AI Assistant"),
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFamily,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            t(
                "اسأل ما تشاء عن القرآن الكريم، الأذكار، الأحكام الشرعية والأخلاق الإسلامية. يجيبك بأسلوب راقٍ ومستند للقرآن والسنة.",
                "Ask anything about the Quran, Adhkar, Islamic rulings, and ethics. Get answers powered by Free LLM API."
            ),
            color = TextMain.copy(alpha = 0.75f),
            fontSize = 13.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            t("أسئلة مقترحة للبدء:", "Suggested questions:"),
            color = Gold.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(1.dp, Gold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .clickable { onSelectSuggestion(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = suggestion,
                        color = TextMain,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * فقاعة الرسالة (إما للمستخدم أو للمساعد).
 */
@Composable
private fun ChatBubbleItem(msg: ChatMsg) {
    val isUser = msg.fromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.15f))
                    .border(1.dp, Gold.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) Gold.copy(alpha = 0.15f) else NavyCard)
                .border(
                    width = 1.dp,
                    color = if (isUser) Gold.copy(alpha = 0.4f) else Gold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (!isUser) {
                    Text(
                        t("نور", "NOOR"),
                        color = Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    text = msg.text,
                    color = TextMain,
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

/**
 * مؤشر الكتابة التفاعلي (Typing Indicator) الذي يظهر أثناء انتظار رد الذكاء الاصطناعي.
 */
@Composable
private fun TypingIndicatorItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 160, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 320, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.15f))
                .border(1.dp, Gold.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(NavyCard)
                .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    t("نور يفكر الآن", "NOOR is thinking"),
                    color = Gold.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(dot1Alpha)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(dot2Alpha)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(dot3Alpha)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                }
            }
        }
    }
}
