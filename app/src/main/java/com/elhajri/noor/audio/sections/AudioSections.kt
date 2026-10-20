package com.elhajri.noor.audio.sections

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.audio.player.PlayerCore
import com.elhajri.noor.audio.player.PlayerTrack
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

// ---------------------------------------------------------------------------
// Data models
// ---------------------------------------------------------------------------

data class PodcastEpisode(
    val id: String,
    val title: String,
    val audioUrl: String,
    val duration: String,
    val description: String
)

data class PodcastShow(
    val id: String,
    val title: String,
    val host: String,
    val categoryAr: String,
    val coverUrl: String,
    val description: String,
    val episodes: List<PodcastEpisode>
)

data class RuqyahAudio(
    val id: String,
    val title: String,
    val reciter: String,
    val category: String,
    val categoryAr: String,
    val audioUrl: String,
    val duration: String,
    val description: String
)

data class RuqyahAyahGroup(
    val id: String,
    val title: String,
    val description: String,
    val verses: List<String>
)

// ---------------------------------------------------------------------------
// Data loading + resume-position storage
// ---------------------------------------------------------------------------

object AudioSectionsData {

    private fun readArray(context: Context, file: String): JSONArray =
        JSONArray(context.assets.open("data/$file").bufferedReader().use { it.readText() })

    fun podcasts(context: Context): List<PodcastShow> {
        val arr = readArray(context, "podcasts.json")
        val shows = mutableListOf<PodcastShow>()
        for (i in 0 until arr.length()) {
            val sh = arr.getJSONObject(i)
            val epsArr = sh.optJSONArray("episodes") ?: JSONArray()
            val eps = mutableListOf<PodcastEpisode>()
            for (j in 0 until epsArr.length()) {
                val ep = epsArr.getJSONObject(j)
                eps.add(
                    PodcastEpisode(
                        id = ep.optString("id", "$i-$j"),
                        title = ep.optString("title", ""),
                        audioUrl = ep.optString("audio_url", ""),
                        duration = ep.optString("duration", ""),
                        description = ep.optString("description", "")
                    )
                )
            }
            shows.add(
                PodcastShow(
                    id = sh.optString("id", "show$i"),
                    title = sh.optString("title", ""),
                    host = sh.optString("host", ""),
                    categoryAr = sh.optString("category_ar", ""),
                    coverUrl = sh.optString("cover_url", ""),
                    description = sh.optString("description", ""),
                    episodes = eps
                )
            )
        }
        return shows
    }

    fun ruqyahAudio(context: Context): List<RuqyahAudio> {
        val arr = readArray(context, "ruqyah_audio.json")
        return (0 until arr.length()).map { i ->
            val o: JSONObject = arr.getJSONObject(i)
            RuqyahAudio(
                id = o.optString("id", "r$i"),
                title = o.optString("title", ""),
                reciter = o.optString("reciter", ""),
                category = o.optString("category", "ruqyah_general"),
                categoryAr = o.optString("category_ar", "رقية عامة"),
                audioUrl = o.optString("audio_url", ""),
                duration = o.optString("duration", ""),
                description = o.optString("description", "")
            )
        }
    }

    fun ruqyahAyat(context: Context): List<RuqyahAyahGroup> {
        val arr = readArray(context, "ruqyah_ayat.json")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val versesArr = o.optJSONArray("verses") ?: JSONArray()
            val verses = mutableListOf<String>()
            for (j in 0 until versesArr.length()) {
                val v = versesArr.get(j)
                verses.add(v.toString())
            }
            RuqyahAyahGroup(
                id = o.optString("id", "g$i"),
                title = o.optString("title", ""),
                description = o.optString("description", ""),
                verses = verses
            )
        }
    }

    // Resume-where-you-left for podcast episodes
    fun savePosition(context: Context, episodeId: String, seconds: Float) {
        context.getSharedPreferences("audio_sections_prefs", Context.MODE_PRIVATE)
            .edit().putFloat("pos_$episodeId", seconds).apply()
    }

    fun getPosition(context: Context, episodeId: String): Float =
        context.getSharedPreferences("audio_sections_prefs", Context.MODE_PRIVATE)
            .getFloat("pos_$episodeId", 0f)
}

// ---------------------------------------------------------------------------
// Podcasts screen
// ---------------------------------------------------------------------------

@Composable
fun PodcastsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val shows = remember { AudioSectionsData.podcasts(context) }
    var expandedShowId by remember { mutableStateOf<String?>(null) }
    val playerState by PlayerCore.state.collectAsState()

    // Persist playback position every 3 seconds for resume support
    LaunchedEffect(playerState.currentId, playerState.isPlaying) {
        val id = playerState.currentId ?: return@LaunchedEffect
        while (true) {
            delay(3000)
            val s = PlayerCore.state.value
            if (s.currentId == id && s.isPlaying && s.currentTime.isFinite() && s.currentTime > 0f) {
                AudioSectionsData.savePosition(context, id, s.currentTime)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
            }
            Icon(Icons.Filled.SpatialAudio, contentDescription = null, tint = Gold)
            Spacer(Modifier.width(8.dp))
            Text(
                "البودكاست الإسلامي",
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "برامج ودروس صوتية مختارة بأسلوب هادئ — صوت فقط بدون موسيقى",
            color = TextSecondaryLinen,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shows, key = { it.id }) { show ->
                PodcastShowCard(
                    show = show,
                    expanded = expandedShowId == show.id,
                    playingId = playerState.currentId,
                    isPlaying = playerState.isPlaying,
                    onToggleExpand = {
                        SfxTap(context)
                        expandedShowId = if (expandedShowId == show.id) null else show.id
                    },
                    onPlayEpisode = { episode ->
                        SfxTap(context)
                        val tracks = show.episodes.filter { it.audioUrl.isNotBlank() }.map { ep ->
                            PlayerTrack(
                                id = ep.id,
                                url = ep.audioUrl,
                                title = ep.title,
                                artist = show.host
                            )
                        }
                        val idx = tracks.indexOfFirst { it.id == episode.id }.coerceAtLeast(0)
                        PlayerCore.ensure(context)
                        PlayerCore.playQueue(tracks, idx)
                        // Resume from saved position if any
                        val saved = AudioSectionsData.getPosition(context, episode.id)
                        if (saved > 10f) {
                            val currentId = episode.id
                            Thread {
                                try {
                                    Thread.sleep(1200)
                                    val s = PlayerCore.state.value
                                    if (s.currentId == currentId && s.isPlaying) {
                                        PlayerCore.seek(saved)
                                    }
                                } catch (_: Exception) {
                                }
                            }.start()
                        }
                    }
                )
            }
        }
    }
}

private fun SfxTap(context: Context) {
    try {
        com.elhajri.noor.audio.SfxManager.playTap(context)
    } catch (_: Exception) {
    }
}

@Composable
private fun PodcastShowCard(
    show: PodcastShow,
    expanded: Boolean,
    playingId: String?,
    isPlaying: Boolean,
    onToggleExpand: () -> Unit,
    onPlayEpisode: (PodcastEpisode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard()
            .clickable { onToggleExpand() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = show.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .background(NavyCard)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    show.title,
                    color = TextMain,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(show.host, color = Gold, fontSize = 13.sp)
                Text(
                    "${show.episodes.size} حلقة",
                    color = TextSecondaryLinen,
                    fontSize = 12.sp
                )
            }
        }
        if (!expanded && show.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                show.description,
                color = TextSecondaryLinen,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            show.episodes.forEach { ep ->
                val thisPlaying = playingId == ep.id
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (thisPlaying) Gold.copy(alpha = 0.12f) else Color.Transparent
                        )
                        .clickable { onPlayEpisode(ep) }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (thisPlaying && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            ep.title,
                            color = TextMain,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(ep.duration, color = TextSecondaryLinen, fontSize = 11.sp)
                    }
                    if (thisPlaying) {
                        Icon(
                            Icons.Filled.Headphones,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ruqyah screen
// ---------------------------------------------------------------------------

private val ruqyahCategories = listOf(
    "ruqyah_eye" to "رقية العين والحسد",
    "ruqyah_sihr" to "رقية السحر",
    "ruqyah_mas" to "رقية الشعوذة والمس",
    "ruqyah_general" to "رقية عامة"
)

@Composable
fun RuqyahScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val items = remember { AudioSectionsData.ruqyahAudio(context) }
    val ayat = remember { AudioSectionsData.ruqyahAyat(context) }
    var selectedCategory by remember { mutableStateOf("ruqyah_eye") }
    val playerState by PlayerCore.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
            }
            Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Gold)
            Spacer(Modifier.width(8.dp))
            Text(
                "الرقية الشرعية المسموعة",
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Category chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ruqyahCategories, key = { it.first }) { (key, label) ->
                val selected = selectedCategory == key
                Text(
                    label,
                    color = if (selected) Navy else Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) Gold else Gold.copy(alpha = 0.12f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { SfxTap(context); selectedCategory = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Now playing calm card
            if (playerState.currentId != null && items.any { it.id == playerState.currentId }) {
                item { RuqyahNowPlayingCard(playerState) }
            }
            // Recitations of the selected category
            val filtered = items.filter { it.category == selectedCategory }
            items(filtered, key = { it.id }) { r ->
                RuqyahItemCard(
                    item = r,
                    playing = playerState.currentId == r.id && playerState.isPlaying
                ) {
                    SfxTap(context)
                    PlayerCore.ensure(context)
                    PlayerCore.play(
                        PlayerTrack(
                            id = r.id,
                            url = r.audioUrl,
                            title = r.title,
                            artist = r.reciter
                        )
                    )
                }
            }
            // Ayat text section (works fully offline)
            item {
                Text(
                    "آيات وفوائد الرقية الشرعية",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "تعمل بدون إنترنت — اقرأها بيقين وتدبر",
                    color = TextSecondaryLinen,
                    fontSize = 12.sp
                )
            }
            items(ayat, key = { "ayat_" + it.id }) { g ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard()
                        .padding(14.dp)
                ) {
                    Text(g.title, color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    if (g.description.isNotBlank()) {
                        Text(
                            g.description,
                            color = TextSecondaryLinen,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    g.verses.forEach { v ->
                        Text(
                            v,
                            color = TextMain,
                            fontSize = 15.sp,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuqyahItemCard(item: RuqyahAudio, playing: Boolean, onPlay: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard()
            .clickable { onPlay() }
            .padding(14.dp)
    ) {
        Icon(
            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                color = TextMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(item.reciter, color = Gold, fontSize = 12.sp)
            Text(item.duration, color = TextSecondaryLinen, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RuqyahNowPlayingCard(state: com.elhajri.noor.audio.player.PlayerState) {
    val dur = if (state.duration.isFinite() && state.duration > 0f) state.duration else 1f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard()
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Headphones, contentDescription = null, tint = Gold)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    state.title,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "اسمع بيقين وحضور قلب — صوت هادئ",
                    color = TextSecondaryLinen,
                    fontSize = 11.sp
                )
            }
            Icon(
                if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier
                    .size(34.dp)
                    .clickable {
                        try { PlayerCore.toggle() } catch (_: Exception) {}
                    }
            )
        }
        Slider(
            value = (if (state.currentTime.isFinite()) state.currentTime else 0f).coerceIn(0f, dur),
            onValueChange = { v ->
                try { PlayerCore.seek(v) } catch (_: Exception) {}
            },
            valueRange = 0f..dur,
            colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = Gold,
                inactiveTrackColor = Gold.copy(alpha = 0.2f)
            )
        )
    }
}
