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

private fun translateReciterName(name: String): String = when (name) {
    "مشاري العفاسي" -> t("مشاري العفاسي", "Mishary Alafasy")
    "عبد الباسط عبد الصمد" -> t("عبد الباسط عبد الصمد", "Abdul Basit Abdul Samad")
    "ياسر الدوسري" -> t("ياسر الدوسري", "Yasser Al-Dosari")
    "سعود الشريم" -> t("سعود الشريم", "Sa'ud Al-Shuraim")
    "عبد الرحمن السدیس", "عبد الرحمن السديس" -> t("عبد الرحمن السديس", "Abdul Rahman Al-Sudais")
    "ماهر المعيقلي" -> t("ماهر المعيقلي", "Maher Al-Muaiqly")
    "محمود خليل الحصري" -> t("محمود خليل الحصري", "Mahmoud Khalil Al-Husary")
    "محمد صديق المنشاوي" -> t("محمد صديق المنشاوي", "Mohamed Siddiq Al-Minshawi")
    "أحمد العجمي" -> t("أحمد العجمي", "Ahmed Al-Ajmi")
    "أبو بكر الشاطري" -> t("أبو بكر الشاطري", "Abu Bakr Al-Shatri")
    "سعد الغامدي" -> t("سعد الغامدي", "Saad Al-Ghamdi")
    else -> name
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

        // Search box
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

        // Surah List
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

                            IconButton(
                                onClick = { FavoritesStore.toggleFavorite(context, surah.number) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = t("مفضلة", "Favorite"),
                                    tint = if (isFav) Gold else GoldSoft.copy(alpha = 0.4f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = t("قراءة", "Read"),
                                tint = GoldSoft.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                    }
                }
            }
        }
    }
}

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
        Text(t("القارئ", "Reciter"), color = GoldSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                        text = translateReciterName(reciter.name),
                        color = if (selected) Color(0xFF0D2B1F) else GoldSoft,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        IconButton(
            onClick = {
                com.elhajri.noor.quran.ReciterOffline.downloadAll(context, currentReciter, currentReciter.servers)
            },
            modifier = Modifier.size(30.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = t("تنزيل التلاوات دون إنترنت", "Download recitations offline"), tint = Gold, modifier = Modifier.size(16.dp))
        }
    }
}
