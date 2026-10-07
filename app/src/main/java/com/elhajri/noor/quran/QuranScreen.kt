package com.elhajri.noor.quran

import com.elhajri.noor.ui.themeScreenBackground
import com.elhajri.noor.ui.noorGlassCard
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Surah
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.NoorGradients

fun toArabicNumber(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { if (it.isDigit()) arabicDigits[it - '0'] else it }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(onSurahClick: (Surah) -> Unit) {
    val context = LocalContext.current
    val allSurahs = remember { DataLoader.surahs(context) }
    
    LaunchedEffect(Unit) {
        FavoritesStore.init(context)
    }

    val favorites by FavoritesStore.favorites.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    val filteredSurahs = remember(searchQuery, showFavoritesOnly, favorites, allSurahs) {
        allSurahs.filter { surah ->
            val matchesSearch = searchQuery.isBlank() ||
                    surah.name.contains(searchQuery, ignoreCase = true) ||
                    surah.englishName.contains(searchQuery, ignoreCase = true) ||
                    surah.number.toString().contains(searchQuery)
            val matchesFav = !showFavoritesOnly || favorites.contains(surah.number)
            matchesSearch && matchesFav
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title
        com.elhajri.noor.ui.NoorSectionTitle(
            title = "القرآن الكريم",
            subtitle = "١١٤ سورة · ٦٢٣٦ آية"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن سورة...", color = GoldSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
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

        // ===== شريط القارئ المدمج — اختيار المقرئ والتنزيل بلا إنترنت من الشاشة الرئيسية =====
        CompactReciterStrip()

        Spacer(modifier = Modifier.height(10.dp))

        // Filter tabs: All vs Favorites
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showFavoritesOnly,
                onClick = { showFavoritesOnly = false },
                label = { Text("جميع السور (114)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gold,
                    selectedLabelColor = Navy,
                    containerColor = NavyCard,
                    labelColor = GoldSoft
                )
            )
            FilterChip(
                selected = showFavoritesOnly,
                onClick = { showFavoritesOnly = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("المفضلة (${favorites.size})", fontSize = 12.sp)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gold,
                    selectedLabelColor = Navy,
                    containerColor = NavyCard,
                    labelColor = GoldSoft
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Surah List
        if (filteredSurahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showFavoritesOnly) "لا توجد سور في المفضلة" else "لا توجد نتائج بحث",
                    color = GoldSoft.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    val isFav = favorites.contains(surah.number)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 16.dp)
                            .clickable { onSurahClick(surah) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                            // Surah Number Medallion
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gold.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = toArabicNumber(surah.number),
                                    color = Gold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Surah Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "سورة ${surah.name}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${surah.type} · ${toArabicNumber(surah.ayahs)} آية",
                                    fontSize = 12.sp,
                                    color = GoldSoft.copy(alpha = 0.7f)
                                )
                            }

                            // Favorite Toggle Button
                            IconButton(
                                onClick = { FavoritesStore.toggleFavorite(context, surah.number) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "مفضلة",
                                    tint = if (isFav) Gold else GoldSoft.copy(alpha = 0.4f)
                                )
                            }

                            // Open Book Icon
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "قراءة",
                                tint = GoldSoft.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                    }
                }
            }
        }
    }
}

/**
 * شريط القارئ المدمج: اسم المقرئ المختار + شريحة صغيرة لكل مقرئ + تنزيل دون إنترنت للمختار.
 * يعرض بطاقة رفيعة واحدة لا تستهلك مساحة كبيرة.
 */
@Composable
private fun CompactReciterStrip() {
    val context = LocalContext.current
    val allReciters = remember { DataLoader.reciters(context) }
    val savedReciterId = remember { com.elhajri.noor.settings.NoorSettings.getReciter(context).ifBlank { com.elhajri.noor.data.Prefs.getReciter(context) } }
    var currentReciter by remember {
        mutableStateOf(
            allReciters.find { it.id == savedReciterId } ?: allReciters.firstOrNull()
                ?: com.elhajri.noor.data.Reciter("alafasy", "مشاري العفاسي", listOf("https://server11.mp3quran.net/afs/"))
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard(cornerRadius = 14.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("القارئ", color = GoldSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(allReciters.size) { i ->
                val reciter = allReciters[i]
                val selected = reciter.id == currentReciter.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) Gold else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (selected) Color.Transparent else Gold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .clickable {
                            currentReciter = reciter
                            com.elhajri.noor.data.Prefs.setReciter(context, reciter.id)
                            com.elhajri.noor.settings.NoorSettings.setReciter(context, reciter.id)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = reciter.name,
                        color = if (selected) Color(0xFF0D2B1F) else GoldSoft,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        // تنزيل دون إنترنت للمقرئ المختار — أيقونة مدمجة بلا مساحة كبيرة
        IconButton(
            onClick = {
                com.elhajri.noor.quran.ReciterOffline.downloadAll(context, currentReciter, currentReciter.servers)
            },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = "تنزيل التلاوات دون إنترنت", tint = Gold, modifier = Modifier.size(16.dp))
        }
    }
}
