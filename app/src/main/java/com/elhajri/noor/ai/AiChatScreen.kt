package com.elhajri.noor.ai

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.launch

private data class ChatMsg(val fromUser: Boolean, val text: String)

/** فقاعات المحادثة — على هاتف المستخدم، بلا خوادم إطلاقاً */
@Composable
fun AiChatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedModelId by remember { mutableStateOf(LocalAiEngine.getSelectedModel(context).id) }
    val selectedModel = com.elhajri.noor.ai.AiModels.byId(selectedModelId)
    var downloaded by remember { mutableStateOf(LocalAiEngine.isReady(context)) }
    var downloading by remember { mutableStateOf(false) }
    var progressCurrent by remember { mutableStateOf(0L) }
    var progressTotal by remember { mutableStateOf(1L) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var initializing by remember { mutableStateOf(false) }
    var initError by remember { mutableStateOf<String?>(null) }
    var thinking by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMsg>() }

    fun toArabicDigits(v: Any): String {
        val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
        return v.toString().map { map[it] ?: it }.joinToString("")
    }

    // عند تبديل النموذج المختار: أعد فحص جهوزيته
    LaunchedEffect(selectedModelId) {
        LocalAiEngine.setSelectedModel(context, selectedModelId)
        downloaded = LocalAiEngine.isReady(context)
    }

    // تهيئة النموذج عند أول فتح بعد التنزيل
    LaunchedEffect(downloaded) {
        if (downloaded && !initializing && initError == null) {
            initializing = true
            try {
                LocalAiEngine.ensureInitialized(context)
            } catch (e: Exception) {
                initError = e.message ?: "تعذّر تشغيل النموذج على هذا الجهاز"
            }
            initializing = false
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, thinking) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun send() {
        val q = input.trim()
        if (q.isEmpty() || thinking) return
        input = ""
        messages.add(ChatMsg(true, q))
        thinking = true
        initError = null
        scope.launch {
            try {
                LocalAiEngine.ensureInitialized(context)
                val answer = LocalAiEngine.ask(context, q)
                messages.add(ChatMsg(false, answer))
            } catch (e: Exception) {
                messages.add(ChatMsg(false, "حدث خطأ أثناء التوليد: ${e.message ?: "غير معروف"}"))
            }
            thinking = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Navy).statusBarsPadding()) {
        // ============ الشريط العلوي ============
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("رجوع", color = Gold, fontSize = 13.sp)
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("المساعد الذكي", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            if (downloaded) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "حذف النموذج", tint = TextMain.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            } else { Spacer(Modifier.width(48.dp)) }
        }

        when {
            // ============ لم يُنزَّل النموذج: بطاقة تعريف + زر التنزيل ============
            !downloaded && !downloading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(24.dp)).background(Gold.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("ذكاء اصطناعي على هاتفك", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "مساعد إسلامي ذكي يعمل محلياً بالكامل بنموذج مجاني مفتوح من Google LiteRT.\n" +
                            "اسأل ما تشاء عن دينك بلا حدود:\n" +
                            "• بدون إنترنت بعد التحميل\n" +
                            "• بدون حسابات ولا مفاتيح API\n" +
                            "• أسئلتك لا تُغادر هاتفك أبداً",
                        color = TextMain.copy(alpha = 0.75f), fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Spacer(Modifier.height(16.dp))

                    // ============ اختيار النموذج: كامل أو خفيف ============
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.elhajri.noor.ai.AiModels.all.forEach { m ->
                            val isSelected = m.id == selectedModelId
                            val isDownloadedModel = LocalAiEngine.isReady(context, m)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Gold.copy(alpha = 0.12f) else NavyCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) Gold else Gold.copy(alpha = 0.12f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !downloading) { selectedModelId = m.id }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(m.name, color = if (isSelected) Gold else TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Text(m.description, color = TextMain.copy(alpha = 0.55f), fontSize = 10.sp)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    m.sizeLabel() + if (isDownloadedModel) " ✓" else "",
                                    color = GoldSoft, fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        "حجم النموذج المختار: ${selectedModel.sizeLabel()} — يُنزَّل مرة واحدة فقط",
                        color = GoldSoft, fontSize = 12.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    if (LocalAiEngine.freeBytes(context) < selectedModel.sizeBytes + 200_000_000L) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "المساحة المتاحة على جهازك لا تكفي للنموذج (${selectedModel.sizeLabel()}).\nحرّر بعض المساحة ثم عُد.",
                            color = TextMain.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center
                        )
                    } else {
                        val btnInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Gold)
                                .clickable { downloading = true; downloadError = null }
                                .padding(horizontal = 28.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("تنزيل النموذج", color = Navy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ============ جارٍ التنزيل: تقدم مباشر + إلغاء ============
            downloading -> {
                LaunchedEffect(Unit) {
                    try {
                        val ok = LocalAiEngine.download(context, selectedModel) { c, t ->
                            progressCurrent = c; progressTotal = t
                        }
                        if (ok) { downloaded = true; downloading = false }
                        else { downloading = false } // أُلغي
                    } catch (e: Exception) {
                        downloadError = e.message ?: "تعذّر التنزيل"
                        downloading = false
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(16.dp))
                    val pct = if (progressTotal > 0) (progressCurrent * 100 / progressTotal).toInt() else 0
                    Text("${toArabicDigits(pct)}٪", color = Gold, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (progressTotal > 0) progressCurrent.toFloat() / progressTotal else 0f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Gold,
                        trackColor = NavyLight
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${toArabicDigits(progressCurrent / 1048576)} / ${toArabicDigits(progressTotal / 1048576)} ميجابايت",
                        color = TextMain.copy(alpha = 0.6f), fontSize = 12.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("أرسل التطبيق للخلفية بأمان — التنزيل مستمر", color = GoldSoft, fontSize = 11.sp)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { LocalAiEngine.cancelDownload() }) {
                        Text("إلغاء", color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }
            }

            // ============ جاهز: واجهة المحادثة ============
            else -> {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (initializing) {
                            Text("جارٍ تشغيل النموذج على جهازك…", color = GoldSoft, fontSize = 14.sp)
                            Spacer(Modifier.height(14.dp))
                            LinearProgressIndicator(color = Gold, trackColor = NavyLight, modifier = Modifier.width(140.dp))
                            Spacer(Modifier.height(20.dp))
                            Text("أول تشغيل قد يستغرق ثوانٍ", color = TextMain.copy(alpha = 0.5f), fontSize = 11.sp)
                        } else if (initError != null) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(30.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(initError ?: "", color = TextMain.copy(alpha = 0.7f), fontSize = 13.sp, textAlign = TextAlign.Center)
                        } else {
                            val suggestions = listOf(
                                "ما فضل الصدقة؟",
                                "اكتب لي دعاء لتفريج الهم",
                                "ما آداب زيارة المريض؟",
                                "لخص لي قصة يوسف عليه السلام"
                            )
                            Text("اسأل مساعدك عن دينك", color = Gold, fontSize = 16.sp, fontFamily = AmiriFamily)
                            Spacer(Modifier.height(14.dp))
                            suggestions.forEach { sug ->
                                Text(
                                    sug,
                                    color = TextMain.copy(alpha = 0.8f), fontSize = 13.sp,
                                    modifier = Modifier
                                        .padding(vertical = 5.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { input = sug }
                                        .padding(horizontal = 16.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        items(messages) { msg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.fromUser) Arrangement.Start else Arrangement.End
                            ) {
                                Text(
                                    msg.text,
                                    color = if (msg.fromUser) Navy else TextMain,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    modifier = Modifier
                                        .widthIn(max = 300.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (msg.fromUser) Gold else NavyLight.copy(alpha = 0.55f))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                        if (thinking) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    val transition = rememberInfiniteTransition(label = "typing")
                                    val alpha by transition.animateFloat(
                                        initialValue = 0.25f, targetValue = 1f,
                                        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                                        label = "typingAlpha"
                                    )
                                    Text(
                                        "المساعد يفكّر…",
                                        color = GoldSoft.copy(alpha = alpha),
                                        fontSize = 12.sp,
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(NavyLight.copy(alpha = 0.4f)).padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ============ حقل الإدخال ============
                if (!initializing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(NavyLight.copy(alpha = 0.6f))
                                .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (input.isEmpty()) {
                                Text("اكتب سؤالك هنا…", color = TextMain.copy(alpha = 0.35f), fontSize = 13.sp)
                            }
                            BasicTextField(
                                value = input,
                                onValueChange = { input = it },
                                textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { send() },
                            enabled = input.isNotBlank() && !thinking
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (input.isNotBlank() && !thinking) Gold else Gold.copy(alpha = 0.3f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // رسالة خطأ التنزيل — أعلى الشاشة عند العودة من التنزيل الفاشل
        if (downloadError != null && !downloading) {
            Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(downloadError ?: "", color = TextMain, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(
                        "إعادة المحاولة",
                        color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { downloadError = null; downloading = true }
                    )
                }
            }
        }
    }

    // ============ حوار تأكيد حذف النموذج ============
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = NavyLight,
            titleContentColor = Gold,
            title = { Text("حذف النموذج؟", fontFamily = AmiriFamily) },
            text = { Text("سيُحذف النموذج من هاتفك ويمكنك تنزيله مجدداً في أي وقت. المحادثة الحالية ستنتهي.", color = TextMain, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    LocalAiEngine.deleteModel(context)
                    downloaded = false
                    messages.clear()
                    LocalAiEngine.clearConversation()
                    showDeleteDialog = false
                }) { Text("حذف", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء", color = Gold) }
            }
        )
    }
}
