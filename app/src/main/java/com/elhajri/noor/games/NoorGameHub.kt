package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NoorCardSpec
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.themeScreenBackground

@Composable
fun NoorGameHubScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(InteractiveGameCategories.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // Force recomposition on favorite toggles
    var favoriteVersion by remember { mutableIntStateOf(0) }

    val activeTheme = NoorThemeState.active
    val totalStars = remember { StarEconomyStore.getStarBalance(context) }

    val filteredGames = remember(selectedCategory, searchQuery, showOnlyFavorites, favoriteVersion) {
        InteractiveGameRegistry.games.filter { game ->
            val matchesCategory = (selectedCategory == InteractiveGameCategories.ALL || game.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    game.titleAr.contains(searchQuery, ignoreCase = true) ||
                    game.titleEn.contains(searchQuery, ignoreCase = true) ||
                    game.descriptionAr.contains(searchQuery, ignoreCase = true)
            val matchesFavorite = !showOnlyFavorites || GameFavoritesStore.isFavorite(context, game.id)

            matchesCategory && matchesSearch && matchesFavorite
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .noorGlassCard(cornerRadius = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("رجوع", "Back"),
                        tint = Gold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t("ألعاب نور التفاعلية", "NOOR Games Hub"),
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = t("ألعاب مهارة وذكاء إسلامية ممتعة", "Fun Islamic Skill & Puzzle Games"),
                        color = TextSecondaryLinen,
                        fontSize = 12.sp
                    )
                }

                // Stars Badge
                Row(
                    modifier = Modifier
                        .noorGlassCard(cornerRadius = 16.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = totalStars.toString().toArabicDigits(),
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Search Bar & Favorites Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            t("ابحث عن لعبة...", "Search games..."),
                            color = TextSecondaryLinen.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Gold
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondaryLinen
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavyCard.copy(alpha = 0.5f),
                        unfocusedContainerColor = NavyCard.copy(alpha = 0.3f),
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Gold.copy(alpha = 0.3f),
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Favorite Filter Toggle Button
                IconButton(
                    onClick = { showOnlyFavorites = !showOnlyFavorites },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (showOnlyFavorites) Gold.copy(alpha = 0.25f)
                            else NavyCard.copy(alpha = 0.3f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (showOnlyFavorites) Gold else Gold.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorites",
                        tint = if (showOnlyFavorites) Color(0xFFFF5252) else TextSecondaryLinen
                    )
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(InteractiveGameCategories.list) { category ->
                    val isSelected = (selectedCategory == category)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Gold
                                else NavyCard.copy(alpha = 0.4f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Gold else Gold.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.Black else TextMain,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Games Grid
            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = TextSecondaryLinen.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = t("لا توجد ألعاب مطابقة", "No games found"),
                            color = TextSecondaryLinen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredGames, key = { it.id }) { game ->
                        val isFav = GameFavoritesStore.isFavorite(context, game.id)

                        GameCardItem(
                            game = game,
                            isFavorite = isFav,
                            onFavoriteToggle = {
                                GameFavoritesStore.toggleFavorite(context, game.id)
                                favoriteVersion++
                            },
                            onClick = { onNavigate(game.route) }
                        )
                    }

                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCardItem(
    game: InteractiveGame,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(NoorCardSpec.HeightGameCard) // UNIFORM HeightGameCard = 145.dp
            .clip(RoundedCornerShape(16.dp))
            .noorGlassCard(cornerRadius = 16.dp)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon + Favorite Heart Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Game Icon Container
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(game.accentColor.copy(alpha = 0.2f))
                        .border(1.dp, game.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = game.icon,
                        contentDescription = game.titleAr,
                        tint = game.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Favorite Heart Button
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFF5252) else TextSecondaryLinen.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Content: Title & Subtitle / Category
            Column {
                Text(
                    text = game.titleAr,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = game.descriptionAr.ifEmpty { game.category },
                    color = TextSecondaryLinen,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
