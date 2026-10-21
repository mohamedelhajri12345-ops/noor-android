package com.elhajri.noor.offline

import android.content.Context
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.viewinterop.AndroidView
import com.elhajri.noor.audio.player.NasheedPlayerManager
import com.elhajri.noor.audio.player.PlayerTrack
import com.elhajri.noor.library.LibraryCatalogLoader
import com.elhajri.noor.library.LibraryStore
import com.elhajri.noor.nasheeds.Nasheed
import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import com.elhajri.noor.videos.KidsVideoStore
import kotlinx.coroutines.launch
import java.io.File
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onOpenBookReader: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var downloadedItems by remember { mutableStateOf(OfflineManager.getAllDownloadedItems(context)) }
    val downloadStatuses by OfflineManager.downloadStatus.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: الكل, 1: أناشيد, 2: كتب, 3: فيديوهات, 4: تنزيل المزيد
    var searchQuery by remember { mutableStateOf("") }

    var itemToDelete by remember { mutableStateOf<OfflineItem?>(null) }
    var activePlayingVideoItem by remember { mutableStateOf<OfflineItem?>(null) }

    fun refreshItems() {
        downloadedItems = OfflineManager.getAllDownloadedItems(context)
    }

    val totalStorageBytes = remember(downloadedItems) { downloadedItems.sumOf { it.sizeBytes } }
    val formattedStorage = remember(totalStorageBytes) { OfflineManager.formatFileSize(totalStorageBytes) }

    val filteredItems = remember(downloadedItems, selectedTab, searchQuery) {
        downloadedItems.filter { item ->
            val matchesTab = when (selectedTab) {
                1 -> item.type == OfflineItemType.NASHEED
                2 -> item.type == OfflineItemType.BOOK
                3 -> item.type == OfflineItemType.VIDEO
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.subtitle.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = t("مركز التنزيلات بدون إنترنت", "Download Manager"),
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = AmiriFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = t("رجوع", "Back"), tint = Gold)
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
                .themeScreenBackground()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Storage Overview Header
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t("المساحة المستخدمة:", "Storage Used: ") + formattedStorage,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${NoorLocale.formatDigits(downloadedItems.size)} " + t("عناصر محمّلة للعمل بدون إنترنت", "items downloaded for offline use"),
                            color = GoldSoft.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(t("ابحث في التنزيلات...", "Search downloads..."), color = GoldSoft.copy(alpha = 0.5f), fontSize = 14.sp) },
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

            // Tab Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf(
                    0 to t("الكل (${downloadedItems.size})", "All (${downloadedItems.size})"),
                    1 to t("الأناشيد", "Nasheeds"),
                    2 to t("الكتب", "Books"),
                    3 to t("الفيديوهات", "Videos"),
                    4 to t("تنزيل المزيد", "Download More")
                )
                items(tabs) { (id, title) ->
                    FilterChip(
                        selected = selectedTab == id,
                        onClick = { selectedTab = id },
                        label = { Text(title, fontSize = 12.sp) },
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

            if (selectedTab == 4) {
                // Download More Tab (Catalog browser)
                DownloadMoreCatalogSection(
                    onRefresh = { refreshItems() }
                )
            } else {
                // List of Downloaded Items
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = t("لا توجد ملفات محملة في هذا القسم", "No downloaded files in this section"),
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
                        items(filteredItems, key = { "${it.type}_${it.id}" }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyCard),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Gold.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (item.type) {
                                                OfflineItemType.NASHEED -> Icons.Default.MusicNote
                                                OfflineItemType.BOOK -> Icons.Default.Book
                                                OfflineItemType.VIDEO -> Icons.Default.Movie
                                            },
                                            contentDescription = null,
                                            tint = Gold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.title,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            OfflineGoldBadge(showText = false)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${item.subtitle} • ${OfflineManager.formatFileSize(item.sizeBytes)}",
                                            color = GoldSoft.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Action Play / Open Button
                                    IconButton(
                                        onClick = {
                                            when (item.type) {
                                                OfflineItemType.NASHEED -> {
                                                    NasheedPlayerManager.ensure(context)
                                                    NasheedPlayerManager.play(
                                                        PlayerTrack(
                                                            id = item.id,
                                                            url = item.localPath,
                                                            title = item.title,
                                                            artist = item.subtitle
                                                        )
                                                    )
                                                }
                                                OfflineItemType.BOOK -> {
                                                    onOpenBookReader(item.id)
                                                }
                                                OfflineItemType.VIDEO -> {
                                                    activePlayingVideoItem = item
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = t("تشغيل", "Play/Open"),
                                            tint = Gold
                                        )
                                    }

                                    // Action Delete Button
                                    IconButton(
                                        onClick = { itemToDelete = item }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = t("حذف", "Delete"),
                                            tint = GoldSoft.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
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

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = NavyCard,
            titleContentColor = Gold,
            title = { Text(t("تأكيد الحذف", "Confirm Delete"), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    t(
                        "هل أنت متأكد من حذف \"${item.title}\" من جهازك؟ ستتمكن من تنزيله لاحقاً عند الاتصال بالإنترنت.",
                        "Are you sure you want to delete \"${item.title}\" from your device? You can download it again later when connected."
                    ),
                    color = GoldSoft
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (item.type) {
                            OfflineItemType.NASHEED -> OfflineManager.deleteNasheed(context, item.id)
                            OfflineItemType.BOOK -> LibraryStore.deleteDownloadedBook(context, item.id)
                            OfflineItemType.VIDEO -> OfflineManager.deleteVideo(context, item.id)
                        }
                        itemToDelete = null
                        refreshItems()
                    }
                ) {
                    Text(t("حذف", "Delete"), color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(t("إلغاء", "Cancel"), color = GoldSoft)
                }
            }
        )
    }

    // In-App Video Playback Dialog for Local MP4 Videos
    activePlayingVideoItem?.let { videoItem ->
        AlertDialog(
            onDismissRequest = { activePlayingVideoItem = null },
            containerColor = Color.Black,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(videoItem.title, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    IconButton(onClick = { activePlayingVideoItem = null }) {
                        Icon(Icons.Default.Close, contentDescription = t("إغلاق", "Close"), tint = Gold)
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mc = MediaController(ctx)
                                mc.setAnchorView(this)
                                setMediaController(mc)
                                setVideoPath(videoItem.localPath)
                                start()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun DownloadMoreCatalogSection(
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val downloadStatuses by OfflineManager.downloadStatus.collectAsState()

    var catalogCategory by remember { mutableStateOf(0) } // 0: الأناشيد, 1: فيديوهات التطبيق

    val allNasheeds = remember {
        try {
            val jsonStr = context.assets.open("data/nasheeds.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)
            val arr = root.getJSONArray("nasheeds")
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Nasheed(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    artist = o.getString("artist"),
                    duration = o.optString("duration", ""),
                    category = o.getString("category"),
                    url = o.getString("url")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    val appVideos = remember {
        KidsVideoStore.filterVideos(context, t("فيديوهات التطبيق", "App Videos"))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            FilterChip(
                selected = catalogCategory == 0,
                onClick = { catalogCategory = 0 },
                label = { Text(t("الأناشيد (${allNasheeds.size})", "Nasheeds (${allNasheeds.size})"), fontSize = 12.sp) }
            )
            FilterChip(
                selected = catalogCategory == 1,
                onClick = { catalogCategory = 1 },
                label = { Text(t("فيديوهات التطبيق MP4 (${appVideos.size})", "App MP4 Videos (${appVideos.size})"), fontSize = 12.sp) }
            )
        }

        if (catalogCategory == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allNasheeds, key = { it.id }) { track ->
                    val isDownloaded = OfflineManager.isNasheedDownloaded(context, track.id)
                    val status = downloadStatuses[track.id]

                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(track.artist, color = GoldSoft.copy(alpha = 0.7f), fontSize = 12.sp)
                            }

                            if (isDownloaded) {
                                OfflineGoldBadge()
                            } else if (status?.isDownloading == true) {
                                CircularProgressIndicator(
                                    progress = { status.progress.coerceIn(0.1f, 1f) },
                                    modifier = Modifier.size(24.dp),
                                    color = Gold,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        if (!OfflineManager.isNetworkAvailable(context)) {
                                            showOfflineNoticeToast(context, track.title)
                                        } else {
                                            coroutineScope.launch {
                                                OfflineManager.downloadNasheed(
                                                    context = context,
                                                    id = track.id,
                                                    title = track.title,
                                                    artist = track.artist,
                                                    url = track.url,
                                                    onComplete = { success, _ -> if (success) onRefresh() }
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "تنزيل", tint = Gold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(appVideos, key = { it.id }) { video ->
                    val isDownloaded = OfflineManager.isVideoDownloaded(context, video.id)
                    val status = downloadStatuses[video.id]

                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(video.channel, color = GoldSoft.copy(alpha = 0.7f), fontSize = 12.sp)
                            }

                            if (isDownloaded) {
                                OfflineGoldBadge()
                            } else if (status?.isDownloading == true) {
                                CircularProgressIndicator(
                                    progress = { status.progress.coerceIn(0.1f, 1f) },
                                    modifier = Modifier.size(24.dp),
                                    color = Gold,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                val mp4 = video.mp4Url ?: ""
                                IconButton(
                                    onClick = {
                                        if (!OfflineManager.isNetworkAvailable(context)) {
                                            showOfflineNoticeToast(context, video.title)
                                        } else {
                                            coroutineScope.launch {
                                                OfflineManager.downloadVideo(
                                                    context = context,
                                                    id = video.id,
                                                    title = video.title,
                                                    channelOrCategory = video.channel,
                                                    url = mp4,
                                                    onComplete = { success, _ -> if (success) onRefresh() }
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = t("تنزيل فيديو", "Download Video"), tint = Gold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
