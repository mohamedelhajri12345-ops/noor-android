package com.elhajri.noor.favorites

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Surah
import com.elhajri.noor.quran.FavoritesStore
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NoorGradients

object StoryFavorites {
    private const val KEY = "favorite_stories"
    fun get(context: Context): List<Int> =
        context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
            .getString(KEY, "")!!.split(",").mapNotNull { it.toIntOrNull() }
    fun toggle(context: Context, id: Int) {
        val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val cur = get(context).toMutableList()
        if (cur.contains(id)) cur.remove(id) else cur.add(id)
        sp.edit().putString(KEY, cur.joinToString(",")).apply()
    }
}

@Composable
fun FavoritesScreen(onOpenSurah: (Surah) -> Unit, onOpenStory: (Int) -> Unit) {
    val context = LocalContext.current
    FavoritesStore.init(context)
    val surahs = remember { DataLoader.surahs(context) }
    val stories = remember { DataLoader.stories(context) }
    var favSurahIds by remember { mutableStateOf(FavoritesStore.favorites.value) }
    var favStoryIds by remember { mutableStateOf(StoryFavorites.get(context)) }
    LaunchedEffect(Unit) { FavoritesStore.favorites.collect { favSurahIds = it } }

    Column(modifier = Modifier.fillMaxSize().background(NoorGradients.ScreenBackground).padding(16.dp)) {
        Text("المفضلة", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("السور والقصص التي تتابعها", color = GoldSoft, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        val favSurahs = surahs.filter { it.number in favSurahIds }
        val favStories = stories.filter { it.id in favStoryIds }
        if (favSurahs.isEmpty() && favStories.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("لا توجد عناصر في المفضلة بعد\nاضغط على ♡ بجانب أي سورة أو قصة لحفظها هنا", color = GoldSoft.copy(alpha = 0.6f), fontSize = 14.sp)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (favSurahs.isNotEmpty()) {
                item { Text("السور المفضلة", color = Gold, fontWeight = FontWeight.Bold) }
                items(favSurahs) { s ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card), shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onOpenSurah(s) }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("﴿ ${s.name} ﴾", color = GoldSoft, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text("آية ${s.ayahs}", color = GoldSoft.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                }
            }
            if (favStories.isNotEmpty()) {
                item { Text("القصص المفضلة", color = Gold, fontWeight = FontWeight.Bold) }
                items(favStories) { st ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card), shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onOpenStory(st.id) }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(st.title, color = GoldSoft, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
