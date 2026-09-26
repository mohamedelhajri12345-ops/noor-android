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
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.data.Reciter
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

data class AyahItem(val number: Int, val text: String)

private suspend fun fetchOrLoadSurahAyahs(context: Context, surahNumber: Int): List<AyahItem> = withContext(Dispatchers.IO) {
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

    // Audio player
    val player = remember { SurahAudioPlayer() }
    val isPlaying by player.isPlaying.collectAsState()
    val isLoadingAudio by player.isLoading.collectAsState()
    val progress by player.progress.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    // Load ayahs text and bookmark
    LaunchedEffect(currentSurahNum) {
        isLoadingText = true
        ayahs = fetchOrLoadSurahAyahs(context, currentSurahNum)
        isLoadingText = false

        // Bookmark last read
        val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        sp.edit()
            .putInt("last_read_surah", currentSurahNum)
            .putString("last_read_surah_name", currentName)
            .apply()
    }

    // Auto-advance logic
    LaunchedEffect(currentSurahNum, currentReciter) {
        player.onAutoAdvance = {
            if (currentSurahNum < 114) {
                currentSurahNum += 1
            }
        }
    }

    val playAudio = {
        val padded = String.format("%03d", currentSurahNum)
        val servers = currentReciter.servers
        if (servers.isNotEmpty()) {
            val primary = "${servers[0]}$padded.mp3"
            val fallbacks = servers.drop(1).map { "${it}$padded.mp3" }
            player.prepare(primary, fallbacks)
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
                        onValueChange = { player.seekTo(it) },
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
                                    player.pause()
                                } else {
                                    if (player.progress.value > 0f) {
                                        player.play()
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
                                fontSize = 22.sp,
                                lineHeight = 42.sp,
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
                            Text(
                                text = reciter.name,
                                color = if (isSelected) Navy else Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp)
                            )
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
