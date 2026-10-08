package com.elhajri.noor.videos

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.theme.ThemeStore
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.themeScreenBackground

/**
 * Kids Videos Library Screen ("مكتبة الفيديو للأطفال").
 *
 * Features:
 * - Category filter chips derived dynamically from data.
 * - Live search text field filtering by Arabic title.
 * - "متابعة المشاهدة" (Continue Watching) banner row for last watched video.
 * - LazyVerticalGrid (2 columns) with glass visual style, gold thin border, and video details.
 * - Rewarded ad / points gating dialog when daily free limit (3 videos) is reached.
 * - Zero crescent moon icons anywhere.
 */
@Composable
fun VideosScreen(
    onBack: () -> Unit,
    onOpenVideo: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val categories = remember { KidsVideoStore.getCategories(context) }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }

    val videos = remember(selectedCategory, searchQuery) {
        KidsVideoStore.filterVideos(context, selectedCategory, searchQuery)
    }

    // Continue Watching state
    val lastVideoId = remember { Prefs.getLastWatchedVideoId(context) }
    val lastVideoTitle = remember { Prefs.getLastWatchedVideoTitle(context) }
    val lastVideoPos = remember { Prefs.getLastWatchedVideoPosition(context) }
    val lastVideo = remember(lastVideoId) {
        if (!lastVideoId.isNullOrEmpty()) KidsVideoStore.getVideoById(context, lastVideoId) else null
    }

    // Gating Dialog State
    var pendingGatedVideoId by remember { mutableStateOf<String?>(null) }
    var gatingErrorMessage by remember { mutableStateOf<String?>(null) }

    fun handleVideoClick(video: KidsVideo) {
        if (VideoGatingStore.shouldGateVideo(context, video.id)) {
            pendingGatedVideoId = video.id
        } else {
            onOpenVideo(video.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Gold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مكتبة الفيديو للأطفال",
                        color = Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قصص الأنبياء والعظماء وسيرة النبي ﷺ — رسوم متحركة موثوقة",
                        color = GoldSoft,
                        fontSize = 11.sp
                    )
                }
            }

            // Search TextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("ابحث عن فيديو...", color = GoldSoft.copy(alpha = 0.6f), fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = Gold)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = GoldSoft)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Gold.copy(alpha = 0.35f),
                        focusedContainerColor = NavyCard.copy(alpha = 0.5f),
                        unfocusedContainerColor = NavyCard.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Continue Watching Banner Row ("متابعة المشاهدة")
            if (lastVideo != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .noorGlassCard(cornerRadius = 16.dp, borderWidth = 1.dp)
                        .clickable { handleVideoClick(lastVideo) },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = lastVideo.thumbnailUrl,
                                contentDescription = lastVideo.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Gold)
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "متابعة المشاهدة",
                                        color = Color(0xFF0D1330),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = lastVideo.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = lastVideo.channel,
                                color = GoldSoft,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "متابعة",
                            tint = Gold,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.15f))
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(Gold, Color(0xFFC5A028)))
                                } else {
                                    Brush.linearGradient(
                                        listOf(NavyCard.copy(alpha = 0.6f), NavyCard.copy(alpha = 0.3f))
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Gold else Gold.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color(0xFF0B1120) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Videos Grid (2 Columns)
            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد فيديوهات مطابقة للبحث",
                        color = GoldSoft,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            isGated = VideoGatingStore.shouldGateVideo(context, video.id),
                            onClick = { handleVideoClick(video) }
                        )
                    }
                }
            }
        }

        // Gating Unlock Dialog
        pendingGatedVideoId?.let { videoId ->
            val targetVideo = KidsVideoStore.getVideoById(context, videoId)
            val currentPoints = ThemeStore.getPoints(context)

            AlertDialog(
                onDismissRequest = { pendingGatedVideoId = null },
                containerColor = Color(0xFF0F172A),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "وصلت للحد اليومي المجاني",
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "لقد شاهدت ٣ فيديوهات مجانية اليوم. يمكنك فتح هذا الفيديو بمشاهدة إعلان مكافأة أو استخدام نقاطك.",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        if (targetVideo != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = targetVideo.title,
                                color = GoldSoft,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (gatingErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = gatingErrorMessage!!,
                                color = Color(0xFFFF6B6B),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Option 1: Watch Ad
                        Button(
                            onClick = {
                                if (activity != null) {
                                    VideoGatingStore.unlockWithAd(
                                        activity = activity,
                                        videoId = videoId,
                                        onSuccess = {
                                            pendingGatedVideoId = null
                                            onOpenVideo(videoId)
                                        },
                                        onFailure = {
                                            gatingErrorMessage = "تعذر تحميل الإعلان، حاول مرة أخرى"
                                        }
                                    )
                                } else {
                                    VideoGatingStore.markUnlocked(videoId)
                                    pendingGatedVideoId = null
                                    onOpenVideo(videoId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("شاهد إعلاناً للفتح", color = Color(0xFF0B1120), fontWeight = FontWeight.Bold)
                        }

                        // Option 2: Spend Points
                        OutlinedButton(
                            onClick = {
                                val success = VideoGatingStore.unlockWithPoints(context, videoId)
                                if (success) {
                                    pendingGatedVideoId = null
                                    onOpenVideo(videoId)
                                } else {
                                    gatingErrorMessage = "رصيد النقاط غير كافٍ ($currentPoints / ٥٠)"
                                }
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("فتح بـ ٥٠ نقطة (رصيدك: $currentPoints)", color = Gold, fontSize = 12.sp)
                        }

                        // Close Button
                        TextButton(
                            onClick = { pendingGatedVideoId = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إلغاء", color = GoldSoft)
                        }
                    }
                },
                dismissButton = null
            )
        }
    }
}

@Composable
private fun VideoCard(
    video: KidsVideo,
    isGated: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .noorGlassCard(cornerRadius = 18.dp, borderWidth = 1.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Thumbnail container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                // Play icon or Lock icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGated) Icons.Default.Lock else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Category Chip Badge on top right of thumbnail
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                        .border(0.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.category,
                        color = Gold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Video Details (Title & Channel)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Text(
                    text = video.channel,
                    color = GoldSoft,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
