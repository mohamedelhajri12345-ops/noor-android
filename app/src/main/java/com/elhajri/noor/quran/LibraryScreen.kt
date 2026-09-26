package com.elhajri.noor.quran

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import org.json.JSONArray
import org.json.JSONObject

private const val FAV_PREFS = "nur_nasheed_favs_prefs"
private const val KEY_FAVS = "fav_ids"

data class NasheedCategory(val id: String, val name: String)
data class Nasheed(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val category: String,
    val url: String
)

private fun loadNasheedsData(context: Context): Pair<List<NasheedCategory>, List<Nasheed>> {
    return try {
        val jsonStr = context.assets.open("data/nasheeds.json").bufferedReader().use { it.readText() }
        val root = JSONObject(jsonStr)

        val catsArr = root.getJSONArray("nasheedCategories")
        val categories = List(catsArr.length()) { i ->
            val o = catsArr.getJSONObject(i)
            NasheedCategory(o.getString("id"), o.getString("name"))
        }

        val nasheedsArr = root.getJSONArray("nasheeds")
        val nasheeds = List(nasheedsArr.length()) { i ->
            val o = nasheedsArr.getJSONObject(i)
            Nasheed(
                o.getString("id"),
                o.getString("title"),
                o.getString("artist"),
                o.optString("duration", ""),
                o.getString("category"),
                o.getString("url")
            )
        }

        Pair(categories, nasheeds)
    } catch (e: Exception) {
        Pair(emptyList(), emptyList())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val (categories, nasheeds) = remember { loadNasheedsData(context) }

    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    // Favorites
    val sp = remember { context.getSharedPreferences(FAV_PREFS, Context.MODE_PRIVATE) }
    var favIdsStr by remember { mutableStateOf(sp.getString(KEY_FAVS, "[]") ?: "[]") }
    val favorites = remember(favIdsStr) {
        val set = mutableSetOf<String>()
        try {
            val arr = JSONArray(favIdsStr)
            for (i in 0 until arr.length()) set.add(arr.getString(i))
        } catch (e: Exception) {}
        set
    }

    val toggleFav = { id: String ->
        val set = favorites.toMutableSet()
        if (set.contains(id)) set.remove(id) else set.add(id)
        val arr = JSONArray()
        set.forEach { arr.put(it) }
        sp.edit().putString(KEY_FAVS, arr.toString()).apply()
        favIdsStr = arr.toString()
    }

    // Audio Player State — backed by the shared background Media3 service
    var currentlyPlayingId by remember { mutableStateOf<String?>(null) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    val controller = com.elhajri.noor.audio.NoorAudioController
    val audioPlaying by controller.isPlaying.collectAsState()

    // keep local flag in sync with the real shared player
    LaunchedEffect(audioPlaying) {
        isPlayingAudio = audioPlaying && currentlyPlayingId != null
        if (!audioPlaying) currentlyPlayingId = null
    }

    val playTrack = { track: Nasheed ->
        val ctx = controller.appContext
        if (ctx != null) {
            if (currentlyPlayingId == track.id) {
                controller.toggle()
                isPlayingAudio = controller.isPlaying.value
            } else {
                currentlyPlayingId = track.id
                isPlayingAudio = true
                controller.play(ctx, track.url, track.title)
            }
        }
    }

    // Category Visuals Mapping
    val categoryVisuals = mapOf(
        "prophet" to Pair("⭐", "عن النبي ﷺ"),
        "religious" to Pair("🤲", "دينية"),
        "arabic" to Pair("🌙", "عربية"),
        "english" to Pair("🌍", "إنجليزية"),
        "occasions" to Pair("💒", "مناسبات")
    )

    val filteredNasheeds = remember(selectedCategory, searchQuery, nasheeds) {
        nasheeds.filter { n ->
            val matchesCategory = selectedCategory == "all" || n.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    n.title.contains(searchQuery, ignoreCase = true) ||
                    n.artist.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مكتبة الأناشيد", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "رجوع", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
            )
        },
        containerColor = Navy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "أناشيد إسلامية بدون موسيقى — ${toArabicNumber(nasheeds.size)} نشيد",
                fontSize = 12.sp,
                color = GoldSoft.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن نشيد أو منشد...", color = GoldSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = GoldSoft) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NavyCard,
                    unfocusedContainerColor = NavyCard,
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = NavyLight,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Horizontal Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat.id == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.id },
                        label = { Text(cat.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold,
                            selectedLabelColor = Navy,
                            containerColor = NavyCard,
                            labelColor = GoldSoft
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nasheeds List
            if (filteredNasheeds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد نتائج", color = GoldSoft.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(filteredNasheeds, key = { _, item -> item.id }) { index, track ->
                        val isCurrent = currentlyPlayingId == track.id
                        val isPlaying = isCurrent && isPlayingAudio
                        val isFav = favorites.contains(track.id)
                        val visual = categoryVisuals[track.category] ?: Pair("🤲", "دينية")

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) NavyLight else NavyCard
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Expressive cover icon box
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Gold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(visual.first, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Track Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist,
                                        fontSize = 12.sp,
                                        color = GoldSoft.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(visual.second, fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.6f))
                                        Text(" · ", fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.6f))
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = GoldSoft.copy(alpha = 0.6f), modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(track.duration, fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.6f))
                                    }
                                }

                                // Favorite Heart Button
                                IconButton(
                                    onClick = { toggleFav(track.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "مفضلة",
                                        tint = if (isFav) Gold else GoldSoft.copy(alpha = 0.4f)
                                    )
                                }

                                // Play / Pause Button
                                IconButton(
                                    onClick = { playTrack(track) },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Gold)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل",
                                        tint = Navy,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
