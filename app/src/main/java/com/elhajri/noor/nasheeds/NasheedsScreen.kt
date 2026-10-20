package com.elhajri.noor.nasheeds

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
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

private fun toArabicNumber(number: Int): String {
    val englishDigits = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val builder = StringBuilder()
    for (ch in englishDigits) {
        if (ch in '0'..'9') {
            builder.append(arabicDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NasheedsScreen(onBack: () -> Unit) {
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

    // Audio player
    val playerState by com.elhajri.noor.audio.player.NasheedPlayerManager.state.collectAsState()
    val currentlyPlayingId = playerState.currentId
    val isPlayingAudio = playerState.isPlaying

    LaunchedEffect(playerState.lastError) {
        playerState.lastError?.let { err ->
            if (err.isNotBlank()) {
                android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    val filteredNasheeds = remember(selectedCategory, searchQuery, nasheeds) {
        nasheeds.filter { n ->
            val matchesCategory = selectedCategory == "all" || n.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    n.title.contains(searchQuery, ignoreCase = true) ||
                    n.artist.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }
    val playTrack = { track: Nasheed ->
        com.elhajri.noor.audio.player.NasheedPlayerManager.ensure(context)
        val queue = filteredNasheeds.map { t ->
            com.elhajri.noor.audio.player.PlayerTrack(
                id = t.id,
                url = t.url,
                title = t.title,
                artist = t.artist
            )
        }
        val idx = filteredNasheeds.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        if (currentlyPlayingId == track.id) {
            com.elhajri.noor.audio.player.NasheedPlayerManager.toggle()
        } else {
            com.elhajri.noor.audio.player.NasheedPlayerManager.playQueue(queue, idx)
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
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            com.elhajri.noor.ui.NoorSectionTitle(
                title = "الأناشيد",
                subtitle = "أناشيد إسلامية بدون موسيقى — ${toArabicNumber(nasheeds.size)} نشيد",
                modifier = Modifier.padding(bottom = 6.dp)
            )

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

            if (filteredNasheeds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد نتائج طابقة للبحث", color = GoldSoft)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredNasheeds) { track ->
                        val isPlayingThis = (currentlyPlayingId == track.id) && isPlayingAudio
                        val isFav = favorites.contains(track.id)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavyCard),
                            shape = RoundedCornerShape(12.dp),
                            onClick = { playTrack(track) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isPlayingThis) Gold else NavyLight,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = if (isPlayingThis) Navy else Gold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = track.artist,
                                        color = GoldSoft,
                                        fontSize = 12.sp
                                    )
                                }

                                IconButton(onClick = { toggleFav(track.id) }) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "مفضلة",
                                        tint = if (isFav) Color.Red else GoldSoft
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
