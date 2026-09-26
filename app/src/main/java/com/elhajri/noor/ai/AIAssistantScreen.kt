package com.elhajri.noor.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String,
    val content: String
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
- عربي فصيح واضح، مع روح إيمانية دافئة."""

private val SUGGESTED_QUESTIONS = listOf(
    "⏰ ما هي أوقات الصلاة الخمس؟",
    "🤲 ما هي أذكار الصباح؟",
    "📖 ما فضل سورة الإخلاص؟",
    "❤️ كيف أزيد في محبة النبي ﷺ؟"
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
fun AIAssistantScreen(onBack: () -> Unit) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    role = "assistant",
                    content = "السلام عليكم ورحمة الله 🌙\nأنا مساعدك الذكي في تطبيق \"القرآن الكريم\"، مساعدك في الأمور الإسلامية. كيف يمكنني مساعدتك اليوم؟"
                )
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val sendMessage: (String) -> Unit = { textToSend ->
        val trimmed = textToSend.trim()
        if (trimmed.isNotEmpty() && !isLoading) {
            val userMsg = ChatMessage("user", trimmed)
            val updatedMessages = messages + userMsg
            messages = updatedMessages
            inputText = ""
            isLoading = true
            keyboardController?.hide()

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    // Same InvokeLLM integration the web app uses (verified public endpoint)
                    val conversation = updatedMessages.joinToString("\n") { msg ->
                        if (msg.role == "user") "المستخدم: " + msg.content else "المساعد: " + msg.content
                    }
                    val prompt = SYSTEM_PROMPT + "\n\n" + conversation + "\n\nالمساعد:"

                    val reqBodyJson = JSONObject()
                    reqBodyJson.put("prompt", prompt)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = reqBodyJson.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("https://app.base44.com/api/apps/6a833faeb9e42cca9a6576fa/integration-endpoints/Core/InvokeLLM")
                        .post(body)
                        .build()

                    val response = okHttpClient.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                        val replyText: String = try {
                            when (val parsed = org.json.JSONTokener(responseBody).nextValue()) {
                                is String -> parsed
                                is JSONObject -> parsed.optString("reply", parsed.optString("content", parsed.optString("text", "")))
                                else -> ""
                            }
                        } catch (e: Exception) {
                            try {
                                val respObj = JSONObject(responseBody)
                                when {
                                    respObj.has("choices") -> respObj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content", "")
                                    respObj.has("content") -> respObj.optString("content", "")
                                    respObj.has("text") -> respObj.optString("text", "")
                                    else -> ""
                                }
                            } catch (e2: Exception) { "" }
                        }

                        val finalText = if (replyText.isNotBlank()) replyText else "عذرًا، لم أتمكن من الرد الآن."
                        withContext(Dispatchers.Main) {
                            messages = messages + ChatMessage("assistant", finalText)
                            isLoading = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            messages = messages + ChatMessage("assistant", "عذرًا، حدث خطأ. تأكد من اتصالك بالإنترنت وحاول مرة أخرى.")
                            isLoading = false
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        messages = messages + ChatMessage("assistant", "عذرًا، حدث خطأ. تأكد من اتصالك بالإنترنت وحاول مرة أخرى.")
                        isLoading = false
                    }
                }
            }
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                                fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        containerColor = Navy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
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

                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .then(
                                    if (!isUser) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = Gold.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    } else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) Gold else NavyCard
                            ),
                            shape = if (isUser) {
                                RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
                            } else {
                                RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
                            }
                        ) {
                            Text(
                                text = msg.content,
                                color = if (isUser) Navy else TextMain,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        if (isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Navy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
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
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyCard),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "أسئلة مقترحة:",
                        color = GoldSoft,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    SUGGESTED_QUESTIONS.chunked(2).forEach { rowQuestions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowQuestions.forEach { q ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { sendMessage(q) },
                                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = q,
                                        color = TextMain,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            if (rowQuestions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("اكتب سؤالك هنا...", color = GoldSoft.copy(alpha = 0.5f), fontSize = 14.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) })
                    )

                    IconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isLoading) Gold else Gold.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = Navy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
