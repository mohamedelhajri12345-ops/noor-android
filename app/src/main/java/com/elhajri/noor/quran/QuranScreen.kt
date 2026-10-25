package com.elhajri.noor.quran

import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
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

fun toArabicNumber(number: Int): String {
    return NoorLocale.formatDigits(number)
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
            title = t("القرآن الكريم", "The Holy Qur'an"),
            subtitle = t("١١٤ سورة · ٦٢٣٦ آية", "114 Surahs · 6,236 Verses")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search box — positioned in non-overlapping vertical layout
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(t("ابحث عن سورة...", "Search Surah..."), color = GoldSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = t("بحث", "Search"), tint = GoldSoft) },
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
                label = { Text(t("جميع السور (114)", "All Surahs (114)"), fontSize = 12.sp) },
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
                        Text(t("المفضلة (${favorites.size})", "Favorites (${favorites.size})"), fontSize = 12.sp)
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

        // Surah List with weight(1f) preventing overlay
        if (filteredSurahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showFavoritesOnly) t("لا توجد سور في المفضلة", "No favorite surahs yet") else t("لا توجد نتائج بحث", "No search results found"),
                    color = GoldSoft.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
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

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (NoorLocale.isEnglish) surah.englishName else t("سورة ", "Surah ") + surah.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            val surahTypeTranslated = when (surah.type) {
                                "مكية", "Meccan" -> t("مكية", "Meccan")
                                else -> t("مدنية", "Medinan")
                            }
                            Text(
                                text = "$surahTypeTranslated · ${toArabicNumber(surah.ayahs)} " + t("آية", "verses"),
                                fontSize = 12.sp,
                                color = GoldSoft.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = { FavoritesStore.toggle(context, surah.number) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = t("المفضلة", "Favorite"),
                                tint = if (isFav) Gold else GoldSoft.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactReciterStrip() {
    val context = LocalContext.current
    val activeReciterName = remember {
        val id = com.elhajri.noor.settings.NoorSettings.getReciter(context).ifBlank { com.elhajri.noor.data.Prefs.getReciter(context) }
        DataLoader.reciters(context).find { it.id == id }?.name ?: "مشاري العفاسي"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color = NavyCard)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = t("القارئ الحالي: ", "Current Reciter: ") + activeReciterName,
            color = GoldSoft,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
