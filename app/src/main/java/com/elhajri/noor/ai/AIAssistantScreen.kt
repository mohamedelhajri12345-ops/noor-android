package com.elhajri.noor.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String,
    val text: String
)

data class SuggestionChipItem(
    val icon: ImageVector,
    val text: String
)

private const val SYSTEM_PROMPT = """أنت "المساعد الذكي" في تطبيق "القرآن الكريم"، مساعد ذكاء اصطناعي إسلامي متخصص، تجيب على الأسئلة الإسلامية بناءً على القرآن الكريم والسنة النبوية المطهرة بفهم السلف الصالح.

## المبادئ الأساسية
- التزامك التام بالكتاب والسنة، ولا تفتي برأيك الشخصي.
- اعتمد على المصادر الموثوقة: القرآن الكريم، الكتب الستة (البخاري، مسلم، أبو داود، الترمذي، النسائي، ابن ماجه)، ومسند أحمد.
- عند ذكر الحديث: اذكر الراوي والدرجة (صحيح، حسن، ضعيف) إن أمكن.

## قواعد الإجابة
1- الدقة قبل كل شيء: لا تخمن. إذا لم تكن متأكدًا، قل صراحةً "لا أملك معلومة مؤكدة عن هذا" — هذا خير من الفتيا بلا علم.
2- الإيجاز: اجابة مركزة وواضحة (٥٠–١٥٠ كلمة عادةً)، إلا إذا طلب المستخدم التفصيل.
3- التأصيل الشرعي: اذكر الدليل (السورة والآية، أو الراوي ودرجة الحديث) عند كل حكم.
4- لا تُفتي في المسائل الخلافية بين المذاهب بقول واحد دون الإشارة لوجود خلاف، واذكر قول الجمهور ثم غيره بإيجاز.
5- ركّز على ما ينبني عليه العمل: العبادات، الأخلاق، المعاملات، العقيدة.
6- تعامل بلطف واحترام مع كل مستخدم، واستخدم خطاب الأخوة الإيمانية.

## حدود التخصص
- تخصصك المسائل الإسلامية. إذا كان السؤال غير ديني إطلاقًا، وجّه المستخدم بلطف: "تخصصي المسائل الإسلامية، لكن يسعدني مساعدتك في أي أمر ديني."
- لا تُجب على الأسئلة الطبية أو القانونية التي تتطلب اختصاصاً مهنياً — وجّه المستخدم لأهل الاختصاص.
- لا تتدخل في السياسة ولا في النزاعات الشخصية.

## الأسلوب
- عربي فصيح واضح، مع روح إيمانية دافئة.
- ابدأ التحية مرة واحدة فقط، ثم ادخل في صلب الإجابة.
- استخدم الترقيم والنقاط لتنظيم الإجابة الطويلة."""

private val SUGGESTIONS = listOf(
    SuggestionChipItem(Icons.Default.AccessTime, "ما هي أوقات الصلاة الخمس؟"),
    SuggestionChipItem(Icons.Default.FrontHand, "ما هي أذكار الصباح؟"),
    SuggestionChipItem(Icons.Default.Book, "ما فضل سورة الإخلاص؟"),
    SuggestionChipItem(Icons.Default.Favorite, "كيف أزيد في محبة النبي ﷺ؟")
)

private val okHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(onBack: () -> Unit = {}) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    role = "assistant",
                    text = "السلام عليكم ورحمة الله 🌙\nأنا مساعدك الذكي في تطبيق \"القرآن الكريم\"، مساعدك في الأمور الإسلامية. كيف يمكنني مساعدتك اليوم؟"
                )
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val sendText: (String) -> Unit = { textToSend ->
        val trimmed = textToSend.trim()
        if (trimmed.isNotEmpty() && !loading) {
            val userMsg = ChatMessage("user", trimmed)
            val updatedMessages = messages + userMsg
            messages = updatedMessages
            input = ""
            loading = true
            keyboardController?.hide()

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val conversation = updatedMessages.joinToString("\n") { m ->
                        if (m.role == "user") "المستخدم: ${m.text}" else "المساعد: ${m.text}"
                    }
                    val fullPrompt = "$SYSTEM_PROMPT\n\n$conversation\n\nالمساعد:"

                    val reqBodyJson = JSONObject()
                    reqBodyJson.put("prompt", fullPrompt)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = reqBodyJson.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("https://app.base44.com/api/apps/6a833faeb9e42cca9a6576fa/integration-endpoints/Core/InvokeLLM")
                        .post(body)
                        .build()

                    val response = okHttpClient.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful && responseBody.isNotBlank()) {
                        var parsedText = ""
                        try {
                            val token = JSONTokener(responseBody).nextValue()
                            if (token is String) {
                                parsedText = token
                            } else if (token is JSONObject) {
                                parsedText = when {
                                    token.has("text") -> token.optString("text", "")
                                    token.has("response") -> token.optString("response", "")
                                    token.has("content") -> token.optString("content", "")
                                    token.has("message") -> token.optString("message", "")
                                    token.has("reply") -> token.optString("reply", "")
                                    else -> token.toString()
                                }
                            }
                        } catch (e: Exception) {
                            parsedText = responseBody
                        }

                        val reply = if (parsedText.isNotBlank()) parsedText else "عذرًا، لم أتمكن من الرد الآن."
                        withContext(Dispatchers.Main) {
                            messages = messages + ChatMessage("assistant", reply)
                            isOffline = false
                            loading = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            messages = messages + ChatMessage("assistant", "عذرًا، حدث خطأ. تأكد من اتصالك بالإنترنت وحاول مرة أخرى.")
                            loading = false
                        }
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        isOffline = true
                        val offlineMsg = "🌙 عذرًا، المساعد الذكي يحتاج إلى اتصال بالإنترنت. باقي خصائص التطبيق (القرآن، الأذكار، القبلة...) تعمل بدون إنترنت."
                        messages = messages + ChatMessage("assistant", offlineMsg)
                        loading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        messages = messages + ChatMessage("assistant", "عذرًا، حدث خطأ. تأكد من اتصالك بالإنترنت وحاول مرة أخرى.")
                        loading = false
                    }
                }
            }
        }
    }

    LaunchedEffect(messages.size, loading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A0F1A),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "مساعد القرآن الكريم",
                                color = Gold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AmiriFamily
                            )
                            Text(
                                text = "اسألني عن أي أمر ديني",
                                color = GoldSoft.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0F1A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isOffline) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = NavyCard.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "🌙", fontSize = 18.sp)
                        Text(
                            text = "أنت بدون اتصال — المساعد الذكي يحتاج إنترنت، باقي الخصائص تعمل أوفلاين",
                            color = TextMain.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Surface(
                            modifier = Modifier.widthIn(max = 280.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isUser) Gold else NavyCard,
                            border = if (isUser) null else BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                color = if (isUser) Navy else TextMain,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }

                        if (isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Gold, GoldSoft)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Navy,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (loading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = NavyCard,
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Gold,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "المساعد يفكر...",
                                        color = GoldSoft,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (messages.size <= 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SUGGESTIONS.chunked(2).forEach { rowChips ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowChips.forEach { chip ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { sendText(chip.text) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = NavyCard,
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = chip.icon,
                                            contentDescription = null,
                                            tint = Gold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = chip.text,
                                            color = TextMain,
                                            fontSize = 11.sp,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = NavyCard,
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = {
                            Text(
                                text = "اكتب سؤالك هنا...",
                                color = TextMain.copy(alpha = 0.4f),
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendText(input) }),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(
                        onClick = { sendText(input) },
                        enabled = input.isNotBlank() && !loading,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (input.isNotBlank() && !loading)
                                    Brush.horizontalGradient(listOf(Gold, GoldSoft))
                                else
                                    Brush.horizontalGradient(listOf(Gold.copy(alpha = 0.3f), GoldSoft.copy(alpha = 0.3f)))
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = Navy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
