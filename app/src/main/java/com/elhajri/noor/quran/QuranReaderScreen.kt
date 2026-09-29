package com.elhajri.noor.quran

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.data.Reciter
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

data class AyahItem(val number: Int, val text: String)

private suspend fun fetchOrLoadSurahAyahs(context: Context, surahNumber: Int): List<AyahItem> = withContext(Dispatchers.IO) {
    // ===== المصحف الكامل مدمج داخل التطبيق — يعمل بدون إنترنت من أول فتحة =====
    try {
        val full = JSONObject(context.assets.open("data/quran_full.json").bufferedReader().use { it.readText() })
        val surahs = full.getJSONArray("surahs")
        for (i in 0 until surahs.length()) {
            val s = surahs.getJSONObject(i)
            if (s.getInt("number") == surahNumber) {
                val ayahs = s.getJSONArray("ayahs")
                val list = List(ayahs.length()) { j ->
                    val a = ayahs.getJSONObject(j)
                    AyahItem(a.getInt("n"), a.getString("t"))
                }
                if (list.isNotEmpty()) return@withContext list
            }
        }
    } catch (_: Exception) {}
    val file = File(context.filesDir, "quran_$surahNumber.json")
    if (file.exists() && file.length() > 0) {
        try {
            val jsonStr = file.readText()
            val jsonArr = JSONArray(jsonStr)
            val list = List(jsonArr.length()) { i ->
                val obj = jsonArr.getJSONObject(i)
                AyahItem(obj.getInt("number"), obj.getString("text"))
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (e: Exception) {
            // fallback to network fetch
        }
    }

    try {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.alquran.cloud/v1/surah/$surahNumber/quran-uthmani")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val jsonObj = JSONObject(bodyStr)
                val dataObj = jsonObj.getJSONObject("data")
                val ayahsArr = dataObj.getJSONArray("ayahs")

                val result = mutableListOf<AyahItem>()
                val cacheArr = JSONArray()

                for (i in 0 until ayahsArr.length()) {
                    val aObj = ayahsArr.getJSONObject(i)
                    val num = aObj.optInt("numberInSurah", i + 1)
                    val txt = aObj.getString("text")
                    result.add(AyahItem(num, txt))

                    val itemObj = JSONObject()
                    itemObj.put("number", num)
                    itemObj.put("text", txt)
                    cacheArr.put(itemObj)
                }

                file.writeText(cacheArr.toString())
                return@withContext result
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    emptyList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    surahName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allSurahs = remember { DataLoader.surahs(context) }
    val allReciters = remember { DataLoader.reciters(context) }

    var currentSurahNum by remember { mutableIntStateOf(surahNumber) }
    val currentSurah = remember(currentSurahNum) {
        allSurahs.find { it.number == currentSurahNum }
    }
    val currentName = currentSurah?.name ?: surahName

    var ayahs by remember { mutableStateOf<List<AyahItem>>(emptyList()) }
    var isLoadingText by remember { mutableStateOf(true) }
    var isLightMode by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }

    val favorites by FavoritesStore.favorites.collectAsState()
    val isFav = favorites.contains(currentSurahNum)

    // Reciter setup
    val savedReciterId = remember { Prefs.getReciter(context) }
    var currentReciter by remember {
        mutableStateOf(
            allReciters.find { it.id == savedReciterId } ?: allReciters.firstOrNull() ?: Reciter("alafasy", "مشاري العفاسي", listOf("https://server11.mp3quran.net/afs/"))
        )
    }

    // Audio player — the separate native mushaf player (web audioManager port)
    val playerState by com.elhajri.noor.audio.player.QuranPlayerManager.state.collectAsState()
    val isPlaying = playerState.isPlaying
    val isLoadingAudio = playerState.isLoading && playerState.currentId == "quran-$currentSurahNum"
    val progress = if (playerState.duration > 0f) (playerState.currentTime / playerState.duration).coerceIn(0f, 1f) else 0f

    LaunchedEffect(Unit) { com.elhajri.noor.audio.player.QuranPlayerManager.ensure(context) }

    // Load ayahs text and bookmark
    LaunchedEffect(currentSurahNum) {
        isLoadingText = true
        ayahs = fetchOrLoadSurahAyahs(context, currentSurahNum)
        isLoadingText = false

        // Bookmark last read + تتبّع الختمة والسلسلة اليومية (خصائص مقترحة سابقاً)
        val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val readSet = (sp.getStringSet("khatma_read_surahs", emptySet()) ?: emptySet()).toMutableSet()
        readSet.add(currentSurahNum.toString())
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val lastDate = sp.getString("streak_last_date", null)
        var streak = sp.getInt("streak_count", 0)
        if (lastDate != today) {
            val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date(System.currentTimeMillis() - 86_400_000L))
            streak = if (lastDate == yesterday) streak + 1 else 1
        }
        sp.edit()
            .putInt("last_read_surah", currentSurahNum)
            .putString("last_read_surah_name", currentName)
            .putStringSet("khatma_read_surahs", readSet)
            .putString("streak_last_date", today)
            .putInt("streak_count", streak)
            .apply()
    }

    // Auto-advance handled natively by the playback queue (current + next 4 surahs)

    val playAudio = {
        // queue like the web QuranReader.jsx: current surah + next 4
        val tracks = (currentSurahNum..minOf(114, currentSurahNum + 4)).mapNotNull { n ->
            val servers = currentReciter.servers
            val name = allSurahs.find { it.number == n }?.name ?: currentName
            if (servers.isEmpty()) null
            else com.elhajri.noor.audio.player.PlayerTrack(
                id = "quran-$n",
                url = "${servers[0]}${String.format("%03d", n)}.mp3",
                title = "سورة $name",
                artist = currentReciter.name,
                fallbackUrls = servers.drop(1).map { "${it}${String.format("%03d", n)}.mp3" }
            )
        }
        if (playerState.currentId == "quran-$currentSurahNum") {
            com.elhajri.noor.audio.player.QuranPlayerManager.toggle()
        } else {
            com.elhajri.noor.audio.player.QuranPlayerManager.playQueue(tracks, 0)
        }
    }

    // Custom AnnotatedString for ayahs with gold medallions
    val annotatedAyahsText = remember(ayahs) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                append(ayah.text)
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                ) {
                    append(" ﴿${toArabicNumber(ayah.number)}﴾ ")
                }
            }
        }
    }

    // ===== استنساخ: نسخ الآيات والمصحف =====
    val clipboard = LocalClipboardManager.current
    var showCopySheet by remember { mutableStateOf(false) }
    var copyFrom by remember(currentSurahNum) { mutableIntStateOf(1) }
    var copyTo by remember(currentSurahNum) { mutableIntStateOf(currentSurah?.ayahs ?: 1) }

    fun buildMushafText(from: Int, to: Int): String {
        val selected = ayahs.filter { it.number in from..to }
        val body = selected.joinToString(" ") { a ->
            "${a.text} ﴿${toArabicNumber(a.number)}﴾"
        }
        val ref = "﴾ سورة ${currentName}"
        return "$body\n$ref"
    }

    fun copyToClipboard(from: Int, to: Int) {
        val text = buildMushafText(from, to)
        clipboard.setText(androidx.compose.ui.text.AnnotatedString(text))
        Toast.makeText(context, "تم نسخ المصحف بنجاح", Toast.LENGTH_SHORT).show()
        showCopySheet = false
    }

    fun shareSurah(from: Int, to: Int) {
        val text = buildMushafText(from, to)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "مشاركة المصحف"))
        showCopySheet = false
    }

    if (showCopySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCopySheet = false },
            containerColor = NavyCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    "استنساخ المصحف",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "انسخ آيات سورة $currentName أو شاركها",
                    color = GoldSoft,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // نسخ السورة كاملة
                Button(
                    onClick = { copyToClipboard(1, currentSurah?.ayahs ?: ayahs.size) },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("نسخ السورة كاملة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(Modifier.height(10.dp))

                // مشاركة السورة كاملة
                OutlinedButton(
                    onClick = { shareSurah(1, currentSurah?.ayahs ?: ayahs.size) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("مشاركة السورة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(Modifier.height(20.dp))

                // نسخ نطاق آيات محدد
                Text("نسخ نطاق محدد من الآيات", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { if (copyFrom > 1) copyFrom-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) { Text("−", fontSize = 18.sp) }
                    Text(
                        "من الآية ${toArabicNumber(copyFrom)}",
                        color = Gold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { if (copyFrom < (currentSurah?.ayahs ?: copyFrom + 1) - 1) copyFrom++ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) { Text("+", fontSize = 18.sp) }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { if (copyTo > copyFrom) copyTo-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) { Text("−", fontSize = 18.sp) }
                    Text(
                        "إلى الآية ${toArabicNumber(copyTo)}",
                        color = Gold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { if (copyTo < (currentSurah?.ayahs ?: copyTo)) copyTo++ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) { Text("+", fontSize = 18.sp) }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { if (copyFrom <= copyTo) copyToClipboard(copyFrom, copyTo) },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("نسخ الآيات المحددة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "سورة $currentName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "رجوع", tint = Gold)
                    }
                },
                actions = {
                    // استنساخ: نسخ المصحف
                    IconButton(onClick = { showCopySheet = true }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "استنساخ المصحف",
                            tint = Gold
                        )
                    }
                    // Reading Mode Toggle
                    IconButton(onClick = { isLightMode = !isLightMode }) {
                        Icon(
                            imageVector = if (isLightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                            contentDescription = "تغيير الوضع",
                            tint = Gold
                        )
                    }
                    // Favorite Toggle
                    IconButton(onClick = { FavoritesStore.toggleFavorite(context, currentSurahNum) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "مفضلة",
                            tint = if (isFav) Gold else GoldSoft
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyCard
                )
            )
        },
        bottomBar = {
            // Audio Player Bar
            Surface(
                color = NavyCard,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Progress bar / Seek bar
                    Slider(
                        value = progress,
                        onValueChange = { com.elhajri.noor.audio.player.QuranPlayerManager.seek(it * playerState.duration) },
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = NavyLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Reciter selector button
                        TextButton(
                            onClick = { showReciterDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = GoldSoft)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentReciter.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Play/Pause Button
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    com.elhajri.noor.audio.player.QuranPlayerManager.toggle()
                                } else {
                                    if (playerState.currentId != null) {
                                        com.elhajri.noor.audio.player.QuranPlayerManager.toggle()
                                    } else {
                                        playAudio()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Gold)
                        ) {
                            if (isLoadingAudio) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Navy,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل",
                                    tint = Navy,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Surah Info badge
                        currentSurah?.let {
                            Text(
                                text = "${it.type} · ${toArabicNumber(it.ayahs)} آية",
                                fontSize = 12.sp,
                                color = GoldSoft.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        containerColor = if (isLightMode) Color(0xFFFFFBEB) else Navy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoadingText) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Gold
                )
            } else if (ayahs.isEmpty()) {
                Text(
                    text = "تعذر تحميل نص السورة. يرجى الاتصال بالإنترنت.",
                    color = if (isLightMode) Color.DarkGray else TextMain,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLightMode) Color(0xFFFEF3C7) else NavyCard
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Text(
                            text = annotatedAyahsText,
                            style = TextStyle(
                                // حجم خط المصحف قابل للتعديل من الإعدادات (صغير/متوسط/كبير)
                                fontSize = when (com.elhajri.noor.data.Prefs.getReaderFontSize(context)) {
                                    "S" -> 18.sp; "L" -> 26.sp; else -> 22.sp
                                },
                                lineHeight = 46.sp,
                                fontFamily = AmiriFamily,
                                color = if (isLightMode) Color(0xFF1C1917) else TextMain,
                                textAlign = TextAlign.Justify
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Reciter Selection Dialog
    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = { showReciterDialog = false },
            title = { Text("اختر القارئ", color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allReciters.forEach { reciter ->
                        val isSelected = reciter.id == currentReciter.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentReciter = reciter
                                    Prefs.setReciter(context, reciter.id)
                                    // like the web changeReciter: restart the queue with the new reciter
                                    if (com.elhajri.noor.audio.player.QuranPlayerManager.state.value.currentId != null) {
                                        val tracks = (currentSurahNum..minOf(114, currentSurahNum + 4)).mapNotNull { n ->
                                            val servers = reciter.servers
                                            val name = allSurahs.find { it.number == n }?.name ?: currentName
                                            if (servers.isEmpty()) null
                                            else com.elhajri.noor.audio.player.PlayerTrack(
                                                id = "quran-$n",
                                                url = "${servers[0]}${String.format("%03d", n)}.mp3",
                                                title = "سورة $name",
                                                artist = reciter.name,
                                                fallbackUrls = servers.drop(1).map { "${it}${String.format("%03d", n)}.mp3" }
                                            )
                                        }
                                        com.elhajri.noor.audio.player.QuranPlayerManager.playQueue(tracks, 0)
                                    }
                                    showReciterDialog = false
                                    if (isPlaying) {
                                        playAudio()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Gold else NavyLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = reciter.name,
                                    color = if (isSelected) Navy else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReciterDialog = false }) {
                    Text("إلغاء", color = GoldSoft)
                }
            },
            containerColor = NavyCard
        )
    }
}
