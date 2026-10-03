package com.elhajri.noor.athkar

import com.elhajri.noor.ui.themeScreenBackground
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.NoorGradients
import com.elhajri.noor.ui.NoorHeroBanner
import com.elhajri.noor.ui.NoorHeroImages

internal fun toArabicNumber(number: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return number.toString().map { map[it] ?: it }.joinToString("")
}

@Composable
fun AthkarScreen(onOpenCategory: (String, String) -> Unit = { _, _ -> }) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (selectedCategory != null) {
        AthkarDetailScreen(
            categoryId = selectedCategory!!.first,
            title = selectedCategory!!.second,
            onBack = { selectedCategory = null }
        )
    } else {
        val categories = remember { DataLoader.athkarCategories(context) }
        val sp = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .themeScreenBackground()
                .padding(16.dp)
        ) {
            com.elhajri.noor.ui.NoorSectionTitle(
                title = "الأذكار",
                subtitle = "حصنك من كل شر بإذن الله",
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { cat ->
                    val isCompleted = sp.getBoolean("athkar_completed_${cat.id}", false)
                    val dhikrs = remember(cat.id) { DataLoader.athkar(context, cat.id) }
                    val completedCount = dhikrs.indices.count { i ->
                        val target = dhikrs[i].count
                        val saved = sp.getInt("athkar_count_${cat.id}_$i", 0)
                        saved >= target
                    }
                    val totalCount = dhikrs.size
                    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenCategory(cat.id, cat.name)
                                selectedCategory = Pair(cat.id, cat.name)
                            },
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Gold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "مكتمل",
                                            tint = Gold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = toArabicNumber(cat.count),
                                            color = Gold,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = cat.name,
                                            color = TextMain,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isCompleted) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Gold.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "مكتمل",
                                                    color = Gold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = cat.subtitle,
                                        color = GoldSoft.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (!isCompleted && progress > 0f) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = progress,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(CircleShape),
                                            color = Gold,
                                            trackColor = NavyLight
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = GoldSoft,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
