package com.elhajri.noor.community

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatView(
    conversation: Conversation,
    currentUser: CurrentUser,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<CommunityMessage>>(emptyList()) }
    var textInput by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    // Full screen image view
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    // Delete confirm
    var messageToDelete by remember { mutableStateOf<CommunityMessage?>(null) }

    // Voice playback
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingMsgId by remember { mutableStateOf<String?>(null) }

    // Voice recording
    var isRecording by remember { mutableStateOf(false) }
    var recDuration by remember { mutableStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var voiceFile by remember { mutableStateOf<File?>(null) }

    fun playVoice(url: String, msgId: String) {
        if (playingMsgId == msgId) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playingMsgId = null
            return
        }
        mediaPlayer?.release()
        val mp = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { start() }
            setOnCompletionListener {
                playingMsgId = null
                release()
                mediaPlayer = null
            }
            prepareAsync()
        }
        mediaPlayer = mp
        playingMsgId = msgId
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception) {}
                release()
            }
        }
    }

    fun loadMessages() {
        coroutineScope.launch {
            val res = CommunityApi.getMessages(conversation.id)
            res.onSuccess { list ->
                messages = list
            }
            loading = false
        }
    }

    LaunchedEffect(conversation.id) {
        loadMessages()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val uploadRes = CommunityApi.uploadFile(bytes, "img_${System.currentTimeMillis()}.jpg", "image/jpeg")
                    uploadRes.onSuccess { url ->
                        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
                        val tempMsg = CommunityMessage(
                            id = "temp_${System.currentTimeMillis()}",
                            conversationId = conversation.id,
                            nickname = currentUser.name,
                            text = "📷 صورة",
                            avatarUrl = currentUser.avatar,
                            fileUrl = url,
                            fileType = "image",
                            createdDate = nowIso,
                            pending = true
                        )
                        messages = messages + tempMsg

                        val createRes = CommunityApi.createMessage(
                            conversationId = conversation.id,
                            nickname = currentUser.name,
                            text = "📷 صورة",
                            avatarUrl = currentUser.avatar,
                            fileUrl = url,
                            fileType = "image"
                        )
                        createRes.onSuccess { created ->
                            messages = messages.map { if (it.id == tempMsg.id) created else it }
                            CommunityApi.updateConversation(conversation.id, "📷 صورة", currentUser.name, nowIso)
                        }.onFailure {
                            messages = messages.map { if (it.id == tempMsg.id) it.copy(pending = false, failed = true) else it }
                        }
                    }
                }
            }
        }
    }

    // Voice recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recDuration = 0
            while (isRecording && recDuration < 60) {
                delay(1000)
                recDuration += 1
            }
            if (recDuration >= 60 && isRecording) {
                // Stop automatically at 60s
                mediaRecorder?.apply {
                    try { stop() } catch (_: Exception) {}
                    release()
                }
                mediaRecorder = null
                isRecording = false
                voiceFile?.let { file ->
                    if (file.exists() && file.length() > 0) {
                        val bytes = file.readBytes()
                        val uploadRes = CommunityApi.uploadFile(bytes, file.name, "audio/m4a")
                        uploadRes.onSuccess { url ->
                            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
                            CommunityApi.createMessage(
                                conversationId = conversation.id,
                                nickname = currentUser.name,
                                text = "🎙️ رسالة صوتية",
                                avatarUrl = currentUser.avatar,
                                fileUrl = url,
                                fileType = "voice"
                            ).onSuccess {
                                CommunityApi.updateConversation(conversation.id, "🎙️ رسالة صوتية", currentUser.name, nowIso)
                                loadMessages()
                            }
                        }
                    }
                }
            }
        }
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
                voiceFile = file
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setOutputFile(file.absolutePath)
                recorder.prepare()
                recorder.start()
                mediaRecorder = recorder
                isRecording = true
            } catch (_: Exception) {
                isRecording = false
            }
        }
    }

    fun toggleVoiceRecord() {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
            } catch (_: Exception) {}
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false

            voiceFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    coroutineScope.launch {
                        val bytes = file.readBytes()
                        val uploadRes = CommunityApi.uploadFile(bytes, file.name, "audio/m4a")
                        uploadRes.onSuccess { url ->
                            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
                            val createRes = CommunityApi.createMessage(
                                conversationId = conversation.id,
                                nickname = currentUser.name,
                                text = "🎙️ رسالة صوتية",
                                avatarUrl = currentUser.avatar,
                                fileUrl = url,
                                fileType = "voice"
                            )
                            createRes.onSuccess {
                                CommunityApi.updateConversation(conversation.id, "🎙️ رسالة صوتية", currentUser.name, nowIso)
                                loadMessages()
                            }
                        }
                    }
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                try {
                    val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
                    voiceFile = file
                    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(context)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaRecorder()
                    }
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    recorder.setOutputFile(file.absolutePath)
                    recorder.prepare()
                    recorder.start()
                    mediaRecorder = recorder
                    isRecording = true
                } catch (_: Exception) {
                    isRecording = false
                }
            } else {
                recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    fun sendMessage() {
        val trimmed = textInput.trim()
        if (trimmed.isEmpty()) return

        textInput = ""
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
        val tempMsg = CommunityMessage(
            id = "temp_${System.currentTimeMillis()}",
            conversationId = conversation.id,
            nickname = currentUser.name,
            text = trimmed,
            avatarUrl = currentUser.avatar,
            createdDate = nowIso,
            pending = true
        )
        messages = messages + tempMsg

        coroutineScope.launch {
            val res = CommunityApi.createMessage(
                conversationId = conversation.id,
                nickname = currentUser.name,
                text = trimmed,
                avatarUrl = currentUser.avatar
            )
            res.onSuccess { created ->
                messages = messages.map { if (it.id == tempMsg.id) created else it }
                CommunityApi.updateConversation(conversation.id, trimmed, currentUser.name, nowIso)
            }.onFailure {
                messages = messages.map { if (it.id == tempMsg.id) it.copy(pending = false, failed = true) else it }
            }
        }
    }

    val convName = if (conversation.type == "group") conversation.name.ifEmpty { "مجموعة" }
    else {
        val otherIdx = conversation.memberHandles.indexOfFirst { it != currentUser.handle }
        if (otherIdx >= 0) conversation.memberNames.getOrNull(otherIdx) ?: conversation.name
        else conversation.name
    }

    Scaffold(
        containerColor = Color(0xFF070B14),
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
                            Text(convName.take(1), color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Column {
                            Text(
                                text = convName,
                                color = Gold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AmiriFamily
                            )
                            Text(
                                text = if (conversation.type == "group") "${conversation.memberHandles.size} أعضاء" else "محادثة خاصة",
                                color = TextMain.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B14))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (loading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("لا توجد رسائل بعد\nابدأ المحادثة الآن", color = TextMain.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMine = msg.nickname == currentUser.name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            if (!isMine) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Gold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (msg.avatarUrl.isNotEmpty()) {
                                        AsyncImage(model = msg.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(msg.nickname.take(1), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Surface(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            if (isMine && !msg.pending) {
                                                messageToDelete = msg
                                            }
                                        }
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isMine) Gold else NavyCard,
                                border = if (isMine) null else BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (!isMine && msg.nickname.isNotEmpty()) {
                                        Text(
                                            text = msg.nickname,
                                            color = Gold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }

                                    if (msg.fileUrl.isNotEmpty() && msg.fileType == "image") {
                                        AsyncImage(
                                            model = msg.fileUrl,
                                            contentDescription = "صورة",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 200.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { fullScreenImageUrl = msg.fileUrl },
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (msg.fileUrl.isNotEmpty() && (msg.fileType == "voice" || msg.fileType == "audio")) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { playVoice(msg.fileUrl, msg.id) },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isMine) Navy else Gold)
                                            ) {
                                                Icon(
                                                    imageVector = if (playingMsgId == msg.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = if (isMine) Gold else Navy,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Text(
                                                text = if (playingMsgId == msg.id) "جاري التشغيل..." else "رسالة صوتية 🎙️",
                                                color = if (isMine) Navy else TextMain,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    if (msg.text.isNotEmpty() && msg.text != "📷 صورة" && msg.text != "🎙️ رسالة صوتية") {
                                        Text(
                                            text = msg.text,
                                            color = if (isMine) Navy else TextMain,
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (msg.pending) {
                                            Text("⏳", fontSize = 9.sp)
                                        } else if (msg.failed) {
                                            Text("⚠", color = Color.Red, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = NavyCard,
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "إرفاق صورة", tint = Gold, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { toggleVoiceRecord() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "تسجيل صوتي",
                            tint = if (isRecording) Color.Red else Gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isRecording) {
                        Text(
                            text = "جاري التسجيل (${recDuration}s)...",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("اكتب رسالتك...", color = TextMain.copy(alpha = 0.4f), fontSize = 12.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    IconButton(
                        onClick = { sendMessage() },
                        enabled = textInput.isNotBlank() && !isRecording,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (textInput.isNotBlank() && !isRecording) Gold else Gold.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Navy, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Full screen image dialog
    if (fullScreenImageUrl != null) {
        AlertDialog(
            onDismissRequest = { fullScreenImageUrl = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { fullScreenImageUrl = null }) {
                    Text("إغلاق", color = Gold)
                }
            },
            text = {
                AsyncImage(
                    model = fullScreenImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    contentScale = ContentScale.Fit
                )
            },
            containerColor = Color(0xFF070B14)Card
        )
    }

    // Delete message confirm dialog
    if (messageToDelete != null) {
        val target = messageToDelete!!
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("حذف الرسالة", color = Gold, fontSize = 16.sp) },
            text = { Text("هل تريد حذف هذه الرسالة؟", color = TextMain, fontSize = 13.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            CommunityApi.deleteMessage(target.id)
                            messages = messages.filter { it.id != target.id }
                        }
                        messageToDelete = null
                    }
                ) {
                    Text("حذف", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("إلغاء", color = TextMain)
                }
            },
            containerColor = Color(0xFF070B14)Card
        )
    }
}
