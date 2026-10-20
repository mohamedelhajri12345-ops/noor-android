package com.elhajri.noor.library

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MenuBook
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
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.noorGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onOpenReader: (String) -> Unit
) {
    val context = LocalContext.current
    val allBooks = remember { LibraryCatalogLoader.loadCatalog(context) }

    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: الكتالوج, 1: متابعة القراءة, 2: رف التنزيلات

    var selectedBookForDetail by remember { mutableStateOf<Book?>(null) }

    val readingProgressList = remember(selectedTab) { LibraryStore.getAllReadingProgressObjects(context) }
    val downloadedMap = remember(selectedTab) { LibraryStore.getDownloadedBooksMap(context) }

    val filteredBooks = remember(selectedCategory, searchQuery, selectedTab, allBooks) {
        when (selectedTab) {
            1 -> { // متابعة القراءة
                val progressIds = readingProgressList.map { it.bookId }.toSet()
                allBooks.filter { b ->
                    progressIds.contains(b.id) &&
                            (searchQuery.isBlank() || b.title.contains(searchQuery, true) || b.author.contains(searchQuery, true))
                }
            }
            2 -> { // رف التنزيلات
                allBooks.filter { b ->
                    LibraryStore.isBookAvailableLocally(context, b) &&
                            (searchQuery.isBlank() || b.title.contains(searchQuery, true) || b.author.contains(searchQuery, true))
                }
            }
            else -> { // جميع الكتب
                allBooks.filter { b ->
                    val matchesCat = selectedCategory == "all" || b.category == selectedCategory
                    val matchesQuery = searchQuery.isBlank() ||
                            b.title.contains(searchQuery, ignoreCase = true) ||
                            b.author.contains(searchQuery, ignoreCase = true)
                    matchesCat && matchesQuery
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المكتبة الإسلامية", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = AmiriFamily) },
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
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Banner
            com.elhajri.noor.ui.NoorSectionTitle(
                title = "المكتبة",
                subtitle = "٥٥٥ كتاب وقصة إسلامية معتمدة — بدون إعلانات",
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث باسم الكتاب أو المؤلف...", color = GoldSoft.copy(alpha = 0.5f), fontSize = 13.sp) },
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

            // Section Tabs (جميع الكتب / متابعة القراءة / رف التنزيلات)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavyCard,
                contentColor = Gold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("جميع الكتب (${allBooks.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("متابعة القراءة", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("التنزيلات", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips Row (Shown for "جميع الكتب" tab)
            if (selectedTab == 0) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LibraryCategories.LIST) { cat ->
                        val isSelected = cat.id == selectedCategory
                        val count = LibraryCatalogLoader.getCategoryCount(allBooks, cat.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat.id },
                            label = { Text("${cat.iconEmoji} ${cat.nameAr} ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold,
                                selectedLabelColor = Navy,
                                containerColor = NavyCard,
                                labelColor = GoldSoft
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Continue Reading Carousel (if on Tab 0 and has progress)
            if (selectedTab == 0 && readingProgressList.isNotEmpty() && searchQuery.isBlank()) {
                val inProgressBooks = remember(readingProgressList, allBooks) {
                    readingProgressList.mapNotNull { prog ->
                        allBooks.find { it.id == prog.bookId }?.let { b -> Pair(b, prog) }
                    }.take(5)
                }

                if (inProgressBooks.isNotEmpty()) {
                    Text("استكمال القراءة", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(inProgressBooks) { (book, prog) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(200.dp)
                                    .clickable { onOpenReader(book.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(book.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(Modifier.height(2.dp))
                                        Text("صفحة ${prog.lastPage + 1} من ${prog.totalPages}", color = GoldSoft, fontSize = 10.sp)
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = prog.progressPercentage / 100f,
                                            color = Gold,
                                            trackColor = NavyLight,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Book Cover Grid
            if (filteredBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 2) "لا توجد كتب منزّلة حالياً" else if (selectedTab == 1) "لم تبدأ قراءة أي كتاب بعد" else "لا توجد كتب تطابق البحث",
                        color = GoldSoft,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredBooks) { book ->
                        val isLocal = LibraryStore.isBookAvailableLocally(context, book)
                        val prog = LibraryStore.getProgress(context, book.id)

                        BookGridCard(
                            book = book,
                            isDownloaded = isLocal,
                            readingProgress = prog,
                            onClick = { selectedBookForDetail = book }
                        )
                    }
                }
            }
        }
    }

    // Detail Modal Sheet
    selectedBookForDetail?.let { book ->
        BookDetailSheet(
            book = book,
            onDismiss = { selectedBookForDetail = null },
            onReadClick = { b ->
                selectedBookForDetail = null
                onOpenReader(b.id)
            }
        )
    }
}

@Composable
private fun BookGridCard(
    book: Book,
    isDownloaded: Boolean,
    readingProgress: ReadingProgress?,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .noorGlassCard(cornerRadius = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Book Cover Aspect Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF13281E), Color(0xFF09120D))
                        )
                    )
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = book.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = AmiriFamily
                        )
                    }
                }

                // Downloaded Badge
                if (isDownloaded || book.bundledAsset != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Gold, shape = CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "أوفلاين",
                            tint = Navy,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = book.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Author
            Text(
                text = book.author,
                color = GoldSoft,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Page count & Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${book.pages} ص",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )

                if (readingProgress != null && readingProgress.progressPercentage > 0) {
                    Text(
                        text = "${readingProgress.progressPercentage}%",
                        color = Gold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
